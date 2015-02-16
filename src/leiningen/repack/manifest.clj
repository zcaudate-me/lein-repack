(ns leiningen.repack.manifest
  (:require [clojure.java.io :as io]
            [clojure.set :as set]
            [leiningen.core.project :as project]
            [leiningen.repack.graph
             [internal :as internal]
             [external :as external]]
            [leiningen.repack.analyser :as analyser]
            [leiningen.repack.analyser [java clj cljs]]
            [leiningen.repack.manifest [common :refer [build-filemap]] source]
            [leiningen.repack.data.file-info :refer [map->FileInfo]]
            [leiningen.repack.data.util :as util]))

(def ^:dynamic *default-config*
  [{:type :clojure
    :path "src"
    :levels 1}])

(defn clj-version [project]
  (->> (:dependencies project)
       (filter #(= (first %) 'org.clojure/clojure))
       (first)
       (second)))

(defn create-root-entry [project branches]
  (-> (select-keys project [:name :group :version :dependencies])
      (update-in [:dependencies] #(apply conj (vec %) (map :coordinate branches)))
      (assoc :files [])))

(defn create-branch-entry [project filemap i-deps ex-deps pkg]
  (let [{:keys [group version] base :name} project
        name   (str group "/" base "." pkg)]
    {:coordinate [(symbol name) version]
     :files (mapv :path (get filemap pkg))
     :dependencies (->> (get i-deps pkg)
                        (map (fn [k]
                               [(symbol (str group "/" base "." k)) version]))
                        (concat [['org.clojure/clojure (clj-version project)]]
                                (filter identity (get ex-deps pkg)))
                        vec)
     :version version
     :name name
     :group group}))

(defn create [project]
  (let [cfgs (or (:repack project) *default-config*)
        cfgs (if (vector? cfgs) cfgs [cfgs])
        filemap   (->> cfgs
                       (map #(build-filemap (:root project)
                                            (merge (select-keys project [:jar-exclusions]) %)))
                       (apply merge-with set/union))
        i-deps (merge-with set/union
                           (internal/resource-dependencies cfgs)
                           (internal/find-all-module-dependencies filemap))
        ex-deps  (external/find-all-external-imports filemap i-deps project)
        ks       (keys filemap)
        branches (mapv #(create-branch-entry project filemap i-deps ex-deps %) ks)]
    {:root (create-root-entry project branches)
     :branches (zipmap ks branches)}))
