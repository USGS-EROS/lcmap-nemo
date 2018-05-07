(ns lcmap.nemo.table-test
  (:require [clojure.test :refer :all]
            [lcmap.nemo.tables :as tables]))

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

