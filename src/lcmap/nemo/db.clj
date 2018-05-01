(ns lcmap.nemo.db
  "Cassandra connections and helper functions."
  (:require [clojure.tools.logging :as log]
            [mount.core :refer [defstate] :as mount]
            [qbits.alia :as alia]
            [qbits.alia.codec.default :as default-codec]
            [lcmap.nemo.config :as config])
  (:import org.joda.time.DateTime))

(set! *warn-on-reflection* true)

;; ## Overview
;;
;; This namespace defines the state and behavior of connections to
;; the Cassandra cluster. It relies on values `lcmap.nemo.config`
;; for specific values such as hostnames and credentials.
;;


;; ## Joda Support
;;
;; Enabling encoding of Joda types provides a degree of convenience.
;;

(extend-protocol default-codec/Encoder
  org.joda.time.DateTime (encode [x] (.toDate x)))


;; ## Declarations
;;

(declare db-cluster default-session system-schema-session)


;; ## db-cluster
;;
;; After start `db-cluster` refers to com.datastax.driver.core.Cluster,
;; an object that maintains general information about the cluster; use
;; db-session to execute queries.
;;

(defn db-cluster-start
  "Open cluster connection."
  []
  (let [db-cfg (config/alia (config/checked-environment))]
    (log/debugf "start db cluster connection")
    (alia/cluster db-cfg)))


(defn db-cluster-stop
  "Shutdown cluster connection."
  []
  (log/debugf "stop db cluster connection")
  (alia/shutdown db-cluster))


(defstate db-cluster
  :start (db-cluster-start)
  :stop  (db-cluster-stop))


;; ## database sessions
;;
;; After start `default-session` and `system-schema-session` refer
;; to a com.datastax.driver.core.SessionManager
;; object that can be used to execute queries.
;;
;; _WARNING: Do not use the same session for multiple keyspaces, functions
;; that rely on this state expect a stable keyspace name!_
;;

(defn default-session-start
  "Create session that uses the default keyspace."
  []
  (log/debugf "start default db session")
  (alia/connect db-cluster (:db-keyspace (config/checked-environment))))


(defn default-session-stop
  "Close Cassandra session."
  []
  (log/debugf "stop default db session")
  (alia/shutdown default-session))


(defstate default-session
  :start (default-session-start)
  :stop  (default-session-stop))


(defn system-schema-session-start
  "Create session that uses the system_schema keyspace."
  []
  (log/debugf "start system_schema db session")
  (alia/connect db-cluster "system_schema"))


(defn system-schema-session-stop
  "Close Cassandra system_schema session."
  []
  (log/debugf "stop system_schema db session")
  (alia/shutdown system-schema-session))


(defstate system-schema-session
  :start (system-schema-session-start)
  :stop  (system-schema-session-stop))
