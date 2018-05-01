(ns lcmap.nemo.http-test
  (:require [clojure.test :refer :all]
            [lcmap.nemo.shared :as shared]
            [lcmap.nemo.fixtures :as fixtures]
            [lcmap.nemo.config :as config]
            [lcmap.nemo.http :refer :all]
            [org.httpkit.client :as http]))


(use-fixtures :once fixtures/all-fixtures)


(deftest get-base-url-test
  (testing "it exists but it's fishy"
    (let [resp (shared/go-fish {:url ""})]
      (is (= 200 (:status resp)))
      (is (= ["Nemo loves Cassandra."] (-> resp :body))))))


(deftest get-healthy-test
  (testing "it looks good right now"
    (let [resp (shared/go-fish {:url "/healthy"})]
      (is (= 200 (:status resp))))))


