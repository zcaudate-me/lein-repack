(ns leiningen.repack.manifest.graph
  (:require [korra.common :refer [*sep*]]
            [korra.resolve :as resolve]))

(defn resolve-with-ns [x dependencies project]
  (or (->> dependencies
           (map #(if (resolve/resolve-with-deps x % :repositories
                                                ;; korra assumes
                                                ;; repositories is a
                                                ;; map, lein accepts a
                                                ;; collection of pairs
                                                (into {} (:repositories project))) %))
           (filter identity)
           first)
      (if (class? x)
        (let [nms (symbol (.getName (.getPackage String)))]
          (resolve-with-ns nms dependencies)))))

(defn is-clojure? [coordinate]
  (= (first coordinate) 'org.clojure/clojure))

(defn create-branch-lookup [branches]
    (->> branches
         (map (fn [e]
                (let [[k v] e]
                  (zipmap (:namespaces v)
                          (repeat k)))))
         (apply merge)))

(defn create-branch-coordinate [{:keys [version name group] :as project} package]
  (let [name-fn (if-let [name-fn-form (get-in project [:repack :name-fn])] (eval name-fn-form) #(str %1 "." %2))]
   [(symbol (str group *sep* (name-fn name package))) version]))

(defn create-root-dependencies [project branches]
  (mapv (fn [k] (create-branch-coordinate project k))
        (keys branches)))

(defn create-branch-dependencies [{:keys [version name group dependencies] :as project}
                                  branches lu dep-namespaces dep-classes]
  (let [own-deps (->> (map lu dep-namespaces)
                      (distinct)
                      (filter identity)
                      (map #(create-branch-coordinate project %)))
        ext-deps (->> (concat dep-namespaces dep-classes)
                      (map #(resolve-with-ns % dependencies project))
                      (distinct)
                      (filter identity)
                      (filter (comp not is-clojure?)))]
    [name (concat own-deps ext-deps)]))




(comment
  (require '[leiningen.core.project :as project])
  (require '[leiningen.repack.manifest :as manifest])

  (resolve/resolve-coordinates 'cemerick.pomegranate '[com.cemerick/pomegranate "0.2.0"])
  (resolve/resolve-with-deps 'cemerick.pomegranate '[com.cemerick/pomegranate "0.2.0"])
  (resolve-with-ns 'cemerick.pomegranate '[[com.cemerick/pomegranate "0.2.0"]]
                   (project/read "example/hara/project.clj"))
 )
