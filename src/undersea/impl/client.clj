(ns undersea.impl.client
  (:require [undersea.protocols :as p]
            [clj-http.client :as http]
            [clojure.edn :as edn]))

(defrecord HttpClient [uri]
  p/Neighbor
  (available [_]
    (-> (str uri "/available")
        http/get
        :body
        edn/read-string))
  (retrieve [_ k]
    (-> (str uri "/retrieve/" k)
        http/get
        :body
        edn/read-string)))
