(ns client.app
  (:require [clojure.string :refer [join]]
            [clojure.browser.repl :as repl]
            [cljs.core.async :refer [<! >! put! close!]]
            [chord.client :refer [ws-ch]]
            [dommy.core :as dom])
  (:require-macros [cljs.core.async.macros :refer [go]]
                   [dommy.macros :refer [node sel sel1]]))

;; The next three forms are typical boilerplate if you're not
;; using a framework/library
(def application-state (atom {:sequence []}))
 
;; Currently assumes only adding to vector -- break down into smaller functions
(defn render [old-state new-state]
  (let [old-seq (old-state :sequence)
        new-seq (new-state :sequence)
        seq-diff (drop (count old-seq) new-seq)]
    (dom/append! (sel1 ".sequence")
                 (map #(node [:li {:class %} %]) seq-diff))))
 
(add-watch application-state :app-watcher
  (fn [key reference old-state new-state]
    (render old-state new-state)))
 
(defn init! []
  (enable-console-print!))
 
(defn connect-to-data-source! []
  (go
    (let [ws (<! (ws-ch "ws://localhost:8080/data"))]
      (>! ws {:species "homo_sapiens"
              :chromosome "X"
              :start-pos 1000000
              :len 1000})
      ;; Read all data off of queue
      (while true
        (let [next-queue-item (<! ws)
              message (next-queue-item :message)]
          (swap! application-state update-in [:sequence] #(concat % message))))))
  (js/console.log "Go block is registered"))

(init!)
(connect-to-data-source!)
