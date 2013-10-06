(defproject undersea "0.1.0-SNAPSHOT"
  :description "Decentralized REPL data sharing"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/core.async "0.1.242.0-44b1e3-alpha" :exclusions [org.clojure/clojurescript]]
                 [clj-http "0.7.7"]
                 [ring/ring-core "1.2.0"]
                 [ring/ring-jetty-adapter "1.2.0"]
                 [me.raynes/conch "0.5.0"]
                 [clout "1.1.0"]])
