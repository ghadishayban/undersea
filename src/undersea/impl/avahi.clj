(ns undersea.impl.avahi
  (:require [me.raynes.conch :refer [programs]]
            [me.raynes.conch.low-level :refer [destroy proc]]
            [undersea.protocols :as p]
            [clojure.string :as str]))

(programs avahi-browse)

(defn get-neighbors-raw
  []
  (let [raw-output (avahi-browse "-pkrta")
        lines (-> raw-output str/split-lines)]
    (->> lines
         (filter #(= \= (first %)))
         (map #(str/split % #";")))))

(def attrs
  [:garbage
   :network-interface
   :ip-type
   :servicename
   :service-type
   :domain
   :host
   :ip
   :port
   :TXT])

(defn parse-dnstxt
  [neighbor]
  (if-let [txt (get neighbor :TXT)]
    (let [parsed (->> txt
                     (re-seq #"\"([^=]*)=([^\"]*)\"")
                     (map (comp vec rest))
                     (into {}))]
      (-> neighbor
          (assoc :dns-txt parsed)
          (dissoc :TXT)))
    neighbor))

(defn get-neighbors
  []
  (let [clean-fn (comp parse-dnstxt
                     #(dissoc % :garbage)
                     #(zipmap attrs %)) ]
    (->> (get-neighbors-raw)
         (mapv clean-fn))))

(defrecord Avahi [name]
  p/DNSSD
  (publish [this options]
    (let [port (get options :port)]
      (assert port "Require :port to publish")
      (assoc this :instance (proc "avahi-publish" "-s" name "_edn._tcp" (str port)))))
  (unpublish [this]
    (destroy (get this :instance))
    (dissoc this :instance))
  (discover [_]
    (filterv (= "_edn._tcp" (:service-type %))
              (get-neighbors))))
