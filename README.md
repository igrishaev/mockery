# Mockery

[url-mock-2]:http://www.voidspace.org.uk/python/mock/
[url-ivan]:http://grishaev.me/
[url-foord]:http://www.voidspace.org.uk/python/weblog/index.shtml
[url-redefs]:https://clojuredocs.org/clojure.core/with-redefs-fn
[url-sling]:https://github.com/scgilardi/slingshot
[url-docs]:http://grishaev.me/mockery/
[url-tests]:https://github.com/igrishaev/mockery/blob/master/test/mockery/core_test.clj

Mockery is a simple and lightweight library to mock Clojure functions. It is
inspired by Python's [mock tool][url-mock-2] initially written
by [Michael Foord][url-foord].

## Installation

Add it to your dependencies:

`[mockery "0.1.1-SNAPSHOT"]`

Mockery is used mostly for unit tests so it's better to put it into the `:test`
project's profile but not the global one.

## Why?

Imagine you have a function that fetches data from any remote service, say
Google or Twitter. On your dev machine, you don't have valid credentials or even
cannot access a service due to network configuration.

How to write tests for such code? Moreover, how to ensure you application
handles an incorrect HTTP response?

```
HTTP/1.1 403 OK

{"error": "wrong credentials"}
```

Or even non-JSON data (standard Nginx page):

```
HTTP/1.1 504 Gateway Timeout

<html><body><p>Gateway Timeout</p></body></html>
```

Will your application deal with such a response properly without crushing with
500 error? If you think it will, how could you guarantee that?

Now imagine you have modern micro-service architecture where each action
requires collecting data across 3 internal web-services making API calls and to
compose a final result.

Mockery helps to imitate such unusual behaviour and write unit tests that cover
all the cases were mentioned.

## Example

Mockery provides a `with-mock` macros. It substitutes a function with a
temporary one while the code block is being executed. Inside the code block, you
may call that function without performing unwanted I/O.

Under the hood, it relies on the standard
Clojure [clojure.core/with-redefs-fn][url-redefs] function. The changes are
visible across all the threads while macros works.

Quick example:

```clojure
(with-mock mock
  {:target ::test-fn ;; your function to mock
   :return 42}       ;; the result
  (test-fn 1 2 3))   ;; will return 42
```

The first parameter, `mock` in our example, is a variable name to bind a mock
object to. The second one is a map of options. The `:target` is the only
required key that should be a full-qualified keyword or symbol. For example,
`:clojure.string/join` or `'clojure.string/join`. If you'd like to point a
function located at the same namespace, use double colon syntax as well:
`::my-func` (expands into something like `:your.current.ns/my-func`).

## Features

Mockery works with the built-tin unit test framework as well:

```clojure
(defn mock-google [f]
  (with-mock _
    {:target :some.project/get-geo-point ;; makes API call to Google
     :return {:lat 14.2345235
              :lng 52.523513}} ;; what your function should return instead
    (f)))

(use-fixtures
  :each
  mock-google)

(deftest ...)
```

During the test, every `(get-geo-point ...)` call will return just the data you
specified without network communication.

`:return` value could be a function that will be called to produce further
result.


```clojure
(with-mock _
  {:target :myapp.google/get-geo-point
   :return (fn [& _] 100500)}
  (myapp.google/get-geo-point)) ;; returns 100500
```

The library imitates throwing bith Java and [Slingshot][url-sling] exceptions:

```clojure

(with-mock mock
  {:target ::test-fn
   :throw (Exception. "boom")}
  (try
    (test-fn 1)
    (catch Exception e
      (println e)))) ;; it was thrown

(with-mock mock
  {:target ::test-fn
   :throw {:type :domain/error ;; slingshot map
           :data "boom"}}
  (try+
   (test-fn 1)
   (catch [:type :domain/error] data
     (println data)))) ;; process data map here
```

Add side effects (prints, logs) passing a `:side-effect` key with a function
without arguments:

```clojure
(with-mock mock
   {:target ::my-func
    :return 42
    :side-effect #(println "Hello!")}
   (my-func 1))
;; in addition to the result, "Hello!" will appear.
```

Mockery helps to ensure the function was called the exact number of times with
proper arguments. The `mock` atom holds a state that changes while you call the
mocked function. It accumulates its arguments and the number of calls:

```clojure
(with-mock mock
  {:target ::test-fn
   :return 42}
  (test-fn 1)
  (test-fn 1 2)
  (test-fn 1 2 3)
  @mock)

;; returns (some fields are skipped)

{:called? true
 :call-count 3
 :call-args '(1 2 3)                  ;; the last args
 :call-args-list '[(1) (1 2) (1 2 3)] ;; args history
 :return 42}
 ```

You may mock multiple functions at once using `with-mocks` macro:

```clojure
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
         :target ::test-fn}))
 (is (= @bar
        {:called? true
         :call-count 1
         :call-args '(1 2)
         :call-args-list '[(1 2)]
         :target ::test-fn-2})))
```

## Other

For further reading, check out [Mockery documentation][url-docs]
and [unit tests][url-tests]. Feel free to submit your issues/suggestions.

Mockery was written by [Ivan Grishaev][url-ivan], 2017.
