(ns lcmap.nemo.tables
  (:require [clojure.tools.logging :as log]
            [lcmap.nemo.config :as config]
            [lcmap.nemo.db :as db]
            [lcmap.nemo.util :as util]
            [mount.core :as mount]
            [qbits.alia :as alia]
            [qbits.hayt :as hayt]))

;; Combines configuration information with Cassandra system_schema.column to create Compojure HTTP resources
;;
;;   cfg.keyspace
;;   cfg.tables
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


(defn where [partition-keys params]
  (let [query-keys   (map (fn [pk] (-> (:column_name pk) keyword)) partition-keys)
        query-params (into [] (select-keys params query-keys))]
    (vec (map (fn [x] (into [=] x)) query-params))))


(defn select-keyspace []
  {:select :columns
   :columns :*
   :where [[= :keyspace_name (:db-keyspace (config/checked-environment))]]})


(defn select-data [table-name partition-keys params]
  {:select (keyword table-name)
   :columns :*
   :where (where partition-keys params)})


(defn select-partition-keys [table-name partition-keys]
  {:select  (keyword table-name)
   :columns (map #(:column_name %) partition-keys)})


(mount/defstate system-schema-columns
  :start (do
           (log/debugf "loading system_schema.columns")
           (alia/execute db/system-schema-session (select-keyspace))))


(defn types-id [table-name column-name]
  (keyword (str table-name "/" column-name)))


(defn types-entry [row]
  {(types-id (:table_name row) (:column_name row)) (keyword (:type row))})


(mount/defstate system-schema-types
  "Namespace qualified keyword :table/column to column type"
  :start (do
           (log/debugf "creating system_schema map")
           (apply merge (map types-entry system-schema-columns))))


(defmulti coerce
  (fn [table-name column-name]
    ((types-id table-name column-name) system-schema-types)))


(defmethod coerce :bigint [x]
  (-> x util/numberize long))


(defmethod coerce :double [x]
  (-> x util/numberize double))


(defmethod coerce :float [x]
  (-> x util/numberize float))


(defmethod coerce :varint [x]
  (-> x util/numberize int))


(defmethod coerce :decimal [x]
  (-> x util/numberize double))


(defmethod coerce :boolean [x]
  (boolean x))


(defmethod coerce :default [x]
  (str x))


(defn table [system-schema-columns table-name]
  (filter #(= (:table_name %) table-name) system-schema-columns))


(defn partition-keys [system-schema-columns]
  (filter #(= (:kind %) "partition_key") system-schema-columns))


(defn query-data [table-name params]
  (let [pks   (-> system-schema-columns (table table-name) partition-keys)
        query (select-data table-name pks params)]
    (alia/execute db/default-session query)))


(defn query-partition-keys [table-name]
  (let [pks (-> system-schema-columns (table table-name) partition-keys)
        query (select-partition-keys table-name pks)]
    (alia/execute db/default-session query)))
