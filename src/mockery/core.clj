(ns mockery.core
  (:require [slingshot.slingshot :refer [throw+]]))

(def +mock-defaults+
  {:called? false
   :call-count 0
   :call-args nil
   :call-args-list []})

(defn keyword-to-symbol
  [kwd]
  (apply symbol ((juxt namespace name) kwd)))

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

    ;; trigger side effect
    (when-let [side-effect (:side-effect @mock)]
      (side-effect))

    ;; trigger exception
    (when-let [exc (:throw @mock)]
      (if (instance? Exception exc)
        (throw exc)
        (throw+ exc)))

    ;; return value
    (let [return (:return @mock)]
      (if (fn? return)
        (return)
        return))))

(defn check-resolve! [target]
  (when-not (resolve target)
    (when-let [ns' (namespace target)]
      (require (symbol ns')))))

(defn cons? [val]
  (instance? clojure.lang.Cons val))

(defn coerce-target [target]
  (cond
    (symbol? target)
    target

    (keyword? target)
    (keyword-to-symbol target)

    (and (cons? target)
         (-> target first (= 'quote)))
    (second target)

    :else
    (throw (Exception. (format "Wrong target: %s" target)))))

(defmacro with-mocks
  [bind-opt & body]
  (let [binds (take-nth 2 bind-opt)
        opts (take-nth 2 (rest bind-opt))
        targets (mapv #(-> % :target coerce-target) opts)
        make-mock* (fn [bind opt]
                     `(make-mock ~opt))
        mocks (mapv make-mock* binds opts)
        target-fns (for [bind binds]
                     `(make-mock-fn ~bind))
        lets* (vec (interleave binds mocks))
        redefs* (vec (interleave targets target-fns))]
    (doseq [target targets]
      (check-resolve! target))
    `(let ~lets*
       (with-redefs ~redefs*
         ~@body))))

(defmacro with-mock
  [mock opt & body]
  (let [target (-> opt :target coerce-target)]
    (check-resolve! target)
    `(let [~mock (make-mock ~opt)
           target-fn# (make-mock-fn ~mock)]
       (with-redefs [~target target-fn#]
         ~@body))))
