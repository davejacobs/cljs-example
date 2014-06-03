(defproject cljs-example "0.1.0"
  :description "This is an example of a Clojurescript app"
  :url "http://dna.wit.io"

  ;; Clojure and Clojurescript dependencies go here. Find libraries at:
  ;; - http://www.clojure-toolbox.com/
  ;; - http://www.chris-granger.com/projects/cljs/
  ;; - https://github.com/shaunxcode/clojurescript-ecosystem/wiki/libraries 
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/core.incubator "0.1.3"]
                 [org.clojure/clojurescript "0.0-2202"]
                 
                 ;; Server, routing
                 [http-kit/http-kit "2.1.16"]
                 [compojure/compojure "1.1.5"]
                 [om/om "0.6.2"]

                 ;; DOM manipulation
                 [prismatic/dommy "0.1.2"]
                 [jayq/jayq "2.5.0"]
                 [crate/crate "0.2.4"]

                 ;; Async channels
                 [org.clojure/core.async "0.1.278.0-76b25b-alpha"]

                 ;; Websockets + core.async
                 [jarohen/chord "0.3.1"]

                 ;; HTTP client
                 [clj-http/clj-http "0.9.1"]

                 ;; Middleware
                 [ring/ring-devel "1.1.8"]
                 [ring/ring-json "0.3.1"]]

  :plugins [[lein-cljsbuild "1.0.3"]]

  ;; `lein serve` runs the -main function in the namespace server.handler
  :aliases {"serve" ["run" "-m" "server.handler"]}

  ;; Point to both Clojure and Clojurescript directories to Clojure source paths
  :source-paths ["src" "src-cljs"]
  :resource-paths ["resources"]

  ;; Only point to Clojurescript directory in Clojurescript source paths
  :cljsbuild { 
    :builds [{:source-paths ["src-cljs"]
              :compiler 
              {:output-to "resources/public/javascripts/lib.js"
               :output-dir "resources/public/javascripts"
               :optimizations :none
               :source-map true
               :libs ["resources/components/jquery/dist/jquery.js"
                      "resources/components/jquery-cookie/jquery.cookie.js"]}}]})
