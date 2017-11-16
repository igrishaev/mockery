(defproject mockery "0.1.1"
  :description "Mocking Clojure library."
  :url "https://github.com/igrishaev/mockery"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[slingshot "0.12.2"]]

  :profiles {:dev {:plugins [[autodoc/lein-autodoc "1.1.1"]]
                   :dependencies [[org.clojure/clojure "1.8.0"]]}}

  :autodoc {:name "Mockery"
            :page-title "Mockery API Documentation"
            :description "Mocking Clojure library."
            :web-src-dir "https://github.com/igrishaev/mockery/blob/"
            :web-home "https://igrishaev.github.io/mockery"
            :copyright "2017 Ivan Grishaev"})
