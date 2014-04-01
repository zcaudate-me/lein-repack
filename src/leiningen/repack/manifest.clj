(ns leiningen.repack.manifest
  (:require [leiningen.core.project :as project]
            [leiningen.repack.classify :as classify]
            [leiningen.repack.graph :as graph]
            [korra.common :refer [*sep*]]
            [korra.resolve :as resolve]))


(defn create-manifest [project]
  (let [project (project/unmerge-profiles project [:default])
        root (first (:source-paths project))
        [root-files branches]  (classify/split-project-files project)
        {clojure true others false} (group-by graph/is-clojure? (:dependencies project))]
    {:root (-> project
               (select-keys [:group :name :version])
               (assoc :dependencies (->> (graph/create-root-dependencies project branches)
                                         (concat clojure)
                                         (vec))
                      :files (mapv (fn [f] (.replace (.getPath f) (str root *sep*) "")) root-files)))
     :branches (let [modules (classify/classify-modules branches)
                     pkg-lu  (graph/create-branch-lookup modules)]
                 (->> modules
                      (map (fn [[k {:keys [package dep-namespaces dep-classes files]}]]
                             [k (-> project
                                    (select-keys [:group :name :version])
                                    (update-in [:name] #(str % "." package))
                                    (assoc :dependencies (->> [project branches pkg-lu
                                                               dep-namespaces dep-classes]
                                                              (apply graph/create-branch-dependencies)
                                                              (second)
                                                              (concat clojure)
                                                              (vec))
                                           :files (mapv (fn [f] (.replace f (str root *sep*) ""))
                                                        files)
                                           :coordinate (graph/create-branch-coordinate project package)))]))
                      (into {})))}))



(comment
  (clojure.pprint/pprint
   (create-manifest (project/read "example/hara/project.clj")))

  (-> (project/read "example/hara/project.clj")
      (project/unmerge-profiles [:default])
      (split-project-files)
      second)


  #_{:root {:group "im.chit"
            :name  "hara"
            :version "1.1.0"
            :dependencies [[org.clojure/clojure "1.5.1"]
                           [im.chit/hara.common "1.1.0"]]
            :files []}
     :branches
     {"common" {:group "im.chit"
                :name "hara.common"
                :version "1.1.0"
                :dependencies [[org.clojure/clojure "1.5.1"]]}}

     })
