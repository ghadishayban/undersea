(ns undersea.impl.http
  (:require [undersea.protocols :as p]
            [ring.adapter.jetty :refer :all]
            [ring.util.response :refer (response)]
            [clout.core :refer (route-matches)]))

;; make atom storage a ttl-cache?

;; routes:
;; /available
;; /retrieve/:k

(defn- route [r]
  (cond
   (route-matches "/available" r)
   (assoc r :action :available)
   (route-matches "/retrieve/:k" r)
   (assoc r :action :retrieve)
   :else
   (throw (ex-info "Bad request" {:uri (get r :uri)}))))

(defmulti ^:private handle
  (fn [req store]
    (get req :action)))

(defmethod handle :available
  [req store]
  (into #{} (keys store)))

(defmethod handle :retrieve
  [req store]
  (when-let [item (:k (route-matches "/retrieve/:k" req))]
    (get store item)))

(defn- handler [s]
  (fn [req]
    (println req)
    (let [req (route req)
          resp (handle req @s)]
      (response (with-out-str (pr resp))))))

(def ^:private default-options
  {:join? false
   :port 0 ;; randomized port
   :min-threads 2
   :max-threads 5})

(defn- listen-port [s]
  (-> s
      .getConnectors
      first
      .getLocalPort))

(defrecord JettyServer [content]
  p/Stash
  (serve [_ k payload]
    (swap! content assoc k payload))
  (start [this]
    (let [app (handler content)
          server (run-jetty app default-options)
          port (listen-port server)]
      (assoc this :port port :server server)))
  (stop [this]
    (.stop (get this :server))))

(defn server
  []
  (let [jetty (JettyServer. (atom {}))]
    (p/start jetty)))
