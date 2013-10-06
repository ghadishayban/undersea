(ns undersea.protocols)

(defprotocol Neighbor
  (available [_])
  (retrieve [_ k]))

(defprotocol Stash
  (serve [_ k payload])
  (start [_])
  (stop [_]))

(defprotocol DNSSD
  (publish [_ options])
  (unpublish [_])
  (discover [_]))
