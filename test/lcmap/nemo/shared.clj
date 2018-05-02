(ns lcmap.nemo.shared
  (:require [clojure.tools.logging :as log]
            [mount.core :as mount]
            [cheshire.core :as json]
            [org.httpkit.client :as http]
            [lcmap.nemo.config :as config]))

(defn app-url
  "Produce a URL for path to locally running HTTP server. Used for
   integration testing."
  [path]
  (let [port (-> (config/checked-environment) :http-port Integer/parseInt)]
    (str (java.net.URL. (java.net.URL. "http" "localhost" port "/") path))))

(defn try-decode
  "Attempt to decode body as JSON, but leave it be otherwise."
  [body]
  (try
    (json/decode body keyword)
    (catch com.fasterxml.jackson.core.JsonParseException ex
      body)
    (catch java.lang.RuntimeException ex
      body)))

(defn go-fish
  "Helper function for integration tests."
  [opts]
  (let [port ((config/checked-environment) :http-port)
        base (format "http://localhost:%s/" port)]
    (-> opts
        (update-in [:url] (fn [url] (app-url url)))
        (assoc-in  [:headers "Content-Type"] "application/json")
        (update-in [:body] json/encode)
        (http/request)
        (deref)
        (update-in [:body] try-decode))))
