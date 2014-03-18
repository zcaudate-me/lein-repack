(ns hara.common.constructor
  (:require [hara.common.types :refer [bytes?]]
            [hara.common.error :refer [error]]))

;; ## Constructors

(defn queue
  "Returns a `clojure.lang.PersistentQueue` object.

    (def a (queue 1 2 3 4))
    (seq (pop a) ;=> [2 3 4]
  "
  ([] (clojure.lang.PersistentQueue/EMPTY))
  ([x] (conj (queue) x))
  ([x & xs] (apply conj (queue) x xs)))

(defn uuid
  "Returns a `java.util.UUID` object

    (uuid) ;=> <random uuid>

    (uuid \"00000000-0000-0000-0000-000000000000\")
    ;=> #uuid \"00000000-0000-0000-0000-000000000000\"
  "
  ([] (java.util.UUID/randomUUID))
  ([id]
     (cond (string? id)
           (java.util.UUID/fromString id)
           (bytes? id)
           (java.util.UUID/nameUUIDFromBytes id)
           :else (error id " can only be a string or byte array")))
  ([^java.lang.Long msb ^java.lang.Long lsb]
     (java.util.UUID. msb lsb)))

(defn instant
  "Returns a `java.util.Date` object

    (instant) ;=> <current time>

    (instant 0) ;=> 1970-01-01T00:00:00.000-00:00
  "
  ([] (java.util.Date.))
  ([val] (java.util.Date. val)))

(defn uri
  "Returns a `java.net.URI` object

    (uri \"http://www.google.com\")
    ;=> #<URI http://www.google.com>
  "
  [path] (java.net.URI/create path))
