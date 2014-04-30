(ns client.helpers
  (:require [jayq.core :refer [$] :as jq])
  (:require-macros [clojure.core.strint :refer [<<]]))

(defn set-cookie! [k v]
  ($.cookie k v))

(defn get-cookie [k]
  ($.cookie k))

(def input-type-mappings
  {"text" str
   "integer" int})

(defn form-value-for-name [form-element k]
  (let [input-elem (-> ($ form-element) 
                     (jq/find (<< "[name=~{k}]")))
        input-type (jq/data input-elem "typeclass")
        input-val (jq/val input-elem)
        output-type (get input-type-mappings input-type str)]
    (output-type input-val)))

(defn set-form-value-by-name! [form-element k v]
  (-> ($ form-element) 
    (jq/find (<< "[name=~{k}]"))
    (jq/val v)))

(defn save-form-to-cookie! [form fields]
  (doseq [field (map name fields)]
    (set-cookie! field (form-value-for-name form field))))

(defn load-form-from-cookie! [form fields] 
  (doseq [field (map name fields)]
    (set-form-value-by-name! form field (get-cookie field))))

(defn form->map [form fields]
  (let [kv-pairs (for [field fields]
                   [field (form-value-for-name form (name field))])]
    (into {} kv-pairs)))

;; Stolen from https://stackoverflow.com/questions/15223942/clojure-sub-sequence-position-in-sequence
;; because I'm lazy
(defn find-pos
  [sq sub]
  (->>
    (partition (count sub) 1 sq)
    (map-indexed vector)
    (filter #(= (second %) sub))
    (map first)))
