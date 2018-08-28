(ns user
  "Developer namespace."
  (:require [clojure.edn :as edn]
            [clojure.stacktrace :as stacktrace]
            [lcmap.nemo.config :as config]
            [lcmap.nemo.db :as db]
            [lcmap.nemo.http :as http]
            [lcmap.nemo.jmx :as jmx]
            [lcmap.nemo.setup :as setup]
            [lcmap.nemo.util :as util]
            [mount.core :as mount])
  (:use     [clojure.repl]
            [clojure.tools.namespace.repl :only (refresh)])
  (:import [org.joda.time DateTime]))


(defn init
  []
  (setup/init))

(defn start
  []
  (try
  (print "starting mount components...")
  (mount/start)
  (print "...ready!")
  (catch RuntimeException ex
    (print "There was a problem automatically setting up and running nemo.")
    (stacktrace/print-cause-trace ex))))

(defn stop
  []
  (try
    (print "stopping mount components...")
    (mount/stop)
    (print "... stopped!")
    (catch RuntimeException ex
      (print "There was a problem automatically tearing down nemo.")
      (stacktrace/print-cause-trace ex))))
