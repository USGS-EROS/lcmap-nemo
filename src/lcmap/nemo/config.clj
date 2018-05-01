(ns lcmap.nemo.config
  "Configuration related functions.

  Values are obtained from ENV variables and/or profiles.clj. These are:

  | ENV                     | Description                             |
  | --------------          | ----------------------------------------|
  | `HTTP_PORT`             | HTTP listener port                      |
  | `DB_HOST`               | Cassandra node (just one)               |
  | `DB_USER`               | Cassandra username                      |
  | `DB_PASS`               | Cassandra password                      |
  | `DB_PORT`               | Cassandra cluster port                  |
  | `DB_KEYSPACE`           | Cassandra keyspace name                 |
  | `DB_TABLES`             | Cassandra tables to expose as resources |
  ' `LOAD_BALANCING_POLICY` | Cassandra loadbalancing policy          |
  "
  (:require [clojure.pprint :refer [*print-pretty*]]
            [clojure.tools.logging :as log]
            [environ.core :refer [env]]
            [qbits.alia.policy.load-balancing :as lb]))


(set! *warn-on-reflection* true)

(def load-balancing-policies {:round-robin-policy lb/round-robin-policy})


(defn string->vector
  "Split string on comma into vector"
  [s]
  (if (nil? s)
    nil
    (->> (clojure.string/split s #"[,]+")
         (map clojure.string/trim)
         (vec))))


(defn nil-kv?
  "presents a sequence of [:key value] in terms of {:key (nil? value)}"
  [kv]
  (reduce-kv (fn [m k v] (assoc m k (nil? v))) {} kv))


(defn environment
  []
  {:db-host               (-> env :db-host)
   :db-user               (-> env :db-user)
   :db-pass               (-> env :db-pass)
   :db-port               (-> env :db-port)
   :db-keyspace           (-> env :db-keyspace)
   :db-tables             (-> env :db-tables string->vector)
   :http-port             (-> env :http-port)
   :load-balancing-policy (:round-robin-policy load-balancing-policies)
   :consistency           :quorum})


(defn ok?
  [e]
  (let [missing (keys (filter #(true? (second %)) (nil-kv? e)))
        message (format "Missing environment variables: %s" missing)]
    (if (empty? missing)
      (merge e {:ok true})
      (merge e {:ok false :message message}))))


(defn with-except
  [e]
  (if (:ok e)
    e
    (do (log/fatal (:message e))
        (-> e :message str Exception. throw))))


(defn checked-environment
  []
  (-> (environment) ok? with-except))


(defn alia
  [e]
  {:contact-points        (-> e :db-host string->vector)
   :credentials           {:user (:db-user e) :password (:db-pass e)}
   :query-options         {:consistency (:consistency e)}
   :load-balancing-policy (:load-balancing-policy e)})
