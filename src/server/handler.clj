(ns server.handler
  (:require [server.services :as services] 
            [clojure.java.io :as io]
            [clojure.core.async :refer [<! >! put! close! go timeout]]
            [ring.middleware.reload :as reload]
            [ring.util.response :as response]
            [compojure.core :refer :all]
            [compojure.handler :refer [site]]
            [compojure.route :as route]
            [org.httpkit.server :refer [run-server]]
            [chord.http-kit :refer [with-channel]]))

(defn nth-page [coll n page-size]
  (->> coll
    (drop (* page-size n))
    (take page-size)))

; (def gene-data 
  ; (services/fetch-gene-data "homo_sapiens" "X" 1000000 1000))

(defn websocket-handler [request]
  ;; This is a Chord wrapper for http-kit's with-channel macro
  (with-channel request ws-ch
    (go
      ;; OMG a while loop
      (while true
        (let [{:keys [message]} (<! ws-ch)
              {:keys [species-id chromosome start-pos len]} message
              gene-data (services/fetch-gene-data
                          species-id
                          chromosome 
                          start-pos
                          len)
              gene-sequence (gene-data :sequence)
              buffer-size 1]
          (doseq [x (range (/ (count gene-sequence) buffer-size))]
            (let [gene-slice (nth-page gene-sequence x buffer-size)] 
              ;; Throttle writing speed to once every 50 ms
              (<! (timeout 50))
              (>! ws-ch gene-slice))))))))

(defroutes all-routes
  (GET "/data" {:as request}
    (websocket-handler request)) 

  (GET "/" [] 
    (response/file-response
      "index.html" 
      {:root "resources/public"}))

  (route/resources "/" {:root "public"})
  (route/not-found "<p>Page not found.</p>"))

(defonce server 
  (atom nil))

(def handler 
  ;; Wrap the handler in any relevant middleware before
  ;; loading it into the Netty server
  (-> (site all-routes)
    reload/wrap-reload))

(defn start-server []
  (println "Starting server")
  (reset! server (run-server #'handler {:port 8080}))
  (println "Server is up!"))

;; Inspired by http-kit documentation
(defn stop-server []
  (when-not (nil? @server)
    (println "Shutting down server")
    (@server :timeout 100)
    (reset! server nil)))

(defn -main [& args]
  (start-server)
  (.addShutdownHook 
    (Runtime/getRuntime)
    (Thread. #(stop-server))))
