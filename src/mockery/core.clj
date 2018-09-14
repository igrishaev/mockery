(ns mockery.core
  "A library to mock Clojure functions.")

(def ^:private
  +mock-defaults+
  "Mock default fields."
  {:called? false
   :call-count 0
   :call-args nil
   :call-args-list []
   :return-list []})

(defn- keyword-to-symbol
  "Turns a keyword into a symbold."
  [kwd]
  (apply symbol ((juxt namespace name) kwd)))

(defn make-mock
  "Retuns an atom the represents a mock."
  [opt]
  (-> +mock-defaults+
      (merge opt)
      atom))

(defn- smart-throw
  [exc]
  (cond
    (instance? Exception exc)
    (throw exc)

    (map? exc)
    (throw (ex-info "Mockery exception" exc))

    :default
    (assert false (format "Unsupported exception data: %s" exc))))

(defn eval-mock [mock args]
  (let [return (:return @mock)]
    (if (fn? return)
      (apply return args)
      return)))

(defn make-mock-fn
  "Returns a function to substitute a target.
  Calling this function will change the mock's state."
  [mock]
  (fn [& args]

    ;; update mock fields
    (swap! mock assoc :called? true)
    (swap! mock update :call-count inc)
    (swap! mock assoc :call-args args)
    (swap! mock update :call-args-list conj args)

    ;; trigger side effect
    (when-let [side-effect (:side-effect @mock)]
      (side-effect))

    ;; trigger exception
    (when-let [exc (:throw @mock)]
      (smart-throw exc))

    ;; eval the value, save it and return
    (let [result (eval-mock mock args)]
      (swap! mock update :return-list conj result)
      result)))

(defn- qualified?
  [symbol]
  (not (nil? (namespace symbol))))

(defn resolve-failure [target]
  (assert false (format "Cannot resolve target: %s" target)))

(defn resolve!
  "Resolves a variable by either a symbol or keyword.
  Rises an assertion error if not found."
  [target]
  (cond

    (symbol? target)
    (if (qualified? target)
      (if-let [ref (resolve target)]
        ref
        (resolve-failure target))
      (resolve-failure target))

    (keyword? target)
    (recur (keyword-to-symbol target))

    :default
    (resolve-failure target)))

(defmacro with-mocks
  "Like `with-mock` but for multiple mocks at once. `bind-opt` is a
  vector where each odd item is a bind symbol and even item is an
  option map. See `with-mock` for detailed description of each
  parameter."
  [bind-opt & body]
  `(let [~@(for [[i# el#] (map-indexed vector bind-opt)]
             (if (even? i#)
               el#
               `(make-mock ~el#)))]
     (with-redefs-fn
       (hash-map
        ~@(for [[i# el#] (map-indexed vector (reverse bind-opt))]
            (if (even? i#)
              `(resolve! (:target ~el#))
              `(make-mock-fn ~el#))))
       (fn []
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

  -- `:throw`: either an Exception instance to be thrown with the
  standard `throw` pipeline or any map to be thrown with `(ex-info)`
  wrapper. Useful to simulate runtime exceptions.

  More on mock instance.

  The mock instance you have bound to the `mock` parameter is an atom
  holds a map. It extends the `opt` map described above. In addition
  to those immutable fields, it has the following ones:

  -- `:called?`: a boolean flag indicates whether a function was called
  at least one time or not. `false` by default.

  -- `:call-count`: a number times the function was called. 0 by default.

  -- `:call-args`: the last arguments were passed to the
  function. `nil` by default.

  -- `:call-args-list`: a vector of all the args were passed, `[]` by
  default.

  -- `:return`: the latest returned value.

  -- `:return-list`: a vector of all the values have ever been returned.

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
