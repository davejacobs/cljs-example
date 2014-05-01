(ns client.app
  (:require [client.helpers :as h]
            [clojure.string :refer [upper-case]]
            [cljs.core.async :refer [<! >! put! close! timeout]]
            [chord.client :refer [ws-ch]]
            ;; For jQuery interop
            ; [jayq.core :refer [$ css html] :as jq]
            [dommy.core :as dommy])
  (:require-macros [cljs.core.async.macros :refer [go]]
                   [clojure.core.strint :refer [<<]]
                   [dommy.macros :refer [deftemplate node sel sel1]] 
                   [jayq.macros :refer [ready]]))

(declare on-pause-fetch)

(def counter
  (atom 0))

(def application-state 
  (atom {:sequences {}}))

;; This is a separate atom right now and keeps track of pause/play
;; state.
(def reading-state 
  (atom {:sequences {}}))

(deftemplate sequence-templ [identifier]
  [:div.sequence-wrapper
   [:ul {:class identifier}]
   [:a {:href "#"
        :class "pause-fetch"
        :data-identifier identifier}
    "Pause"]])

(deftemplate nucleotide-templ [base]
  [:li
   {:classes [base "nucleotide"]}
   base])

(defn nucleotides-templ [sequence]
  (map nucleotide-templ sequence))

(defn update-sequence! [identifier old-seq new-seq]
  (let [identifier-name (name identifier)
        seq-diff (drop (count old-seq) new-seq)
        elem (sel1 (<< ".~{identifier-name}"))]
    (apply dommy/append! elem (nucleotides-templ seq-diff))))

(defn insert-sequence! [identifier new-seq]
  (let [identifier-name (name identifier)
        sequence (sequence-templ identifier-name)]
    (-> (sel1 ".sequences")
      (dommy/append! sequence))
    (-> (sel1 sequence ".pause-fetch")
      (dommy/listen! :click on-pause-fetch))
    (update-sequence! identifier [] new-seq)))

(defn render! [old-state new-state]
  (doseq [[identifier new-seq] (new-state :sequences)]
    (if-let [old-seq ((old-state :sequences) identifier)]
      (update-sequence! identifier old-seq new-seq)
      (insert-sequence! identifier new-seq))))

(defn render-search-sequence! [search-sequence]
  (doseq [[identifier sequence] (@application-state :sequences)]
    (let [identifier-name (name identifier)
          search-positions (h/find-pos sequence search-sequence) 
          highlight-len (count search-sequence)] 
      (doseq [initial-pos search-positions
              distance (range highlight-len)]
        (-> (sel (<< ".~{identifier-name} > li"))
          (get (+ initial-pos distance))
          (#(node %))
          (dommy/add-class! "highlighted"))))))
 
(defn start-loading-data! [query]
  ;; Bump the counter
  (swap! counter inc)
  (let [identifier (keyword (str "sequence-" @counter))]
    ;; Allot a new place in state for this sequence
    (swap! application-state assoc-in [:sequences identifier] []) 
    (swap! reading-state assoc-in [:sequences identifier] true) 
    ;; Request the data
    (go
      (let [ws (<! (ws-ch "ws://localhost:8080/data"))]
        (>! ws query)
        ;; Read all data off of queue
        (while true
          (if (get-in @reading-state [:sequences identifier])  
            (let [next-queue-item (<! ws)
                  message (next-queue-item :message)]
              (swap! application-state 
                     update-in [:sequences identifier] #(concat % message)))
            (<! (timeout 500))))))))

(defn start-loading-data-from-form! [form fields]
  (let [query (h/form->map form fields)]
    (start-loading-data! query)))

(defn on-fetch-sequences [e]
  (.preventDefault e)
  (let [form (.-currentTarget e)
        fields [:species-id :chromosome :start-pos :len]]
    (h/save-form-to-cookie! form fields)
    (start-loading-data-from-form! form fields)))

(defn on-search-sequences [e]
  (.preventDefault e)
  (let [form (.-currentTarget e)
        search-val (h/form-value-for-name form "search-sequence")
        search-seq (seq (upper-case search-val))]
    (render-search-sequence! search-seq)))

(defn on-pause-fetch [e]
  (.preventDefault e)
  (let [target (.-currentTarget e)
        identifier (keyword (dommy/attr target "data-identifier"))]
    (swap! reading-state assoc-in [:sequences identifier] false)))

(defn bind-events! []
  (-> (sel1 "form.fetch-sequences")
    (dommy/listen! :submit on-fetch-sequences))
  (-> (sel1 "form.search-sequences")
    (dommy/listen! :submit on-search-sequences)))

(defn init! [& args]
  (enable-console-print!)
  (h/load-form-from-cookie! (sel1 "form")
                            ["species-id" "chromosome" "start-pos" "len"])
  (add-watch application-state :app-watcher
             (fn [key reference old-state new-state]
               (render! old-state new-state)))
  (bind-events!))

(-> js/document
  (dommy/listen! "DOMContentLoaded" init!))
