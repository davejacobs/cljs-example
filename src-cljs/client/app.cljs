(ns client.app
  (:require [clojure.string :refer [join]]
            [clojure.browser.repl :as repl]
            [cljs.core.async :refer [<! >! put! close!]]
            [chord.client :refer [ws-ch]]
            [domina :as dom])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(def application-state (atom []))
 
(defn render [old-state new-state]
  (dom/destroy-children! (dom/by-id "content"))
  (dom/append! (dom/by-id "content")
               (join "" (map #(str "<li>" % "</li>") new-state))))
 
(add-watch application-state :app-watcher
  (fn [key reference old-state new-state]
    (render old-state new-state)))
 
(defn init! []
  (enable-console-print!)
  ; (repl/connect "http://localhost:9000/repl")

  ;; See https://github.com/cemerick/austin/blob/master/browser-connected-repl-sample/README.md
  ;; for a convoluted explanation of WTF this is doing
  ; (def repl-env (reset! cemerick.austin.repls/browser-repl-env
                        ; (cemerick.austin/repl-env)))

  (dom/append! (dom/xpath "//body") "<script>Hello world!</script>"))


(defn connect-to-data-source! []
  (go
    (let [ws (<! (ws-ch "ws://localhost:8080/data"))]
      ;; Try out ping
      (>! ws "Server, server, wherefore art thou, server?")
      (js/console.log (:message (<! ws)))

      ;; Read all data off of queue
      (while true
        (let [next-queue-item (<! ws)
              message (next-queue-item :message)]
          (js/console.log message)
          (swap! application-state #(conj % message))))))
  (js/console.log "Go block is all registered"))

(init!)
(connect-to-data-source!)
