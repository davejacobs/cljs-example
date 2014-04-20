(ns server.handler
  (:require [clojure.core.async :refer [<! >! put! close! go]]
            [ring.middleware.reload :as reload]
            [ring.util.response :as response]
            [compojure.core :refer :all]
            [compojure.handler :refer [site]]
            [compojure.route :as route]
            [org.httpkit.server :refer [run-server]]
            [chord.http-kit :refer [with-channel]]))

(defn websocket-handler [request]
  ;; This is a Chord wrapper for http-kit
  (with-channel request ws-ch
    (go
      (while true
        (let [{:keys [message]} (<! ws-ch)]
          (>! ws-ch (str "[message received] " message))
          #_(close! ws-ch))))

    (go
      (doseq [x (range 100)]
        (>! ws-ch (str "Unrequested data " x))))

    #_(on-close channel
      (fn [status]
        (println "channel closed")))

    #_(if (websocket? channel)
      (println "WebSocket channel")
      (println "HTTP channel"))

    #_(on-receive channel
      (fn [data]
        ;; An optional param can pass to send!:
        ;; close-after-send? When unspecified,
        ;; `close-after-send?` defaults to true for HTTP
        ;; channels and false for WebSocket.
        ;; (send! channel data close-after-send?)
        ;;
        ;; data is sent directly to the client
        (doseq [x (range 10)]
         (send! channel "[1 2 3]"))
         ))))

(defroutes all-routes
  (GET "/data" {:as request} (websocket-handler request)) 
  (GET "/" []
    (response/file-response "index.html"
                            {:root "resources/public"}))

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
