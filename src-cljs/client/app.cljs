(ns client.app
  (:require [client.helpers :as h]
            [cljs.core.async :refer [<! >! put! close!]]
            [chord.client :refer [ws-ch]]
            [jayq.core :refer [$ css html] :as jq])
  (:require-macros [cljs.core.async.macros :refer [go]]
                   [clojure.core.strint :refer [<<]]
                   [jayq.macros :refer [ready]]))

(def counter
  (atom 0))

(def application-state 
  (atom {:sequences {}}))

(defn nucleotide [base]
  (<< "<li class='~{base}'>~{base}</li>"))

(defn nucleotides [sequence]
  (apply str (map nucleotide sequence)))

(defn insert-sequence! [identifier new-seq]
  (let [identifier-name (name identifier)
        new-dom-elem (-> ($ (<< "<ul class='~{identifier-name}'>"))
                       (jq/append (nucleotides new-seq)))]
    (-> ($ ".sequences")
      (jq/append new-dom-elem))))

(defn update-sequence! [identifier old-seq new-seq]
  (let [identifier-name (name identifier)
        seq-diff (drop (count old-seq) new-seq)]
    (-> ($ (<< ".~{identifier-name}"))
      (jq/append (nucleotides seq-diff)))))

(defn render! [old-state new-state]
  (doseq [[identifier new-seq] (new-state :sequences)]
    (if-let [old-seq ((old-state :sequences) identifier)]  
      (update-sequence! identifier old-seq new-seq)
      (insert-sequence! identifier new-seq))))
 
(defn start-loading-data! [query]
  ;; Bump the counter
  (swap! counter inc)
  (let [identifier (keyword (str "sequence-" @counter))]
    ;; Allot a new place in state for this sequence
    (swap! application-state update-in [:sequences] #(assoc % identifier [])) 
    ;; Request the data
    (go
      (let [ws (<! (ws-ch "ws://localhost:8080/data"))]
        (>! ws query)
        ;; Read all data off of queue
        (while true
          (let [next-queue-item (<! ws)
                message (next-queue-item :message)]
            (swap! application-state 
                   update-in [:sequences identifier] #(concat % message))))))))

(defn start-loading-data-from-form! [form fields]
  (let [raw-query (for [field fields]
                    [field (h/form-value-for-name form field)])
        query (into {} raw-query)]
    (start-loading-data! query)))

(defn on-submit-form [e]
  (jq/prevent e)
  (let [form (.-currentTarget e)
        fields ["species-id" "chromosome" "start-pos" "len"]]
    (h/save-form-to-cookie! form fields)
    (start-loading-data-from-form! form fields)))

(defn bind-events! []
  (-> ($ "form")
    (jq/bind :submit on-submit-form)))

(defn init! []
  (enable-console-print!)
  (h/load-form-from-cookie! ($ "form")
                            ["species-id" "chromosome" "start-pos" "len"])
  (add-watch application-state :app-watcher
             (fn [key reference old-state new-state]
               (render! old-state new-state)))
  (bind-events!))

(jq/document-ready init!)
