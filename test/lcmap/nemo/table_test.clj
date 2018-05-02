(ns lcmap.nemo.table-test
  (:require [clojure.test :refer :all]
            [lcmap.nemo.tables :as tables]))

(deftest table-test
  (testing "testing tables/table"
    (let [data [{:table_name "one"} {:table_name "two"} {:table_name "three"}]]
      (is (= "one"   (:table_name (first (tables/table data "one")))))
      (is (= "two"   (:table_name (first (tables/table data "two")))))
      (is (= "three" (:table_name (first (tables/table data "three")))))
      (is (= 1 (count (tables/table data "one"))))
      (is (= 1 (count (tables/table data "two"))))
      (is (= 1 (count (tables/table data "three")))))))

(deftest partition-keys-test
  (testing "testing tables/partition-keys"
    (is (= 1 (count (tables/partition-keys [{:kind "partition_key"}]))))
    (is (= 0 (count (tables/partition-keys [{:kind "something_not"}]))))))

(deftest where-test
  (testing "testing tables/where"
    (let [partition-keys [{:column_name "a"} {:column_name "b"}]
          parameters     {:a 1 :b 2}
          where-clause   (tables/where partition-keys  parameters)
          expected       {:where [[= :a 1] [= :b 2]]}]
      (is (= where-clause expected)))))

(deftest select-data-test
  (testing "testing tables/select-data"
    (let [partition-keys [{:column_name "a"} {:column_name "b"}]
          parameters     {:a 1 :b 2}
          query          (tables/select-data "table1" partition-keys parameters)
          expected       {:select :table1
                          :columns :*
                          :where [[= :a 1] [= :b 2]]}]
      (is (= query expected)))))
