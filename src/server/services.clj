(ns server.services
  (:require [clojure.core.strint :refer [<<]]
            [clj-http.client :as http]
            [cheshire.core :as json]
            [ring.util.response :refer [response]]))

(def base-url "http://togows.dbcls.jp/entry/nucleotide")

(defn fetch-data-for-gene-id [gene-id]
  ;; << is string interpolation, provided by clojure.incubator
  (let [url (<< "~{base-url}/~{gene-id}/seq.json")
        resp (http/get url)
        body (json/decode (resp :body))]
    (first body)))
