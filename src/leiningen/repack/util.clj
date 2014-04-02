(ns leiningen.repack.util
  (:require [clojure.java.io :as io]
            [clojure.string :as string]
            [korra.common :refer [*sep*]]))

(defn delete-file-recursively
  "Delete file f. If it's a directory, recursively delete all its contents.
Raise an exception if any deletion fails unless silently is true."
  [f & [silently]]
  (let [f (io/file f)]
    (if (.isDirectory f)
      (doseq [child (.listFiles f)]
        (delete-file-recursively child silently)))
    (io/delete-file f silently)))

(defn copy-file [rel-path source sink]
  (let [source-file (io/as-file (str source *sep* rel-path))
        sink-file   (io/as-file (str sink *sep* rel-path))]
    (io/make-parents sink-file)
    (io/copy source-file sink-file)))

(defn interim-path [project & args]
  (->> args
       (cons "interim")
       (cons (:target-path project))
       (string/join *sep*)))

(defn all-branch-nodes [manifest]
  (->> (:branches manifest)
       (map (fn [[k m]]
              (-> m
                  (select-keys [:coordinate :dependencies])
                  (assoc :id k))))))

(defn all-branch-deps [manifest]
  (->> (:branches manifest)
       (map (fn [[k m]] (:coordinate m)))
       (set)))

(defn topsort-branch-deps-pass
  [all sl]
  (reduce (fn [out i]
            (if (some all (:dependencies i))
              out
              (conj out i))) [] sl))

(defn topsort-branch-deps [manifest]
  (let [sl  (all-branch-nodes manifest)
        all (all-branch-deps manifest)]
    (loop [all all
           sl  sl
           output []]
      (if-not (or (empty? sl)
                  (empty? all))
        (let [pass (topsort-branch-deps-pass all sl)]
          (recur
           (apply disj all (map :coordinate pass))
           (filter (fn [x] (some #(not= x %) pass) ) sl)
           (conj output pass)))
        output))))
