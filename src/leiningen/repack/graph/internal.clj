(ns leiningen.repack.graph.internal
  (:require [clojure.set :as set]))

(defn find-module-dependencies [sym symv tally]
  (reduce-kv (fn [i k v]
               (if (empty? (set/intersection (:imports symv) (:exports v)))
                 i
                 (conj i k)))
             #{}
             (dissoc tally sym)))

(defn find-all-module-dependencies [filemap]
  (let [tally (reduce-kv (fn [i k v]
                           (assoc i k {:imports (apply set/union (map :imports v))
                                       :exports (apply set/union (map :exports v))}))
                         {}
                         filemap)]
    (reduce-kv (fn [i k v]
                 (assoc i k (find-module-dependencies k v tally)))
               {}
               tally)))

(defn resource-dependencies [cfgs]
  (->> (filter #(and (:subpackage %)
                     (:dependents %)) cfgs)
       (map (fn [{pkg  :subpackage
                 deps :dependents}]
              (zipmap deps (repeat #{pkg}))))
       (apply merge-with set/union)))
