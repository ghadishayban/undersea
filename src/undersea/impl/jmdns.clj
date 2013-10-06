(ns undersea.impl.jmdns
  (:require [clojure.core.async :refer [go >! <! chan]]
            [undersea.protocols :as p])
  (:import [javax.jmdns JmDNS ServiceListener ServiceInfo ServiceEvent]))

(defn service [name port]
  (ServiceInfo/create "_edn._tcp.local." name (int port) ""))

(defn service-info->map [^ServiceInfo s]
  {:service-name (.getName s)
   :port (.getPort s)
   :domain (.getDomain s)
   :addresses (seq (.getHostAddresses s))
   :id (.getKey s)
   :name (.getName s)
   :url (.getURL s) ;; deprecated
   :service-type (.getProtocol s)
   :fq-name (.getQualifiedName s)
   :urls (seq (.getURLs s))})

(defrecord PureJavamDNS [name instance store]
  p/DNSSD
  (publish [this options]
    (assert (number? (:port options)) "Must provide :port to publish")
    (let [{port :port} options]
      (.registerService instance (service name port)))
    this)
  (unpublish [this]
    (.unregisterAllServices instance)
    this)
  (discover [_]
    (->> (vals @store)
         (filter map?)
         (into []))))

(defn listener [store]
  (reify ServiceListener
    (serviceAdded [this event]
      (swap! store assoc (.getName event) :unresolved))
    (serviceRemoved [this event]
      (swap! store dissoc (.getName event)))
    (serviceResolved [this event]
      (swap! store assoc
             (.getName event)
             (service-info->map (.getInfo event))))))

(defn make-service [name]
  (let [jm (JmDNS/create)
        store (atom {})]
    (.addServiceListener jm "_edn._tcp.local." (listener store))
    (PureJavamDNS. name jm store)))
