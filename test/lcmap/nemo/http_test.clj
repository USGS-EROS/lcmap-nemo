(ns lcmap.nemo.http-test
  (:require [clojure.test :refer :all]
            [lcmap.nemo.config :as config]
            [lcmap.nemo.fixtures :as fixtures]
            [lcmap.nemo.http :refer :all]
            [lcmap.nemo.shared :as shared]
            [org.httpkit.client :as http]))

(use-fixtures :once fixtures/all-fixtures)

(defn http-tables
  []
  (shared/go-fish {:url ""}))

(defn http-partition-keys
  [table]
  (shared/go-fish {:url (format "/%s" table)}))

(defn http-partition-data
  [table partition-keys]
  (shared/go-fish {:url (format "/%s" table) :query-params partition-keys}))

(deftest get-tables-url-test
  (testing "testing get-table resource"
    (let [resp (http-tables)]
      (is (= 200 (:status resp)))
      (is (= (:db-tables (config/checked-environment)) (-> resp :body :tables))))))

(deftest get-partition-keys-test
  (testing "testing get-partition-keys resource"
    (let [resps (doall (map http-partition-keys (-> (http-tables) :tables)))]
      (is (every? true? (doall (map #(= 200 (:status %)) resps))))
      (is (every? true? (doall (map #(< 0 (count (-> % :body))) resps)))))))

(deftest get-partitions-test
  (testing "testing get-partition resource"
    (let [tables         (-> (http-tables) :tables)
          partition-keys (map #([% (-> % http-partition-keys :body)]) tables)
          partition-data (map http-partition-data partition-keys)]
      (is (every? true? (doall (map #(= 200 (:status %)) partition-data))))
      (is (every? true? (doall (map #(= 18  (-> % :body count)) partition-data)))))))

(deftest get-healthy-test
  (testing "it looks good right now"
    (let [resp (shared/go-fish {:url "/healthy"})]
      (is (= 200 (:status resp))))))

