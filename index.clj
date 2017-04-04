{:namespaces
 ({:doc "A library to mock Clojure functions.",
   :name "mockery.core",
   :wiki-url "https://igrishaev.github.io/mockeryindex.html",
   :source-url
   "https://github.com/igrishaev/mockery/blob/d27ae4124e2a02c633cc2fa442b33fb01b7092b7/src/mockery/core.clj"}),
 :vars
 ({:raw-source-url
   "https://github.com/igrishaev/mockery/raw/d27ae4124e2a02c633cc2fa442b33fb01b7092b7/src/mockery/core.clj",
   :name "with-mock",
   :file "src/mockery/core.clj",
   :source-url
   "https://github.com/igrishaev/mockery/blob/d27ae4124e2a02c633cc2fa442b33fb01b7092b7/src/mockery/core.clj#L104",
   :line 104,
   :var-type "macro",
   :arglists ([mock opt & body]),
   :doc
   "Runs the code block substituting a target function with a dummy\none (\"mock\" function). This dummy function behaves the way you\ndetermine with options. It counts the number of calls, accumulates\nit's arguments, can cause side effects or rise a given\nexception. Once you go out from the macro, the target function will\nobtain it's origin value.\n\nArguments:\n\n- `mock`: a symbol to bind a mock instance inside the code block,\n\n- `opt`: a map with the following parameters:\n\n-- `:target` (required): a symbol or a keyword points to a target\nfunction to mock. May include a namespace (be full-qualified).\n\n-- `:value`: any value to return from a mocked function. If it's a\nfunction by itself (defn, fn or #(...)), it will be called\nafterwards.\n\n-- `:side-effect`: any function with no arguments that is triggered\nwhen calling a target function.\n\n-- `:throw`: either an Exception instance to be thrown with standard\n`throw` pipeline or any data structure (usually a map) to throw it\nwith Slingshot's `throw+` macro. Useful to simulate runtime\nexceptions.\n\nMore on mock instance.\n\nThe mock you bind with a `mock` parameter is an atom holds a map. It\nextends the `opt` map described above. In addition to those\nimmutable fields, it has the following ones:\n\n-- `:called?`: a boolean flag indicates whether a function was called\nat least one time or not. `false` by default.\n\n-- `:call-count`: a number times the function was called. 0 by default.\n\n-- `:call-args`: the last arguments were passed to the\nfunction. `nil` by default.\n\n-- `:call-args-list`: a vector of all the args were passed, `[]` by\ndefault.\n\nExample:\n\n(with-mock mock\n  {:target :clojure.pprint/pprint\n   :return \":-)\"\n   :side-effect #(println \"Hi!\")}\n  (clojure.pprint/pprint {:foo 42}))\n\nHi!\n:-)\n\nOnce you deref the `mock` variable inside the code block, you'll get:\n\n{:called? true\n :call-count 1\n :call-args ({:foo 42})\n :call-args-list [({:foo 42})]\n :target :clojure.pprint/pprint\n :return \":-)\"\n :side-effect #function[mockery.core/eval12666/fn--12667]}\n\nChecking `:call-count` and `:call-args` might be good for your unit\ntests.",
   :namespace "mockery.core",
   :wiki-url
   "https://igrishaev.github.io/mockery/index.html#mockery.core/with-mock"}
  {:raw-source-url
   "https://github.com/igrishaev/mockery/raw/d27ae4124e2a02c633cc2fa442b33fb01b7092b7/src/mockery/core.clj",
   :name "with-mocks",
   :file "src/mockery/core.clj",
   :source-url
   "https://github.com/igrishaev/mockery/blob/d27ae4124e2a02c633cc2fa442b33fb01b7092b7/src/mockery/core.clj#L82",
   :line 82,
   :var-type "macro",
   :arglists ([bind-opt & body]),
   :doc
   "Like `with-mock` but for multiple mocks at once. `bind-opt` is a\nvector where each odd item is a bind symbol and even item is an\noption map. See `with-mock` for detailed description of each\nparameter.",
   :namespace "mockery.core",
   :wiki-url
   "https://igrishaev.github.io/mockery/index.html#mockery.core/with-mocks"})}
