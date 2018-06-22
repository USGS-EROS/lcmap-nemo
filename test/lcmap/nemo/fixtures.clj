(ns lcmap.nemo.fixtures
  (:require [clojure.test :refer :all]
            [clojure.edn :as edn]
            [clojure.tools.logging :as log]
            [mount.core :as mount]
            [lcmap.nemo.http]
            [lcmap.nemo.db]
            [lcmap.nemo.setup :as setup]))

(defn schema-fixture [f]
  (log/tracef "schema-fixture up")
  (setup/init)
  (f)
  (setup/nuke-test-keyspace)
  (log/tracef "schema-fixture down"))

(defn mount-fixture [f]
  (log/tracef "mount-fixture up")
  (mount/start)
  (f)
  (mount/stop)
  (log/tracef "mount-fixture down"))

(def all-fixtures (join-fixtures [schema-fixture mount-fixture]))
