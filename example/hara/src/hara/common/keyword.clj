(ns hara.common.keyword
  (:require [clojure.string :as st]))
            
;; ## Keyword Methods

(defn keyword-str
  "Returns the string representation of the keyword
   without the colon.

    (keyword-str :hello/there)
    ;=> \"hello/there\"
  "
  [k]
  (if (nil? k) "" (#'st/replace-first-char (str k) \: "")))

(defn keyword-join
  "Merges a sequence of keywords into one.

    (keyword-join [:hello :there])
    ;=> :hello/there

    (keyword-join [:a :b :c :d])
    ;=> :a/b/c/d)"
  ([ks] (keyword-join ks "/"))
  ([ks sep]
     (if (empty? ks) nil
         (->> (filter identity ks)
              (map keyword-str)
              (st/join sep)
              keyword))))

(defn keyword-split
  "The opposite of `keyword-join`. Splits a keyword
   by the `/` character into a vector of keys.

    (keyword-split :hello/there)
    ;=> [:hello :there]

    (keyword-split :a/b/c/d)
    ;=> [:a :b :c :d]
  "
  ([k] (keyword-split k #"/"))
  ([k re]
     (if (nil? k) []
         (mapv keyword (st/split (keyword-str k) re)))))

(defn keyword-contains?
  "Returns `true` if the first part of `k` contains `subk`

    (keyword-contains? :a :a)
    ;=> true

    (keyword-contains? :a/b/c :a/b)
    ;=> true
  "
  [k subk]
  (or (= k subk)
      (.startsWith (keyword-str k)
                   (str (keyword-str subk) "/"))))

(defn keyword-nsvec
  "Returns the namespace vector of keyword `k`.

    (keyword-nsvec :hello/there)
    ;=> [:hello]

    (keyword-nsvec :hello/there/again)
    ;=> [:hello :there]
  "
  [k]
  (or (butlast (keyword-split k)) []))

(defn keyword-nsvec?
  "Returns `true` if keyword `k` has the namespace vector `nsv`."
  [k nsv]
  (= nsv (keyword-nsvec k)))

(defn keyword-ns
  "Returns the namespace of `k`.

    (keyword-ns :hello/there)
    ;=> :hello

    (keyword-ns :hello/there/again)
    ;=> :hello/there
  "
  [k]
  (keyword-join (keyword-nsvec k)))

(defn keyword-ns?
  "Returns `true` if keyword `k` has a namespace or
   if `ns` is given, returns `true` if the namespace
   of `k` is equal to `ns`.

    (keyword-ns? :hello)
    ;=> false

    (keyword-ns? :hello/there)
    ;=> true

    (keyword-ns? :hello/there :hello)
    ;=> true
  "
  ([k] (< 0 (.indexOf (str k) "/")))
  ([k ns] (if-let [tkns (keyword-ns k)]
            (= 0 (.indexOf (str k)
                 (str ns "/")))
            (nil? ns))))

(defn keyword-root
  "Returns the namespace root of `k`.

    (keyword-root :hello/there)
    ;=> :hello

    (keyword-root :hello/there/again)
    ;=> :hello
  "
  [k]
  (first (keyword-nsvec k)))

(defn keyword-root?
  "Returns `true` if keyword `k` has the namespace base `nsk`."
  [k nsk]
  (= nsk (keyword-root k)))

(defn keyword-stemvec
  "Returns the stem vector of `k`.

    (keyword-stemvec :hello/there)
    ;=> [:there]

    (keyword-stemvec :hello/there/again)
    ;=> [:there :again]
  "
  [k]
  (rest (keyword-split k)))

(defn keyword-stemvec?
  "Returns `true` if keyword `k` has the stem vector `kv`."
  [k kv]
  (= kv (keyword-stemvec k)))

(defn keyword-stem
  "Returns the steam of `k`.

    (keyword-stem :hello/there)
    ;=> :there

    (keyword-stem :hello/there/again)
    ;=> :there/again
  "
  [k]
  (keyword-join (keyword-stemvec k)))

(defn keyword-stem?
  "Returns `true` if keyword `k` has the stem `kst`."
  [k kst]
  (= kst (keyword-stem k)))

(defn keyword-val
  "Returns the keyword value of the `k`.

    (keyword-val :hello)
    ;=> :hello

    (keyword-val :hello/there)
    ;=> :there"
   [k]
  (last (keyword-split k)))

(defn keyword-val?
  "Returns `true` if the keyword value of `k` is equal
   to `z`."
  [k z]
  (= z (keyword-val k)))