(defproject nemo "1.0.0-SNAPSHOT"
  :description "Cassandra table HTTP Adapter"
  :url "http://github.com/usgs-eros/lcmap-nemo"
  :license {:name "Unlicense"
            :url ""}
  :dependencies [[camel-snake-kebab "0.4.0"]
                 [cc.qbits/alia-all "4.0.3"]
                 [cc.qbits/hayt "4.0.0"]
                 [cc.qbits/alia-joda-time "4.0.2"]
                 [cheshire "5.8.0"]
                 [clojurewerkz/buffy "1.1.0"]
                 [compojure "1.6.0"]
                 [danlentz/clj-uuid "0.1.7"]
                 [digest "1.4.6"]
                 [environ "1.1.0"]
                 [http-kit "2.2.0"]
                 [jumblerg/ring.middleware.cors "1.0.1"]
                 [org.clojure/clojure "1.9.0"]
                 [org.clojure/test.check "0.9.0"]
                 [org.clojure/tools.logging "0.4.0"]
                 [org.slf4j/slf4j-log4j12 "1.7.21"]
                 [metrics-clojure "2.9.0"]
                 [metrics-clojure-health "2.9.0"]
                 [metrics-clojure-jvm "2.9.0"]
                 [metrics-clojure-ring "2.9.0"]
                 [mount "0.1.11"]
                 [ring "1.6.2"]
                 [ring/ring-defaults "0.3.1"]
                 [ring/ring-json "0.4.0"]]
  ;; Emit warnings on all reflection calls.
  :global-vars {*warn-on-reflection* true}
  :plugins [[lein-environ "1.1.0"]]
  :profiles {:dev     {:resource-paths ["dev" "test"]}
             :repl    {:resource-paths ["dev" "test"]
                       :dependencies [[cider/cider-nrepl "0.15.1"]
                                      [org.slf4j/slf4j-log4j12 "1.7.21"]]
                       :env {:db-host "localhost"
                             :db-port 9042
                             :db-keyspace "nemo_dev"
                             :db-user "cassandra"
                             :db-pass "cassandra"
                             :db-tables "one,two"
                             :http-port 5757}}
             :test    {:env {:db-host "localhost"
                             :db-port 9042
                             :db-keyspace "nemo_test"
                             :db-user "cassandra"
                             :db-pass "cassandra"
                             :db-tables "one,two"
                             :http-port 5758}
                       :resource-paths ["test" "test/resources"]}
             :uberjar {:omit-source true
                       :aot :all}}
  :jvm-opts ["-server"]
  :main lcmap.nemo.main
  :repl-options {:init-ns user})
