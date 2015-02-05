(ns leiningen.repack.graph.external
  (:require [korra.common :refer [*sep*]]
            [korra.resolve :as resolve]
            [clojure.set :as set]
            [clojure.string :as string]))

(defn to-jar-entry [[type sym]]
  (str (string/join "/" (string/split (str sym) #"\.")) "." (name type)))

(defn resolve-with-ns [x dependencies project]
  (->> dependencies
       (keep #(if (resolve/resolve-with-deps (to-jar-entry x)
                                             % :repositories
                                             ;; korra assumes
                                             ;; repositories is a
                                             ;; map, lein accepts a
                                             ;; collection of pairs
                                             (into {} (:repositories project))) %))
       first))

(defn find-external-imports [filemap i-deps pkg]
  (let [imports     (->> (get filemap pkg)
                         (map :imports)
                         (apply set/union))
        import-deps (->> (get i-deps pkg)
                         (map (fn [dep]
                                (->> (get filemap dep)
                                     (map :exports)
                                     (apply set/union)))))]
    (apply set/difference imports import-deps)))

(defn is-clojure? [coordinate]
  (= (first coordinate) 'org.clojure/clojure))

(defn find-all-external-imports [filemap i-deps project]
  (reduce-kv (fn [i k v]
               (assoc i k
                      (->> (find-external-imports filemap i-deps k)
                           (map #(resolve-with-ns % (:dependencies project) project))
                           (filter (comp not is-clojure?))
                           (set)
                           (#(disj % nil)))))
             {}
             filemap))
