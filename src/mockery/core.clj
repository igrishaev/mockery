(ns mockery.core
  "A library to mock Clojure functions."
  (:require [slingshot.slingshot :refer [throw+]]))

(def ^:private
  +mock-defaults+
  "Mock default fields."
  {:called? false
   :call-count 0
   :call-args nil
   :call-args-list []})

(defn- keyword-to-symbol
  "Turns a keyword into a symbold."
  [kwd]
  (apply symbol ((juxt namespace name) kwd)))

(defn- make-mock
  "Retuns an atom the represents a mock."
  [opt]
  (-> +mock-defaults+
      (merge opt)
      atom))

(defn- make-mock-fn
  "Returns a function to substitute a target.
  Calling this function will change the mock's state."
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

(defn- check-resolve!
  "Checks whether it's possible to resolve a target symbol.
  In case it's not, silently requires a namespace (if supplied into a
  symbol)."
  [target]
  (when-not (resolve target)
    (when-let [ns' (namespace target)]
      (require (symbol ns')))))

(defn- cons? [val]
  (instance? clojure.lang.Cons val))

(defn- coerce-target
  "Tries to convert various data to a symbol."
  [target]
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
  "Like `with-mock` but for multiple mocks at once. `bind-opt` is a
  vector where each odd item is a bind symbol and even item is an
  option map. See `with-mock` for detailed description of each
  parameter."
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
  "Runs the code block substituting a target function with a dummy
  one (\"mock\" function). This dummy function behaves the way you
  determine with options. It counts the number of calls, accumulates
  it's arguments, can cause side effects or rise a given
  exception. Once you go out from the macro, the target function will
  obtain it's origin value.

  Arguments:

  - `mock`: a symbol to bind a mock instance inside the code block,

  - `opt`: a map with the following parameters:

  -- `:target` (required): a symbol or a keyword points to a target
  function to mock. May include a namespace (be full-qualified).

  -- `:value`: any value to return from a mocked function. If it's a
  function by itself (defn, fn or #(...)), it will be called
  afterwards.

  -- `:side-effect`: any function with no arguments that is triggered
  when calling a target function.

  -- `:throw`: either an Exception instance to be thrown with standard
  `throw` pipeline or any data structure (usually a map) to throw it
  with Slingshot's `throw+` macro. Useful to simulate runtime
  exceptions.

  More on mock instance.

  The mock you bind with a `mock` parameter is an atom holds a map. It
  extends the `opt` map described above. In addition to those
  immutable fields, it has the following ones:

  -- `:called?`: a boolean flag indicates whether a function was called
  at least one time or not. `false` by default.

  -- `:call-count`: a number times the function was called. 0 by default.

  -- `:call-args`: the last arguments were passed to the
  function. `nil` by default.

  -- `:call-args-list`: a vector of all the args were passed, `[]` by
  default.

  Example:

  (with-mock mock
    {:target :clojure.pprint/pprint
     :return \":-)\"
     :side-effect #(println \"Hi!\")}
    (clojure.pprint/pprint {:foo 42}))

  Hi!
  :-)

  Once you deref the `mock` variable inside the code block, you'll get:

  {:called? true
   :call-count 1
   :call-args ({:foo 42})
   :call-args-list [({:foo 42})]
   :target :clojure.pprint/pprint
   :return \":-)\"
   :side-effect #function[mockery.core/eval12666/fn--12667]}

  Checking `:call-count` and `:call-args` might be good for your unit
  tests."
  [mock opt & body]
  `(with-mocks [~mock ~opt]
     ~@body))
