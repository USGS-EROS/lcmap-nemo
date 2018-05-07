(ns lcmap.nemo.table-test
  (:require [clojure.test :refer :all]
            [lcmap.nemo.fixtures :as fixtures]
            [lcmap.nemo.tables :as tables]))

(use-fixtures :once fixtures/all-fixtures)

(deftest where-test
  (testing "testing tables/where"
    (let [partition-keys [:a :b]
          parameters     {:a 1 :b 2}
          where-clause   (tables/where partition-keys  parameters)
          expected       [[= :a 1] [= :b 2]]]
      (is (= where-clause expected)))))

(deftest select-keyspace-test
  (testing "testing select-keyspace"
    (let [keyspace "test_keyspace"
          env      {:db-keyspace keyspace}
          expected {:select :columns
                    :columns :*
                    :where [[= :keyspace_name keyspace]]}]
      (is (= (tables/select-keyspace env) expected)))))
      
(deftest select-data-test
  (testing "testing tables/select-data"
    (let [partition-keys [:a :b]
          parameters     {:a 1 :b 2}
          query          (tables/select-data :table1 partition-keys parameters)
          expected       {:select :table1
                          :columns :*
                          :where [[= :a 1] [= :b 2]]}]
      (is (= query expected)))))

(deftest select-partition-keys-test
  (testing "testing tables/select-partition-keys"
    (let [table    :test-table
          pks      [:a :b]
          expected {:select table
                    :columns pks}]
      (is (= (tables/select-partition-keys table pks) expected))))
)
(deftest restrict-test
  (testing "testing tables/restrict"
    (let [ssc      [{:table_name :a}{:table_name :b}{:table_name :c}]
          tables   [:a :c]
          expected [{:table_name :a}{:table_name :c}]]
      (is (= (tables/restrict ssc tables) expected)))))

(deftest coerce-test
  (testing "testing tables/coerce"
    (is (= (tables/coerce :one :pk1 "123") 123))
    (is (= (tables/coerce :one :pk1 "123.0") 123))))

