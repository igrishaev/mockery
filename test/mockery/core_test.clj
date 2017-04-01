(ns mockery.core-test
  (:require [clojure.test :refer :all]
            [mockery.core :refer [with-mock]]))

(defn test-fn [a b]
  (+ a b))

(deftest test-simple
  (with-mock mock
    {:target test-fn
     :return 42}
    (let [result (test-fn 1 2)]
      (is (= result 42))
      (is (= @mock 1))
)))

(deftest test-multiple
  (with-mock mock
    {:target test-fn
     :return 42}
    (test-fn 1)
    (test-fn 1 2)
    (test-fn 1 2 3)
    (is (= @mock 1))))

(deftest test-keyword
  (with-mock mock
    {:target :test-fn
     :return 42}
    (test-fn 1)
    (is (= @mock 1))))

(deftest test-throw
  (with-mock mock
    {:target :test-fn
     :throw (Exception. "boom")}
    (test-fn 1)
    (is (= @mock 1))))

(deftest test-slingshot
  (with-mock mock
    {:target :test-fn
     :throw {:type :boom}}
    (test-fn 1)
    (is (= @mock 1))))

(deftest test-sideeffect
  (with-mock mock
    {:target :test-fn
     :return 42
     :side-effect #(print 42)
}
    (test-fn 1)
    (is (= @mock 1))))

(deftest test-require-ns
  (with-mock mock
    {:target mockery.foreign-ns/foreign-fn
     :return 42
     }
    (mockery.foreign-ns/foreign-fn)
    (is (= @mock 1))))
