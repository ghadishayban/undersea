(ns undersea
  (:require [undersea.protocols :refer :all]
            [undersea.impl.jmdns :refer (dnssd-service)]
            [undersea.impl.client :refer (client)]
            [undersea.impl.http :refer (server)]))

;; Global *only* because this is a tool for facilitating development
(def ^:private the-server (atom nil))

(defn ^:private stop-if-running
  []
  (when-let [dns (:dns @the-server)]
    (unpublish dns))
  (when-let [http-server (:server @the-server)]
    (stop http-server))
  (reset! the-server nil))

(defn broadcast
  "Starts a named node and broadcasts availability through DNS-SD/Bonjour"
  [your-name]
  (stop-if-running)
  (let [http-server (server)
        port (get http-server :port)
        dns (dnssd-service your-name)]
    (publish dns {:port port})
    (reset! the-server
            {:dns dns
             :server http-server
             :name your-name})))

(defn- running?
  []
  (:dns @the-server))

(defn share!
  "Share a payload of EDN serializable data."
  [k data]
  (assert (running?) "Call broadcast first")
  (-> (:server @the-server)
      (serve (name k) data))
  :success)

(defn whoisaround?
  []
  (let [neighbors (discover (:dns @the-server))
        local-name (-> @the-server :name)]
    (remove #(= local-name (:name %)) neighbors)))  ;; move to impl?

(defn- grab-from
  [uri k]
  (let [c (client uri)]
    (retrieve c (name k))))

(defn grab
  [k]
  (let [neighbors (whoisaround?)]
    (cond
     (empty? neighbors)
     :you-are-alone

     (= (count neighbors) 1)
     (grab-from (-> neighbors first :url) k)

     :else
     :ambiguous-peer)))

(defn- find-peer-uri
  [p]
  (->> (whoisaround?)
       (filter #(= (:name %) p))
       first
       :url))

(defn browse-peer [peer-name]
  (available (client (find-peer-uri peer-name))))
