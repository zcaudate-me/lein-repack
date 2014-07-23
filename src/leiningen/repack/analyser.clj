(ns leiningen.repack.analyser
  (:require [clojure.java.io :as io]
            [clojure.string :as string]
            [leiningen.repack.analyser
             clj cljs java [common :as analyser]]
            [korra.common :refer [*sep*]]))

(defrecord BatchInfo []
  Object
  (toString [this]
    this))

(defmethod print-method BatchInfo [v w]
  (.write w (str v)))

(defn name->path [name]
  (-> (str name)
      (.replaceAll "\\." *sep*)
      (.replaceAll "-" "_")))

(defn filter-sources [dir re]
  (filter (fn [f] (re-find re (.getName f)))
          (file-seq (io/file dir))))

(def re-clj  #"^(.*)\.clj[sx]?$")
(def re-java #"^(.*)\.java$")

(defn is-submodule-file? [file root-dir base excludes]
  (let [base-path (name->path base)
        exclude-paths (map name->path excludes)
        file-path (.getAbsolutePath file)]
    (cond (some #(.startsWith file-path
                              (string/join *sep* [root-dir base-path %]))
                exclude-paths)
          false

          (.startsWith file-path
                       (string/join *sep* [root-dir base-path]))
          true

          :else false)))

(defn classify-file [file root-path base level]
  (let [base-path (name->path base)
        file-path (.getAbsolutePath file)]
    (-> (subs file-path (+ 2 (count root-path) (count base-path)))
        (->> (re-find re-clj))
        (second)
        (string/split (re-pattern *sep*))
        (->> (take level)
             (string/join ".")))))

(defn split-project-files [root-dir base level excludes re-pattern]
  (let [files (->> (filter-sources root-dir re-pattern)
                   (group-by #(submodule-file?
                               % root-dir base excludes)))
        parent-files (vec (get files false))
        module-files (->> (get files true)
                          (group-by #(classify-file % root-dir base level))
                          (reduce-kv (fn [m k v]
                                       (assoc m k (mapv analyser/file-info v)))
                                     {}))]
    [parent-files module-files]))

(defn split-clojure-files
  ([project]
     (let [opts (:repack project)]
       (split-project-files
        (first (:source-paths project))
        (-> (or (:root opts)
                (:name project))
            (str)
            (name->path))
        (or (:levels opts) 1)
        (or (:exclude opts) [])
        re-clj))))

(defn split-java-files
  ([project]
     (let [opts (:repack project)
           java-dir (first (:java-source-paths project))]
       (if (and java-dir
                (-> opts :java))
         (split-project-files
          java-dir
          (-> (or (-> opts :java :root)
                  (-> (str (:group project)
                           "."
                           (:name project))))
              (name->path))
          (or (:levels opts) 1)
          (or (:exclude opts) [])
          re-java)
         [[] {}]))))

(comment
  (require '[leiningen.core.project :as project])
  (split-clojure-files (project/read "example/hara/project.clj"))
  (split-java-files (project/read "example/hara/project.clj"))


  (:source-paths (project/read "project.clj"))
  (:java-source-paths (project/read "project.clj"))

  )
