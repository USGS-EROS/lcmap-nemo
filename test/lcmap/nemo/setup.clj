(ns lcmap.nemo.setup
  (:require [clj-uuid :as uuid]
            [clojure.set]
            [clojure.spec.alpha :as spec]
            [clojure.spec.gen.alpha :as gen]
            [clojure.tools.logging :as log]
            [lcmap.nemo.config :as config]
            [lcmap.nemo.util :as util]
            [qbits.alia :as alia]
            [qbits.hayt :as hayt]))

(def INT_MIN      (* Integer/MIN_VALUE))
(def INT_MAX      (* Integer/MAX_VALUE))
(def BIGINT_MIN   (* 2 INT_MIN))
(def BIGINT_MAX   (* 2 INT_MAX))
(def DB_ROW_COUNT 500)

;; Create custom spec generators for types we are testing that don't have a direct generator
;; available
; As of 3.1, CQL only supports uuid v1 (v0 is null)
(def uuid-gen #(spec/gen #{(uuid/v1)}))
(def byte-gen #(spec/gen (spec/int-in 0 256)))
(def inet-gen #(gen/fmap (fn [[a b c d]] (format "%s.%s.%s.%s" a b c d))
                         (gen/tuple (byte-gen) (byte-gen) (byte-gen) (byte-gen))))

(spec/def ::bigint (spec/int-in BIGINT_MIN BIGINT_MAX))
(spec/def ::timestamp pos-int?)
(spec/def ::blob bytes?)
(spec/def ::text string?)
(spec/def ::float float?)
(spec/def ::double double?)
(spec/def ::ascii (spec/with-gen util/ascii? gen/string-ascii))
(spec/def ::boolean boolean?)
(spec/def ::counter int?)
(spec/def ::decimal  (or (spec/int-in INT_MIN INT_MAX) (spec/double-in :NaN? false :infinite? false)))
(spec/def ::inet     (spec/with-gen util/ipv4? inet-gen))
(spec/def ::timeuuid (spec/with-gen uuid/uuid? uuid-gen))
(spec/def ::uuid     (spec/with-gen uuid/uuid? gen/uuid))
(spec/def ::varchar string?)
(spec/def ::varint (spec/int-in 0 10))
(spec/def ::set  (spec/every string? :kind set?))
(spec/def ::map  (spec/map-of string? string?))
(spec/def ::list (spec/every string? :kind list?))
(spec/def ::nil nil?)

(spec/def ::pk1 ::bigint)
(spec/def ::pk2 ::bigint)
(spec/def ::pk3 ::timestamp)
(spec/def ::f1  ::blob)
(spec/def ::f2  ::text)
(spec/def ::f3  ::float)
(spec/def ::f4  ::double)
(spec/def ::f5  ::ascii)
(spec/def ::f6  ::boolean)
(spec/def ::f7  ::decimal)
(spec/def ::f8  ::inet)
(spec/def ::f9  ::timeuuid)
(spec/def ::f10 ::uuid)
(spec/def ::f11 ::varchar)
(spec/def ::f12 ::varint)
(spec/def ::f13 ::set)
(spec/def ::f14 ::map)
(spec/def ::f15 ::list)

(spec/def :unq/table (spec/keys :req-un [::pk1 ::pk2 ::pk3 ::f1 ::f2 ::f3 ::f4 ::f5 ::f6 ::f7 ::f8
                                         ::f9 ::f10 ::f11 ::f12 ::f13 ::f14 ::f15]))

(defn create-keyspace [keyspace]
  (hayt/create-keyspace (keyword keyspace)
                        (hayt/if-exists false)
                        (hayt/with {:replication {"class" "SimpleStrategy" "replication_factor" "1"}})))

(defn create-table [name]
  (hayt/create-table (keyword name)
                     (hayt/if-exists false)
                     (hayt/column-definitions {:primary-key [[:pk1 :pk2], :pk3]
                                               :pk1 :bigint
                                               :pk2 :bigint
                                               :pk3 :timestamp
                                               :f1  :blob
                                               :f2  :text
                                               :f3  :float
                                               :f4  :double
                                               :f5  :ascii
                                               :f6  :boolean
                                               :f7  :decimal
                                               :f8  :inet
                                               :f9  :timeuuid
                                               :f10 :uuid
                                               :f11 :varchar
                                               :f12 :varint
                                               :f13 (hayt/frozen (hayt/set-type :text))
                                               :f14 (hayt/frozen (hayt/map-type :text :text))
                                               :f15 (hayt/frozen (hayt/list-type :text))})
                     (hayt/with {:compression {"sstable_compression" "LZ4Compressor"}
                                 :compaction  {"class" "LeveledCompactionStrategy"}})))

(defmulti insert-table-data (fn [table data] (type data)))

(defmethod insert-table-data clojure.lang.PersistentHashMap [table data]
  (->> data (hayt/values) (hayt/insert (keyword table))))

(defmethod insert-table-data clojure.lang.PersistentVector [table data]
  (vector (map #(insert-table-data table %) data)))

(defn drop-keyspace [keyspace]
  (hayt/drop-keyspace keyspace))

(defn random-table-data
  ([]
   (gen/generate (spec/gen :unq/table)))
  ([rows]
   (into [] (repeatedly rows random-table-data))))

(defn connect []
  (let [cfg      (config/checked-environment)
        tables   (:db-tables cfg)
        keyspace (:db-keyspace cfg)
        cluster  (-> cfg config/alia alia/cluster)
        session  (alia/connect cluster)]
    {:cfg      cfg
     :tables   tables
     :keyspace keyspace
     :cluster  cluster
     :session  session}))

(defn init []
  (let [db       (connect)
        cfg      (:cfg db)
        tables   (:tables db)
        cluster  (:cluster db)
        keyspace (:keyspace db)
        session  (:session db)]
    (log/debug "nemo db setup started")

    (try
      (log/debugf "creating keyspace '%s' if needed" keyspace)
      (alia/execute session (create-keyspace keyspace))
      (catch java.lang.RuntimeException cause
        (log/errorf "could not create keyspace '%s'" keyspace)
        :fail))
    (try
      (log/debugf "using keyspace '%s'" keyspace)
      (alia/execute session (hayt/use-keyspace keyspace))

      (log/debugf "tables: '%s'" tables)
      (doseq [t tables]
        (do
          (log/debugf (format "creating table:'%s'" t))
          (alia/execute session (create-table t))
          (doseq [_ (range DB_ROW_COUNT)]
            (alia/execute session (insert-table-data t (random-table-data))))))
      :done
      (catch java.lang.RuntimeException cause
        (log/errorf "could not create nemo tables")
        (throw (ex-info (.getMessage cause) {} cause)))
      (finally
        (alia/shutdown session)
        (alia/shutdown cluster)
        (log/debugf "alia connection shut down")))))

;; there's real danger having nuke keyspace run against a value not hardcoded.
;; make keyspace configurable if it's really a problem.
(defn nuke-test-keyspace []
  (let [db       (connect)
        cluster  (:cluster db)
        session  (:session db)
        keyspace "nemo_test"]
    (try
      (do
        (log/debugf "dropping keyspace: '%s'" keyspace)
        (alia/execute session (drop-keyspace keyspace)))
      :done
      (catch Exception cause
        (log/warnf "could not drop keyspace: '%s'" keyspace)
        (log/debugf (Throwable->map cause)))
      (finally
        (alia/shutdown session)
        (alia/shutdown cluster)
        (log/debugf "alia connection shut down")))))
