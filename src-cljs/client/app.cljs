(ns client.app
  (:require [clojure.string :refer [join]]
            [clojure.browser.repl :as repl]
            [cljs.core.async :refer [<! >! put! close!]]
            [chord.client :refer [ws-ch]]
            [domina :as dom])
  (:require-macros [cljs.core.async.macros :refer [go]]))

;; The next three forms are typical boilerplate if you're not
;; using a framework/library
(def application-state (atom []))
 
;; Currently assumes only adding to vector -- should be much more
;; abstract
(defn render [old-state new-state]
  ; (dom/destroy-children! (dom/by-id "content"))
  (let [state-diff (drop (count old-state) new-state)]
    (dom/append! (dom/by-id "content")
                 (join "" (map #(str "<li class='" % "'>"
                                     %
                                     "</li>") state-diff)))))
 
(add-watch application-state :app-watcher
  (fn [key reference old-state new-state]
    (render old-state new-state)))
 
(defn init! []
  (enable-console-print!)
  (repl/connect "http://localhost:9000/repl"))
 
(defn connect-to-data-source! []
  (go
    (let [ws (<! (ws-ch "ws://localhost:8080/data"))]
      ;; Read all data off of queue
      (while true
        (let [next-queue-item (<! ws)
              message (next-queue-item :message)]
          (swap! application-state concat message)))))
  (js/console.log "Go block is registered"))

(init!)
(connect-to-data-source!)
