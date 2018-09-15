(ns mockery.core-test
  (:require [clojure.test :refer :all]
            [slingshot.slingshot :refer [try+]]
            [mockery.core :refer [with-mock with-mocks]]))

(defn test-fn [a b]
  (+ a b))

(defn test-fn-2 [x y]
  (* x y))

(deftest test-no-calls
  (with-mock mock
    {:target ::test-fn}
    (is (= (-> @mock :called?)
           false))))

(deftest test-opt-as-var
  (let [opt {:target ::test-fn}]
    (with-mock mock opt
      (test-fn 1 2)
      (is (= (-> @mock :called?)
             true)))))

(deftest test-old-value
  (let [old1 (test-fn 1 2)]
    (with-mock mock
      {:target ::test-fn
       :return 42}
      (let [new1 (test-fn 1 2)]
        (is (= new1 42))))
    (let [old2 (test-fn 1 2)]
      (is (= old1 old2 3)))))

(deftest test-target-var
  (with-mock mock
    {:target ::test-fn
     :return 42}
    (let [result (test-fn 1 2)]
      (is (= result 42))
      (is (= (-> @mock (dissoc :target))
             {:called? true
              :call-count 1
              :call-args '(1 2)
              :call-args-list '[(1 2)]
              :return-list [42]
              :return 42})))))

(deftest test-multiple-calls
  (with-mock mock
    {:target ::test-fn
     :return 42}
    (test-fn 1)
    (test-fn 1 2)
    (test-fn 1 2 3)
    (is (= (-> @mock (dissoc :target))
           {:called? true
            :call-count 3
            :call-args '(1 2 3)
            :call-args-list '[(1) (1 2) (1 2 3)]
            :return 42
            :return-list [42 42 42]}))))

(deftest test-target-symbol
  (with-mock mock
    {:target 'mockery.core-test/test-fn
     :return 42}
    (test-fn 1)
    (is (= (-> @mock :call-count)
           1))))

(deftest test-throw
  (with-mock mock
    {:target ::test-fn
     :throw (Exception. "boom")}
    (is (thrown? Exception
                 (test-fn 1))) ))

(deftest test-slingshot
  (with-mock mock
    {:target ::test-fn
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
      {:target ::test-fn
       :side-effect effect}
      (test-fn 1)
      (is @trap))))

(deftest test-return-fn
  (with-mock mock
    {:target ::test-fn
     :return #(+ 1 2 3)}
    (is (= (test-fn) 6))))

(deftest test-mock-multiple
  (with-mocks
    [foo {:target ::test-fn}
     bar {:target ::test-fn-2}]
    (test-fn 1)
    (test-fn-2 1 2)
    (is (= @foo
           {:called? true
            :call-count 1
            :call-args '(1)
            :call-args-list '[(1)]
            :target ::test-fn
            :return-list [nil]}))
    (is (= @bar
           {:called? true
            :call-count 1
            :call-args '(1 2)
            :call-args-list '[(1 2)]
            :target ::test-fn-2
            :return-list [nil]}))))

(deftest test-various-results
  (let [state (atom 0)
        func (fn [& _]
               (swap! state inc)
               @state)]
    (with-mocks
      [foo {:target ::test-fn
            :return func}]

      (test-fn :a)
      (test-fn :b)
      (test-fn :c)

      (is (= (dissoc @foo :return)
             {:called? true
              :call-count 3
              :call-args '(:c)
              :call-args-list '[(:a) (:b) (:c)]
              :return-list [1 2 3]
              :target ::test-fn})))))
