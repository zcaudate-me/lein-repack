(ns leiningen.repack.manifest
  (:require [leiningen.core.project :as project]
            [leiningen.repack.classify :as classify]
            [korra.common :refer [*sep*]]
            [korra.resolve :as resolve]))

(defn split-project-files [project]
  (let [opts (:repack project)]
    (classify/split-project-files
     (first (:source-paths project))
     (-> (or (:root opts)
             (:name project))
         (str)
         (.replaceAll "\\." *sep*))
     (or (:level opts) 1)
     (or (:exclude opts) []))))

(defn is-clojure? [coordinate]
  (= (first coordinate) 'org.clojure/clojure))

(defn create-branch-coordinate [{:keys [version name group]} package]
  [(symbol (str group *sep* name "." package)) version])

(defn create-root-dependencies [project branches]
  (mapv (fn [k] (create-branch-coordinate project k))
        (keys branches)))

(defn resolve-with-ns [x dependencies]
  (or (->> dependencies
           (map #(if (resolve/resolve-with-deps x %) %))
           first)
      (if (class? x)
        (let [nms (symbol (.getName (.getPackage String)))]
          (resolve-with-ns nms dependencies)))))

(defn create-branch-dependencies [{:keys [version name group dependencies] :as project}
                                  branches pkg-lu dep-namespaces dep-classes]
  (let [own-deps (->> (map pkg-lu dep-namespaces)
                      (distinct)
                      (filter identity)
                      (map #(create-branch-coordinate project %)))
        ext-deps (->> (concat dep-namespaces dep-classes)
                      (map #(resolve-with-ns % dependencies))
                      (distinct)
                      (filter identity)
                      (filter (comp not is-clojure?)))]
    (concat own-deps ext-deps)))

(defn create-manifest [project]
  (let [project (project/unmerge-profiles project [:default])
        root (first (:source-paths project))
        [root-files branches]  (split-project-files project)
        {clojure true others false} (group-by is-clojure? (:dependencies project))]
    {:root (-> project
               (select-keys [:group :name :version])
               (assoc :dependencies (->> (create-root-dependencies project branches)
                                         (concat clojure)
                                         (vec))
                      :files (mapv (fn [f] (.replace (.getPath f) (str root *sep*) "")) root-files)))
     :branches (let [modules (classify/classify-modules branches)
                     pkg-lu  (classify/create-package-lookup modules)]
                 (->> modules
                      (map (fn [[k {:keys [package dep-namespaces dep-classes files]}]]
                             [k (-> project
                                    (select-keys [:group :name :version])
                                    (update-in [:name] #(str % "." package))
                                    (assoc :dependencies (->> [project branches pkg-lu
                                                               dep-namespaces dep-classes]
                                                              (apply create-branch-dependencies)
                                                              (concat clojure)
                                                              (vec))
                                           :files (mapv (fn [f] (.replace f (str root *sep*) ""))
                                                        files)))]))
                      (into {})))}))


(comment
  (clojure.pprint/pprint
   (create-manifest (project/read "example/hara/project.clj")))



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
