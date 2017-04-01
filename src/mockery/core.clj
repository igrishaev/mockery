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
    (:return @mock)))

(defn make-symbol [target]
  (cond
    (symbol? target)
    (resolve target)

    (keyword? target)
    (keyword-to-symbol target)

    :else
    (throw (Exception. (str target)))))

(defn check-resolve! [target]
  (when-not (resolve target)
    (when-let [ns' (namespace target)]
      (require (symbol ns')))))

(defn coerce-target [target]
  (cond
    (symbol? target)
    target

    (keyword? target)
    (keyword-to-symbol target)

    :else
    (throw (Exception. (format "Wrong target: %s" target)))))

(defmacro with-mock
  [mock opt & body]
  (let [target (-> opt :target coerce-target)]
    (check-resolve! target)
    `(let [~mock (make-mock ~opt)
           target-fn# (make-mock-fn ~mock)]
       (with-redefs [~target target-fn#]
         ~@body))))
