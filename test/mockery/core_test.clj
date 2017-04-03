(ns mockery.core-test
  (:require [clojure.test :refer :all]
            [slingshot.slingshot :refer [try+]]
            [mockery.core :refer [with-mock]]))

(defn test-fn [a b]
  (+ a b))

(deftest test-simple
  (with-mock mock
    {:target test-fn
     :return 42}
    (let [result (test-fn 1 2)]
      (is (= result 42))
      (is (= (-> @mock (dissoc :target))
             {:called? true
              :call-count 1
              :call-args '(1 2)
              :call-args-list '[(1 2)]
              :return 42})))))

(deftest test-multiple
  (with-mock mock
    {:target test-fn
     :return 42}
    (test-fn 1)
    (test-fn 1 2)
    (test-fn 1 2 3)
    (is (= (-> @mock (dissoc :target))
           {:called? true
            :call-count 3
            :call-args '(1 2 3)
            :call-args-list '[(1) (1 2) (1 2 3)]
            :return 42}))))

(deftest test-keyword
  (with-mock mock
    {:target :test-fn
     :return 42}
    (test-fn 1)
    (is (= (-> @mock :target)
           :test-fn))))

(deftest test-throw
  (with-mock mock
    {:target :test-fn
     :throw (Exception. "boom")}
    (is (thrown? Exception
                 (test-fn 1))) ))

(deftest test-slingshot
  (with-mock mock
    {:target :test-fn
     :throw {:type :boom}}
    (try+
     (test-fn 1)
     (is false "should not be executed")
     (catch [:type :boom] _
       (is true)))))

(deftest test-side-effect
  (let [trap (atom false)
        effect #(reset! trap true)]
    (with-mock mock
      {:target :test-fn
       :side-effect effect}
      (test-fn 1)
      (is @trap))))

(deftest test-require-ns
  (with-mock mock
    {:target mockery.foreign-ns/foreign-fn
     :return 42}
    (mockery.foreign-ns/foreign-fn)
    (is (-> @mock :called?))))
