# Mockery

[url-mock-2]:http://www.voidspace.org.uk/python/mock/

[url-foord]:http://www.voidspace.org.uk/python/weblog/index.shtml

Simple and lightweight library to mock Clojure functions. If was inspired by
Python's [mock tool][url-mock-2] initially written by
[Michael Foord][url-foord].

## Why?

Imagine you have a function that fetches data from any remote service, say
Google or Twitter. On your dev machine, you don't have valid credentials or even
cannot access a service due to your network configuration. In that case, how
would you write tests for you code?

Even if your figured out with some kind of stub that returns a proper JSON, your
code is still unsafe...
are still unsafe from    ...

What will happen if the service return an incorrect data? For example:

```
HTTP/1.1 200 OK

{"status": "error"}
```

Or... 403


Or even non-JSON data:

```
HTTP/1.1 500 Internal Server Error

<html><body><p>Internal Server Error</p></body></html>
```

Would your application deal with such behaviour properly withour sending 500
errors to your customers? If you think yes, how can you garantee that?

Now imagine you have modern "micro-service" architecture where each request lets
you collect data from 3 internal web-services to compose a final result.

As you've already got, Mockery helps to imitate such unusual behaviour and write
unit tests with all required checks.

## Example

## Features

## Other
