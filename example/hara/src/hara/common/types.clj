(ns hara.common.types)

;; ## Type Predicates
;;
;; Adds additional type predicates that are not in clojure.core

(defn boolean?
  "Returns `true` if `x` is of type `java.lang.Boolean`.

    (boolean? false) ;=> true
  "
  [x] (instance? java.lang.Boolean x))

(defn hash-map?
  "Returns `true` if `x` implements `clojure.lang.IPersistentMap`.

    (hash-map? {}) ;=> true
  "
  [x] (instance? clojure.lang.APersistentMap x))

(defn hash-set?
  "Returns `true` if `x` implements `clojure.lang.IPersistentHashSet`.

    (hash-set? #{}) ;=> true
  "
  [x] (instance? clojure.lang.PersistentHashSet x))

(defn long?
  "Returns `true` if `x` is of type `java.lang.Long`.

    (h/long? 1) ;=> true

    (h/long? 1N) ;=> false
  "
  [x] (instance? java.lang.Long x))

(defn bigint?
  "Returns `true` if `x` is of type `clojure.lang.BigInt`.

    (h/bigint? 1N) ;=> true
  "
  [x] (instance? clojure.lang.BigInt x))

(defn bigdec?
  "Returns `true` if `x` is of type `java.math.BigDecimal`.

     (h/bigdec? 1M) ;=> true
  "
  [x] (instance? java.math.BigDecimal x))

(defn instant?
  "Returns `true` if `x` is of type `java.util.Date`.

    (instant? (instant 0)) => true
  "
  [x] (instance? java.util.Date x))

(defn uuid?
  "Returns `true` if `x` is of type `java.util.UUID`.

    (uuid? (uuid)) ;=> true
  "
  [x] (instance? java.util.UUID x))

(defn uri?
  "Returns `true` if `x` is of type `java.net.URI`.

    (uri? (uri \"http://www.google.com\"))
    ;=> true
  "
  [x] (instance? java.net.URI x))

(defn bytes?
  "Returns `true` if `x` is a primitive `byte` array.

    (bytes? (byte-array 8)) ;=> true

  "
  [x] (= (Class/forName "[B")
         (.getClass x)))

(defn atom?
  "Returns `true` if `x` is of type `clojure.lang.Atom`.

    (atom? (atom 0)) ;=> true
  "
  [obj] (instance? clojure.lang.Atom obj))

(defn aref?
  "Returns `true` if `x` is of type `clojure.lang.Ref`.

    (aref? (ref 0)) ;=> true
  "
  [obj]  (instance? clojure.lang.Ref obj))

(defn agent?
  "Returns `true` if `x` is of type `clojure.lang.Agent`.

    (agent? (agent 0)) ;=> true
  "
  [obj] (instance? clojure.lang.Agent obj))

(defn iref?
  "Returns `true` if `x` is of type `clojure.lang.IRef`.

    (iref? (atom 0)) ;=> true
  "
  [obj]  (instance? clojure.lang.IRef obj))

(defn ideref?
  "Returns `true` if `x` is of type `java.lang.IDeref`.

    (ideref? (promise)) ;=> true
  "
  [obj]  (instance? clojure.lang.IDeref obj))

(defn promise?
  "Returns `true` is `x` is a promise

    (promise? (future (inc 1))) ;=> true
  "
  [obj]
  (let [s (str (type obj))]
    (or (.startsWith s "class clojure.core$promise$")
        (.startsWith s "class clojure.core$future_call$"))))

(defn type-checker
  "Returns the checking function associated with `k`

    (type-checker :string)
    ;=> #'clojure.core/string?

    (type-checker :bytes)
    ;=> #'adi.utils/bytes?
   "
  [k]
  (resolve (symbol (str (name k) "?"))))

