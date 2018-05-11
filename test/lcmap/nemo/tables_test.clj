(ns lcmap.nemo.tables-test
  (:require [clj-uuid :as uuid]
            [clojure.test :refer :all]
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
          expected {:select table,
                    :columns #qbits.hayt.cql.CQLRaw{:value "DISTINCT a, b"}}]
      (is (= (tables/select-partition-keys table pks) expected)))))

(deftest restrict-test
  (testing "testing tables/restrict"
    (let [ssc      [{:table_name :a}{:table_name :b}{:table_name :c}]
          tables   [:a :c]
          expected [{:table_name :a}{:table_name :c}]]
      (is (= (tables/restrict ssc tables) expected)))))

(deftest coerce-test
  (testing "testing tables/coerce"

    ;; test bigints
    (is (= (tables/coerce :one :pk1 "123") 123))
    (is (= (tables/coerce :one :pk1 "123.0") 123))
    (is (thrown? Exception (tables/coerce :one :pk1 "not-a-number")))

    ;; test timestamps
    (is (= (tables/coerce :one :pk3 123456) 123456))
    (is (= (tables/coerce :one :pk3 "123") 123))
    (is (= (tables/coerce :one :pk3 "123.0") 123)) 
    (is (thrown? Exception (tables/coerce :one :pk3 "not-a-number")))

    ;; test blobs
    (is (= (type (tables/coerce :one :f1 (byte-array [1 2 3 4]))) java.lang.String))

    ;; test text
    (is (= (tables/coerce :one :f2 123) "123"))

    ;; test float
    (is (= (tables/coerce :one :f3 123) 123.0))
    (is (= (tables/coerce :one :f3 "123") 123.0))
    (is (= (type (tables/coerce :one :f3 "123")) java.lang.Float))
    (is (thrown? Exception (tables/coerce :one :f3 "not-a-number")))

    ;; test double
    (is (= (tables/coerce :one :f4 123) 123.0))
    (is (= (tables/coerce :one :f4 "123") 123.0))
    (is (= (type (tables/coerce :one :f4 "123")) java.lang.Double))
    (is (thrown? Exception (tables/coerce :one :f4 "not-a-number")))

    ;; test ascii
    (is (= (tables/coerce :one :f5 "123") "123"))
    (is (= (tables/coerce :one :f5  123 ) "123"))
    (is (thrown? Exception (tables/coerce :one :f5 (char 12345))))

    ;; test boolean
    (is (true?  (tables/coerce :one :f6 "true")))
    (is (true?  (tables/coerce :one :f6 "TrUe")))
    (is (true?  (tables/coerce :one :f6 "TRUE")))
    (is (false? (tables/coerce :one :f6 "false")))
    (is (false? (tables/coerce :one :f6 "FaLsE")))
    (is (false? (tables/coerce :one :f6 "FALSE")))
    (is (false? (tables/coerce :one :f6 nil)))
    (is (false? (tables/coerce :one :f6 "anything-else")))
    (is (false? (tables/coerce :one :f6 0)))
    (is (false? (tables/coerce :one :f6 1)))

    ;; test decimal
    (is (= (tables/coerce :one :f7 123) 123.0))
    (is (= (tables/coerce :one :f7 "123") 123.0))
    (is (= (type (tables/coerce :one :f7 "123")) java.lang.Double))
    (is (thrown? Exception (tables/coerce :one :f7 "not-a-number")))

    ;; test inet
    (is (= (tables/coerce :one :f8 "8.8.8.8") "8.8.8.8"))
    (is (thrown? Exception (tables/coerce :one :f8 "8.8.8")))
    (is (thrown? Exception (tables/coerce :one :f8 "8.8.8.257")))
    (is (thrown? Exception (tables/coerce :one :f8 "not-an-ipaddr")))
    (is (thrown? Exception (tables/coerce :one :f8 12345)))

    ;; test timeuuid
    (let [u (uuid/v1)]
      (is (= (tables/coerce :one :f9 (uuid/to-string u)) u)))
    (is (thrown? Exception (tables/coerce :one :f9 "not-a-uuid")))

    ;; test uuid
    (let [u (uuid/v1)]
      (is (= (tables/coerce :one :f10 (uuid/to-string u)) u)))
    (is (thrown? Exception (tables/coerce :one :f10 "not-a-uuid")))

    ;; test varchar
    (is (= (tables/coerce :one :f11 "a-random-varchar") (str "a-random-varchar")))
    (is (= (tables/coerce :one :f11 123) (str 123)))

    ;; test varint
    (is (= (tables/coerce :one :f12 123)) 123)
    (is (= (tables/coerce :one :f12 "123") 123))
    (is (= (tables/coerce :one :f12 "123.0") 123))
    (is (thrown? Exception (tables/coerce :one :f12 "not-a-number")))))

(deftest available-test
  (testing "testing tables/available"
    (let [schema-map {:a nil :b nil :c nil}
          expected   {:a "a" :b "b" :c "c"}]
      (is (= expected (tables/available schema-map))))))

(deftest partition-keys-test
  (testing "testing tables/partition-keys"
    (let [schema-map {:table1 {:pk1 {:kind "partition_key" :column_name "pk1"}
                               :pk2 {:kind "partition_key" :column_name "pk2"}
                               :sf1 {:kind "not_a_partkey" :column_name "sf1"}
                               :sf2 {:kind "not_a_partkey" :column_name "sf2"}}}
          expected [:pk1 :pk2]]
      (is (= expected (tables/partition-keys schema-map :table1))))))


(deftest partition-keys?-test
  (testing "testing tables/partition-keys?"
    (let [schema-map {:table1 {:pk1 {:kind "partition_key" :column_name "pk1"}
                               :pk2 {:kind "partition_key" :column_name "pk2"}
                               :sf1 {:kind "not_a_partkey" :column_name "sf1"}
                               :sf2 {:kind "not_a_partkey" :column_name "sf2"}}}]
      (is (true?  (tables/partition-keys? :table1 {:pk1 1 :pk2 2} schema-map)))
      (is (false? (tables/partition-keys? :table1 {:pk1 1 :pk3 3} schema-map))))))

