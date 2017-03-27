(ns mockery.core)

(def +mock-defaults+
  {:called? false
   :call-count 0
   :call-args ()
   :call-args-list ()})


{:target :foo/bar
 :value 1
 :return 42
 :return-rrobin 42
 :called? true
 :call-count 3
 :call-args (list 1 2 3)
 :call-args-list 1
 :side-effect #(print 42)
 :rise (Exception.)

 }

(defn resolve-keyword
  [kwd]
  (resolve (apply symbol ((juxt namespace name) kwd))))

(defn make-var [target]
  (cond
    (var? target)
    target

    (symbol? target)
    (resolve target)

    (keyword? target)
    (resolve-keyword target)

    :else
    (throw (Exception. "wrong target"))))

(defn make-redefs [opt]
  {(-> opt :target make-var) (make-mock opt)})

(defn make-mock [opt]
  (-> +mock-defaults+
      (merge opt)
      atom))

(defn make-mock-fn
  [mock]
  (fn [& args]

    ;; update mock fields
    (swap! mock assoc :called? true)
    (swap! mock update-in [:call-count] inc)
    (swap! mock assoc :call-args args)
    (swap! mock update-in [:call-args-list] conj args)

    ;; todo target-var?

    ;; trigger side effect
    (when-let [side-effect (:side-effect @mock)]
      (side-effect))

    ;; trigger exception
    (when-let [rise (:rise @mock)]
      (throw rise))

    ;; todo round robin

    ;; return value
    (:return @mock)))

(defmacro with-mock
  [mock opt & body]
  `(let [~mock (make-mock ~opt)
         target-var# (make-var (:target ~opt))
         target-fn# (make-mock-fn ~mock)]
     (with-redefs-fn {target-var# target-fn#} ;; todo just with-redefs
       (fn []
         ~@body))))

(defn test-fn [foo bar & args]
  [foo bar])

(defn test-mock []
  (with-mock mock
    {:target :test-fn
     :return 42}
    (test-fn 1 2 3)
    @mock))
