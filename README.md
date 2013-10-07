# undersea

Decentralized REPL data sharing over DNS-SD/Bonjour

## Usage

A dev tool to seamlessly grab EDN-readable data from other REPLs on the same LAN.

Send from your node:

```clojure
(use 'undersea)
(broadcast "your-node-name")  ;; a simple ID for DNS-SD/Bonjour

(share! :groceries #{:milk :eggs :cheetos})
```

To receive data from any REPL on the same local network:

```clojure
(grab :groceries)
=> #{:milk :eggs :cheetos}
```

## Pull request welcome for:

If there are multiple nodes on the network it will grab the key from the first one that succeeds

## Limitations
Local LAN only (maybe explore IPv6 DNS-SD sharing)
EDN readable data

## License

Copyright © 2013 Ghadi Shayban

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
