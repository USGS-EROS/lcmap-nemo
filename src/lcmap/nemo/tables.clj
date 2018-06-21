(ns lcmap.nemo.tables
  (:require [clj-uuid :as uuid]
            [clojure.set :as s]
            [clojure.tools.logging :as log]
            [lcmap.nemo.config :as config]
            [lcmap.nemo.db :as db]
            [lcmap.nemo.util :as util]
            [mount.core :as mount]
            [qbits.alia :as alia]
            [qbits.hayt :as hayt]))
;;   Exploits Cassandra system_schema.column to enable Compojure HTTP resource creation
;;
;;
;;   select * from system_schema.columns where keyspace_name='local'
;;   keyspace_name  | table_name     | column_name | clustering_order | column_name_bytes  | kind          | position | type
;;   ----------------+----------------+-------------+------------------+--------------------+---------------+----------+-----------
;;   local |     table1 |    acquired |              asc | 0x6163717569726564 |    clustering |        0 | timestamp
;;   local |     table1 |        data |             none |         0x64617461 |       regular |       -1 |      blob
;;   local |     table1 |        hash |             none |         0x68617368 |       regular |       -1 |      text
;;   local |     table1 |      source |             none |     0x736f75726365 |       regular |       -1 |      text
;;   local |     table1 |           x |             none |               0x78 | partition_key |        0 |    bigint
;;   local |     table1 |           y |             none |               0x79 | partition_key |        1 |    bigint
;;   local |     table2 |    acquired |              asc | 0x6163717569726564 |    clustering |        0 | timestamp
;;   local |     table2 |        data |             none |         0x64617461 |       regular |       -1 |      blob
;;   local |     table2 |        hash |             none |         0x68617368 |       regular |       -1 |      text
;;   local |     table2 |      source |             none |     0x736f75726365 |       regular |       -1 |      text


(defn where
  "Construct where clause for selecting data from tables"
  [partition-keys params]
  (let [query-params (into [] (select-keys params partition-keys))]
    (vec (map (fn [x] (into [=] x)) query-params))))

(defn select-keyspace
  "Query for system_schema.columns entries in the configured keyspace"
  ([e]
   {:select :columns
    :columns :*
    :where [[= :keyspace_name (:db-keyspace e)]]})
  ([]
  (-> (config/checked-environment) select-keyspace)))
  
(defn select-data
  "Query to select data partitions"
  [table partition-keys params]
  {:select table
   :columns :*
   :where (where partition-keys params)})

(defn select-partition-keys
  "Query to select all partition key values"
  [table partition-keys]
  {:select  table
   :columns (apply hayt/distinct* partition-keys)})

(defn restrict
  "Restrict system-schema-columns to supplied table names only"
  [system-schema-columns tables]
  (filter #(some #{(:table_name %)} tables) system-schema-columns))

(mount/defstate system-schema-columns
  :start (do
           (log/debugf "loading system_schema.columns")
           (-> (alia/execute db/system-schema-session (select-keyspace))
               (restrict (-> (config/checked-environment) :db-tables)))))

(mount/defstate system-schema-map
  "Creates a nested map of table name keywords to map of column keywords"
  :start (do
           (log/debugf "creating system_schema map")
           (let [->maps (-> system-schema-columns (into {}))]
             (reduce (fn [a v]
                       (assoc-in a [(keyword (:table_name v))
                                    (keyword (:column_name v))] v)) {} ->maps))))

(defn coerce-catch
  [value to-type exception]
  (let [msg (format "%s cannot be coerced to %s" value to-type)]
    (log/warn msg)
    (log/debug exception)
    (throw (IllegalArgumentException. msg))))
  
(defmulti coerce
  "Multimethod to coerce strings to types expected by Cassandra"
  (fn [table column _]
    (keyword (get-in system-schema-map [table column :type]))))
  
(defmethod coerce :bigint
  [_ _ value]
  (try
    (-> value util/numberize long)
    (catch Exception e
      (coerce-catch value :bigint e))))

(defmethod coerce :int
  [_ _ value]
  (try
    (-> value util/numberize int)
    (catch Exception e
      (coerce-catch value :int e))))

(defmethod coerce :smallint
  [_ _ value]
  (try
    (-> value util/numberize short)
    (catch Exception e
      (coerce-catch value :smallint e))))

(defmethod coerce :tinyint
  [_ _ value]
  (try
    (-> value util/numberize byte)
    (catch Exception e
      (coerce-catch value :tinyint e))))

(defmethod coerce :timestamp
  [_ _ value]
  (try
    (-> value util/numberize int)
    (catch Exception e
      (coerce-catch value :timestamp e))))

(defmethod coerce :double
  [_ _ value]
  (try
    (-> value util/numberize double)
    (catch Exception e
      (coerce-catch value :double e))))

(defmethod coerce :float
  [_ _ value]
  (try
    (-> value util/numberize float)
    (catch Exception e
      (coerce-catch value :float e))))

(defmethod coerce :ascii
  [_ _ value]
  (try
    (if (-> value str util/ascii?)
      (str value)
      (coerce-catch value
                    :ascii
                    (Exception. (format "%s is not ascii" value))))))

(defmethod coerce :varint
  [_ _ value]
  (try
    (-> value util/numberize int)
    (catch Exception e
      (coerce-catch value :varint e))))

(defmethod coerce :decimal
  [_ _ value]
  (try
    (-> value util/numberize double)
    (catch Exception e
      (coerce-catch value :decimal e))))

(defmethod coerce :inet
  [_ _ value]
  (try
    (if (-> value util/ipv4?)
      (str value)
      (throw (Exception.)))
      (catch Exception e
        (coerce-catch value :inet e))))

(defmethod coerce :boolean
  [_ _ ^java.lang.String value]
  (try
    (Boolean/valueOf (str value))
    (catch Exception e
      (coerce-catch value :boolean e))))

(defmethod coerce :timeuuid
  [_ _ value]
  (if (uuid/uuid-string? value)
    (uuid/as-uuid value)
    (coerce-catch value
                  :timeuuid
                  (Exception. (format "%s is not a uuid" value)))))

(defmethod coerce :uuid
  [_ _ value]
  (if (uuid/uuid-string? value)
    (uuid/as-uuid value)
    (coerce-catch value
                  :uuid
                  (Exception. (format "%s is not a uuid" value)))))


(defmethod coerce :default
  [_ _ value]
  (str value))

(defn available
  "Return table ids and names"
  ([schema-map]
   (reduce (fn [a v] (assoc a v (name v))) {} (keys schema-map)))
  ([]
   (available system-schema-map)))

(defn partition-keys
  "Return all partition keys from table hashmap"
  ([schema-map table]
   (into [] (map #(keyword (:column_name %))
                 (filter #(= (:kind %) "partition_key") (-> schema-map table vals)))))
  ([table]
   (partition-keys system-schema-map table)))

(defn partition-keys?
  "Do all partition keys exist in params?"
  ([table params schema-map]
   (let [params (into #{} (keys params))
         pkeys  (into #{} (partition-keys schema-map table))]
     (= (s/intersection params pkeys) pkeys)))
  ([table params]
   (partition-keys? table params system-schema-map)))

(defn coerce-parameters
  "Coerce a map of parameters to proper Cassandra types"
  [table parameters]
  (reduce-kv (fn [m k v] (assoc m k (coerce table k v))) {} parameters))

(defn query-data
  "Select a partition of data from a Cassandra table"
  [table-name parameters]
  (let [table  (keyword table-name)
        pks    (partition-keys table)
        params (coerce-parameters table parameters)
        query  (select-data table pks params)]
    (alia/execute db/default-session query)))

(defn query-partition-keys
  "Select all partition keys/values from a Cassandra table"
  [table-name]
  (let [table (keyword table-name)
        pks   (partition-keys table)
        query (select-partition-keys table pks)]
    (alia/execute db/default-session query)))
