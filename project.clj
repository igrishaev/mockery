(defproject mockery "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[slingshot "0.12.2"]]

  :profiles {:dev {:plugins [[autodoc/lein-autodoc "1.1.1"]]
                   :dependencies [[org.clojure/clojure "1.8.0"]]}})
