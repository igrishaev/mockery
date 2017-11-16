{:namespaces
 ({:doc "A library to mock Clojure functions.",
   :name "mockery.core",
   :wiki-url "https://igrishaev.github.io/mockeryindex.html",
   :source-url
   "https://github.com/igrishaev/mockery/blob/53aef8b6a47c0585bc1362cd1ab623078f52b27c/src/mockery/core.clj"}),
 :vars
 ({:raw-source-url
   "https://github.com/igrishaev/mockery/raw/53aef8b6a47c0585bc1362cd1ab623078f52b27c/src/mockery/core.clj",
   :name "make-mock",
   :file "src/mockery/core.clj",
   :source-url
   "https://github.com/igrishaev/mockery/blob/53aef8b6a47c0585bc1362cd1ab623078f52b27c/src/mockery/core.clj#L17",
   :line 17,
   :var-type "function",
   :arglists ([opt]),
   :doc "Retuns an atom the represents a mock.",
   :namespace "mockery.core",
   :wiki-url
   "https://igrishaev.github.io/mockery/index.html#mockery.core/make-mock"}
  {:raw-source-url
   "https://github.com/igrishaev/mockery/raw/53aef8b6a47c0585bc1362cd1ab623078f52b27c/src/mockery/core.clj",
   :name "make-mock-fn",
   :file "src/mockery/core.clj",
   :source-url
   "https://github.com/igrishaev/mockery/blob/53aef8b6a47c0585bc1362cd1ab623078f52b27c/src/mockery/core.clj#L36",
   :line 36,
   :var-type "function",
   :arglists ([mock]),
   :doc
   "Returns a function to substitute a target.\nCalling this function will change the mock's state.",
   :namespace "mockery.core",
   :wiki-url
   "https://igrishaev.github.io/mockery/index.html#mockery.core/make-mock-fn"}
  {:raw-source-url
   "https://github.com/igrishaev/mockery/raw/53aef8b6a47c0585bc1362cd1ab623078f52b27c/src/mockery/core.clj",
   :name "with-mock",
   :file "src/mockery/core.clj",
   :source-url
   "https://github.com/igrishaev/mockery/blob/53aef8b6a47c0585bc1362cd1ab623078f52b27c/src/mockery/core.clj#L113",
   :line 113,
   :var-type "macro",
   :arglists ([mock opt & body]),
   :doc
   "Runs the code block substituting a target function with a dummy\none (\"mock\" function). This dummy function behaves the way you\ndetermine with options. It counts the number of calls, accumulates\nit's arguments, can cause side effects or rise a given\nexception. Once you go out from the macro, the target function will\nobtain it's origin value.\n\nArguments:\n\n- `mock`: a symbol to bind a mock instance inside the code block,\n\n- `opt`: a map with the following parameters:\n\n-- `:target` (required): a symbol or a keyword points to a target\nfunction to mock. May include a namespace (be full-qualified).\n\n-- `:value`: any value to return from a mocked function. If it's a\nfunction by itself (defn, fn or #(...)), it will be called\nafterwards.\n\n-- `:side-effect`: any function with no arguments that is triggered\nwhen calling a target function.\n\n-- `:throw`: either an Exception instance to be thrown with the\nstandard `throw` pipeline or any map to be thrown with `(ex-info)`\nwrapper. Useful to simulate runtime exceptions.\n\nMore on mock instance.\n\nThe mock instance you have bound to the `mock` parameter is an atom\nholds a map. It extends the `opt` map described above. In addition\nto those immutable fields, it has the following ones:\n\n-- `:called?`: a boolean flag indicates whether a function was called\nat least one time or not. `false` by default.\n\n-- `:call-count`: a number times the function was called. 0 by default.\n\n-- `:call-args`: the last arguments were passed to the\nfunction. `nil` by default.\n\n-- `:call-args-list`: a vector of all the args were passed, `[]` by\ndefault.\n\nExample:\n\n(with-mock mock\n  {:target :clojure.pprint/pprint\n   :return \":-)\"\n   :side-effect #(println \"Hi!\")}\n  (clojure.pprint/pprint {:foo 42}))\n\nHi!\n:-)\n\nOnce you deref the `mock` variable inside the code block, you'll get:\n\n{:called? true\n :call-count 1\n :call-args ({:foo 42})\n :call-args-list [({:foo 42})]\n :target :clojure.pprint/pprint\n :return \":-)\"\n :side-effect #function[mockery.core/eval12666/fn--12667]}\n\nChecking `:call-count` and `:call-args` might be good for your unit\ntests.",
   :namespace "mockery.core",
   :wiki-url
   "https://igrishaev.github.io/mockery/index.html#mockery.core/with-mock"}
  {:raw-source-url
   "https://github.com/igrishaev/mockery/raw/53aef8b6a47c0585bc1362cd1ab623078f52b27c/src/mockery/core.clj",
   :name "with-mocks",
   :file "src/mockery/core.clj",
   :source-url
   "https://github.com/igrishaev/mockery/blob/53aef8b6a47c0585bc1362cd1ab623078f52b27c/src/mockery/core.clj#L91",
   :line 91,
   :var-type "macro",
   :arglists ([bind-opt & body]),
   :doc
   "Like `with-mock` but for multiple mocks at once. `bind-opt` is a\nvector where each odd item is a bind symbol and even item is an\noption map. See `with-mock` for detailed description of each\nparameter.",
   :namespace "mockery.core",
   :wiki-url
   "https://igrishaev.github.io/mockery/index.html#mockery.core/with-mocks"})}
