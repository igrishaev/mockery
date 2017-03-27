(ns mockery.core
  (:require [slingshot.slingshot :refer [throw+]]))

(def +mock-defaults+
  {:called? false
   :call-count 0
   :call-args ()
   :call-args-list ()})

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

(defmacro with-mock
  [mock opt & body]
  `(let [~mock (make-mock ~opt)
         target-var# (make-var (:target ~opt))
         target-fn# (make-mock-fn ~mock)]
     (with-redefs-fn {target-var# target-fn#}
       (fn []
         ~@body))))

;; (defmacro with-mocks
;;   [mock-opt & body]
;;   (if (empty? mock-opt)
;;     42
;;     (let [[mock opt] (first mock-opt)
;;           mock-opt-rest (rest mock-opt)]
;;       `(with-mock ~mock ~opt
;;          (with-mocks ~mock-opt-rest ~@body))
;;       )))

;; (defmacro with-mocks
;;   [mock-opt & body]
;;   (let [mock-binds (take-nth 2 mock-opt)
;;         mock-opts (take-nth 2 (rest mock-opt))
;;         ;; mocks (map make-mock mock-opts)
;;         ;; target-funcs (map make-mock-fn mocks)
;;         ;; target-vars (map (comp make-var :target) mock-opts)
;;         binds (vec (interleave mock-binds mocks))
;;         ]
;;     `(let (vec (interleave ~mock-binds [1]))
;;        42


;;      ;;binds#
;;      ;; (let (vec binds#)
;;      ;;   (with-redefs-fn
;;      ;;     (zipmap target-vars# target-funcs#)
;;      ;;     (fn []
;;      ;;       ~@body)))
;;      )
;;     )
;; )

;; (defmacro with-mock
;;   [mock opt & body]
;;   `(with-mocks [~mock ~opt]
;;      ~@body))
