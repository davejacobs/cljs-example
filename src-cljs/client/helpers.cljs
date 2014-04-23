(ns client.helpers
  (:require [jayq.core :refer [$] :as jq])
  (:require-macros [clojure.core.strint :refer [<<]]))

(defn set-cookie! [k v]
  ($.cookie k v))

(defn get-cookie [k]
  ($.cookie k))

(defn form-value-for-name [form-element k]
  (-> ($ form-element) 
    (jq/find (<< "[name=~{k}]"))
    jq/val))

(defn set-form-value-by-name! [form-element k v]
  (-> ($ form-element) 
    (jq/find (<< "[name=~{k}]"))
    (jq/val v)))

(defn save-form-to-cookie! [form names]
  (doseq [name names]
    (set-cookie! name (form-value-for-name form name))))

(defn load-form-from-cookie! [form names] 
  (doseq [name names]
    (set-form-value-by-name! form name (get-cookie name))))
