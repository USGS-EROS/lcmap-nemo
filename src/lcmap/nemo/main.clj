(ns lcmap.nemo.main
  "Start-up related functions and entry-point."
  (:require [clojure.tools.logging :as log]
            [mount.core]
            [lcmap.nemo.config]
            [lcmap.nemo.db]
            [lcmap.nemo.http]
            [lcmap.nemo.jmx]
            [lcmap.nemo.util])
  (:gen-class))

(defn -main
  "This is the entry-point used to start a Nemo server.

   Arguments are ignored, use ENV variables or profiles.clj
   to configure the app."
  [& args]
  (try
    
    ;; A shutdown hook gives us a way to cleanly stop mount
    ;; states.
    (log/debug "nemo add shutdown hook")
    (lcmap.nemo.util/add-shutdown-hook)
    ;; Remember, only mount states defined that are defined
    ;; in required namepsaces are started.
    (log/debug "nemo start")
    (mount.core/start)
    (catch RuntimeException ex
      (log/errorf "error starting nemo: %s" (.getMessage ex))
      (log/fatalf "nemo died during startup")
      (System/exit 1))))
