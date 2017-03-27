(ns mockery.core-test
  (:require [clojure.test :refer :all]
            [mockery.core :refer [with-mock]]))

(defn test-fn [a b]
  (+ a b))

(deftest test-simple
  (with-mock mock
    {:target :test-fn
     :return 42}
    (let [result (test-fn 1 2)]
      (is (= result 42))
      (is (= @mock
             {:called? true
              :call-count 1
              :call-args '(1 2)
              :call-args-list '((1 2))
              :target :test-fn
              :return 42})))))
