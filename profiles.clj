{:dev  {:env {:db-host "localhost"
              :db-port 9042
              :db-keyspace "nemo_dev"
              :db-user "cassandra"
              :db-pass "cassandra"
              :db-tables "one,two"
              :http-port 5757}}
 :test {:env {:db-host "localhost"
              :db-port 9042
              :db-keyspace "nemo_test"
              :db-user "cassandra"
              :db-pass "cassandra"
              :db-tables "one,two"
              :http-port 5758}}}
