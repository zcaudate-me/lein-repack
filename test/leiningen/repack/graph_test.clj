(ns leiningen.repack.graph-test
  (:use midje.sweet)
  (:require [clojure.set :as set]  ;;[leiningen.repack.graph :refer :all]
            [hara.sort :as sort]
            [leiningen.repack.manifest.common :as common]
            [leiningen.repack.manifest.source :as source]
            [leiningen.repack.analyser.java]
            [leiningen.repack.analyser.clj]
            [leiningen.repack.analyser.cljs]))

(def a (merge-with set/union
                   (common/build-manifest "example/repack.advance"
                                          {:subpackage "jvm"
                                           :path "java/im/chit/repack"
                                           :distribute {"common" #{"common"}
                                                        "web"    #{"web"}}})

                   (source/build-manifest "example/repack.advance"
                                          {:levels 2
                                           :path "src/clj"
                                           :package #{"web"}})))

(defn find-module-dependencies [sym symv tally]
  (reduce-kv (fn [i k v]
               (if (empty? (set/intersection (:imports symv) (:exports v)))
                 i
                 (conj i k)))
             #{}
             (dissoc tally sym)))

(defn find-all-module-dependencies [manifest]
  (let [tally (reduce-kv (fn [i k v]
                           (assoc i k {:imports (apply set/union (map :imports v))
                                       :exports (apply set/union (map :exports v))}))
                         {}
                         manifest)]
    (reduce-kv (fn [i k v]
                 (assoc i k (find-module-dependencies k v tally)))
               {}
               tally)))




(sort/topological-sort (find-all-module-dependencies a))

(let [all-info  (get a "web")
      imports   (apply set/union (map :imports all-info))
      exports   (apply set/union (map :exports all-info))
      ;;selfref (set/intersection imports exports)
      ]
  {:imports imports :exports exports}
  #_{:imports (set/difference imports selfref)
   :exports (set/difference exports selfref)})

(defn package-referencs
  (map :imports )
  (apply ))

(defn package-exports)
