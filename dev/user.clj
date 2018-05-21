(ns user
  "Developer namespace."
  (:require [clojure.edn :as edn]
            [clojure.stacktrace :as stacktrace]
            [lcmap.nemo.jmx]
            [lcmap.nemo.http]
            [lcmap.nemo.db :as db]
            [lcmap.nemo.util :as util]
            [lcmap.nemo.config :as config]
            [mount.core :as mount])
  (:use     [clojure.repl]
            [clojure.tools.namespace.repl :only (refresh)])
  (:import [org.joda.time DateTime]))


;;
;; Starting a REPL will automatically setup and start the system.
;;

(try
  (print "starting mount components...")
  (mount/start)
  (print "...ready!")
  (catch RuntimeException ex
    (print "There was a problem automatically setting up and running nemo.")
    (stacktrace/print-cause-trace ex)))
