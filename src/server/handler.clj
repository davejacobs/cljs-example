(ns server.handler
  (:require [clojure.java.io :as io]
            [clojure.core.async :refer [<! >! put! close! go]]
            [ring.middleware.reload :as reload]
            [ring.util.response :as response]
            [compojure.core :refer :all]
            [compojure.handler :refer [site]]
            [compojure.route :as route]
            [org.httpkit.server :refer [run-server]]
            [chord.http-kit :refer [with-channel]]
            [net.cgrand.enlive-html :as enlive]))

(defn websocket-handler [request]
  ;; This is a Chord wrapper for http-kit's with-channel macro
  (with-channel request ws-ch
    (go
      ;; OMG a while loop
      (while true
        (let [{:keys [message]} (<! ws-ch)]
          (>! ws-ch (str "[message received] " message)))))

    (go
      (doseq [x (range 100)]
        (>! ws-ch (str "Spontaneous data " x))))))

(enlive/deftemplate index
  (io/resource "public/index.html") []
  [:body] (enlive/append
            (enlive/html [:script {:src (browser-connected-repl-js)} ])))

(defroutes all-routes
  (GET "/data" {:as request} (websocket-handler request)) 
  (GET "/" [] (index))

  (route/resources "/" {:root "public"
                        :mime-types {:ttf "font/truetype"
                                     :otf "font/opentype"}})

  (route/not-found "<p>Page not found.</p>"))

(defn -main [& args]
  (let [handler (-> (site all-routes)
                  reload/wrap-reload)
        server (run-server handler {:port 8080})]

    (.addShutdownHook (Runtime/getRuntime)
                      (Thread.
                        #(do
                           (println "Server is shutting down")
                           (server))))

    (println "Server is up!")
    server))
