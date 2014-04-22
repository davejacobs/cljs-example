(ns client.app
  (:require [clojure.string :refer [join]]
            [clojure.browser.repl :as repl]
            [cljs.core.async :refer [<! >! put! close!]]
            [chord.client :refer [ws-ch]]
            [dommy.core :as dom])
  (:require-macros [cljs.core.async.macros :refer [go]]
                   [dommy.macros :refer [deftemplate node sel sel1]]
                   [clojure.core.strint :refer [<<]]))

(def application-state 
  (atom {:sequence []}))

(deftemplate nucleotide [letter]
  [:li {:class letter} letter])

(defn update-sequence! [selector old-seq new-seq]
  (let [seq-diff (drop (count old-seq) new-seq)]
    (dom/append! (sel1 ".sequence")
                 (map nucleotide seq-diff))))

(defn render [old-state new-state]
  (let [old-seq (old-state :sequence)
        new-seq (new-state :sequence)]
    (update-sequence! ".sequence" old-seq new-seq)))
 
(defn init! []
  (enable-console-print!)
  (add-watch application-state :app-watcher
             (fn [key reference old-state new-state]
               (render old-state new-state))))
 
(defn start-loading-data! [species-id chromosome start-pos len]
  (go
    (let [ws (<! (ws-ch "ws://localhost:8080/data"))]
      (>! ws {:species-id species-id
              :chromosome chromosome
              :start-pos start-pos
              :len len})
      ;; Read all data off of queue
      (while true
        (let [next-queue-item (<! ws)
              message (next-queue-item :message)]
          (swap! application-state 
                 update-in [:sequence] #(concat % message))))))
  (js/console.log "Go block is registered"))

(init!)

(defn form-value-for-name [form-element name]
  (-> form-element 
    (sel1 (<< "[name=~{name}]"))
    (#(.-value %))))

(defn on-submit-form [e]
  (.preventDefault e)
  (let [form (.-currentTarget e)
        species-id (form-value-for-name form "species-id")
        chromosome (form-value-for-name form "chromosome")
        start-pos (form-value-for-name form "start-pos")
        len (form-value-for-name form "len")]
    (start-loading-data! species-id chromosome start-pos len)))

(dom/listen! 
  (sel1 :form)
  :submit on-submit-form)
