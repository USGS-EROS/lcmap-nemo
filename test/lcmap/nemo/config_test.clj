(ns lcmap.nemo.config-test
  (:require [clojure.test :refer :all]
            [lcmap.nemo.config :refer :all]))

(deftest ok?-test
  (testing "testing good vs bad environments"
    (let [good [{:a 1} {:a true} {:a "a-ok"} {:a 1 :b false}]
          bad  [{:a nil} {:a 1 :b nil}]]
      (is (every? true?  (map :ok (map ok? good))))
      (is (every? false? (map :ok (map ok? bad)))))))

(deftest string->vector-test
  (testing "testing string->vector"
    (is (= ["one" "two" "three"] (string->vector "one,two,three")))
    (is (nil? (string->vector nil)))))

(deftest nil-kv?-test
  (testing "testing nil-kv?"
    (is (-> {:a nil} nil-kv? :a true?))
    (is (-> {:a 123} nil-kv? :a false?))))

(deftest with-except-test
  (testing "testing with-except"
    (is (true? (-> {:ok true} with-except :ok)))
    (is (thrown? Exception (-> {:ok false} with-except)))))

(deftest alia-test
  (testing "testing alia configuration"
    (let [env {:db-host "  host1,  host2   "
               :db-port 9999
               :db-user "user"
               :db-pass "pass"
               :consistency "1"
               :load-balancing-policy "lbp"}
          cfg (alia env)]
      
      (is (= ["host1" "host2"] (:contact-points cfg)))
      (is (= 9999 (:port cfg)))
      (is (= {:user "user" :password "pass"} (:credentials cfg)))
      (is (= {:consistency "1"} (:query-options cfg)))
      (is (= "lbp" (:load-balancing-policy cfg))))))
