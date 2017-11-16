
repl:
	lein repl

.PHONY: test
test:
	lein test

deploy:
	lein deploy clojars
