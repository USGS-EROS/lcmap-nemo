(ns lcmap.nemo.http
  "Handlers for HTTP requests."
  (:require [cheshire.core :as json]
            [cheshire.generate :as json-gen :refer [add-encoder]]
            [clojure.tools.logging :as log]
            [clojure.stacktrace :as stacktrace]
            [compojure.core :as compojure]
            [lcmap.nemo.config :as config]
            [lcmap.nemo.tables :as tables]
            [mount.core :as mount]
            [org.httpkit.server :as server]
            [ring.middleware.json :as ring-json]
            [ring.middleware.defaults :as ring-defaults]
            [ring.middleware.keyword-params :as ring-keyword-params]
            [ring.middleware.cors :as ring-cors])

  (:import [org.joda.time DateTime]
           [org.apache.commons.codec.binary Base64]
           [com.fasterxml.jackson.core.JsonGenerator]))

;; # Overview
;;
;; This namespace provides functions that produce responses
;; to requests for various resources. It also defines routes,
;; middleware, encoders, and an HTTP server state.
;;


;; # Responders
;;
;; These functions are defined as such to keep routes concise.
;; Each produces a Ring-style response map. You may notice some
;; logic that 'renames' parameters; this was done to maintain
;; compatability with previous consumers of a similar REST API.
;;

;; In order to avoid duplication, resources that provide the same
;; behavior without changing names have been avoided.
;;

(defn get-tables
  "Build response for tables URL."
  [req]
  {:status 200 :body {"tables" (vals (tables/available))}})

(defmulti get-partition
  "Dispatch to get-partition handlers"
  (fn [table-name request]
    (let [table  (keyword table-name)
          params (:params request)]
    (cond
      (nil? (table (tables/available)))           :missing-table
      (empty? params)                             :partition-keys
      (not (tables/partition-keys? table params)) :missing-params
      :else                                       :partition-data))))

(defmethod get-partition :missing-table
  [table-name request]
  (log/debugf (format "GET %s (not found)" table-name))
  {:status 404 :body [(str table-name " not found")]})

(defmethod get-partition :partition-keys
  [table-name _]
  (log/debugf (format "GET %s" table-name))
  (let [results (tables/query-partition-keys table-name)]
    {:status 200 :body results}))

(defmethod get-partition :missing-params
  [table-name request]
  (let [table  (keyword table-name)
        params (into [] (map #(name %) (tables/partition-keys table)))]
  (log/debugf (format "GET %s " table-name " (missing parameters)"))
  {:status 400 :body {"required_parameters" params}}))
                      
(defmethod get-partition :partition-data
  [table-name request]
  (log/debugf (format "GET %s %s" table-name (:params request)))
  (let [results (tables/query-data table-name (:params request))]
    {:status 200 :body results}))

(defn healthy
  "Handler for checking application health."
  [request]
  (log/debug "GET health")
  {:status 200 :body {:healthy true}})

;; ## Routes
;;
;; As mentioned prior, the route definition does nothing aside
;; from invoke the corresponding function. This keeps routes
;; concise (and readable).
;;

(compojure/defroutes routes
  (compojure/context "/" request
    (compojure/GET "/" [] (get-tables request))
    (compojure/GET "/healthy" [] (healthy request))
    (compojure/GET "/:table" [table] (get-partition table request))))

;; ## Middleware
;;
;; The only custom middleware provided by Nemo is an exception
;; handler that produces a generic error message.
;;

(defn cause-messages
  "Produce a list of messages for a Throwable's cause stack trace."
  [throwable]
  (->> throwable
       (iterate (fn [^Throwable t] (.getCause t)))
       (take-while some?)
       (map (fn [^Throwable t] (.getMessage t)))
       (into [])))

(defn wrap-exception-handling
  "Catch otherwise unhandled exceptions."
  [handler]
  (fn [request]
    (try
      (handler request)
      (catch java.lang.RuntimeException ex
        (let [messages (cause-messages ex)]
          (log/errorf "exception: %s" messages)
          {:status 500 :body (json/encode {:errors messages})})))))

;; ## Handler
;;
;; This handler combines all routes and middleware. In addition to
;; our own middleware, we include functions that:
;;
;; - convert params map keys into keywords
;; - convert request and and response bodies to JSON
;; - add CORS headers to responses
;;

(def app
  (-> routes
      (ring-json/wrap-json-body {:keywords? true})
      (ring-json/wrap-json-response)
      (ring-defaults/wrap-defaults ring-defaults/api-defaults)
      (ring-keyword-params/wrap-keyword-params)
      (wrap-exception-handling)
      (ring-cors/wrap-cors #".*")))

;; ## Encoders
;;
;; These functions simplify the conversion of values that do
;; not have a default way of producing a serialized string.
;;

(defn iso8601-encoder
  "Transform a Joda DateTime object into an ISO8601 string."
  [^org.joda.time.DateTime date-time ^com.fasterxml.jackson.core.JsonGenerator generator]
  (log/trace "encoding DateTime to ISO8601")
  (.writeString generator (str date-time)))

(defn base64-encoder
  "Base64 encode a byte-buffer, usually raster data from Cassandra."
  [^java.nio.HeapByteBuffer buffer ^com.fasterxml.jackson.core.JsonGenerator generator]
  (log/trace "encoding HeapByteBuffer")
  (let [size (- (.limit buffer) (.position buffer))
        copy (byte-array size)]
    (.get buffer copy)
    (.writeString generator (Base64/encodeBase64String copy))))

(defn inet-encoder
  "Represent java.net.Inet4Address as java.lang.String"
  [^java.net.InetAddress address ^com.fasterxml.jackson.core.JsonGenerator generator]
  (log/trace "encoding inet address")
  (.writeString generator (. address getHostName)))

(mount/defstate json-encoders
  :start (do
           (json-gen/add-encoder java.net.InetAddress inet-encoder)
           (json-gen/add-encoder org.joda.time.DateTime iso8601-encoder)
           (json-gen/add-encoder java.nio.HeapByteBuffer base64-encoder)))

;; ## HTTP Server
;;
;; This state starts a web server that uses the `app` handler
;; to process requests.
;;

(declare http-server)

(defn http-start
  "Start the http server"
  []
  (let [port (-> (config/checked-environment) :http-port Integer/parseInt)]
    (log/debugf "start http server on port %s" port)
    (server/run-server #'app {:port port})))

(defn http-stop
  "Stop the http server"
  []
  (log/debug "stop http server")
  (http-server))

(mount/defstate http-server
  :start (http-start)
  :stop  (http-stop))
