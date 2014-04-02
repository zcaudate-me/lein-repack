(ns leiningen.repack.split
  (:require [korra.common :refer [*sep*]]
            [clojure.java.io :as io]
            [leiningen.repack.split.project-file :as prj]
            [leiningen.repack.util :refer :all]
            [leiningen.core.project :as project]))

(defn clean [project]
  (let [interim-path (interim-path project)
        interim-dir  (io/file interim-path)]
    (if (.exists interim-dir)
      (delete-file-recursively interim-dir))))

(defn create-scaffold [project manifest]
  (let [interim-path (interim-path project)]
    (do (io/make-parents interim-path)
        (.mkdir (io/file interim-path))
        (.mkdir (io/file (str interim-path *sep* "root")))
        (.mkdir (io/file (str interim-path *sep* "branches"))))
    (doseq [branch (-> manifest :branches keys)]
      (.mkdir (io/file (str interim-path *sep* "branches" *sep* branch))))))

(defn create-source-files [project manifest]
  (let [source-path (-> project :source-paths first)]
    (doseq [root-file (-> manifest :root :files)]
      (copy-file root-file source-path (interim-path project "root" "src")))
    (doseq [branch (-> manifest :branches keys)]
      (doseq [branch-file (-> manifest :branches (get branch) :files)]
        (copy-file branch-file source-path (interim-path project "branches" branch "src"))))))

(defn create-project-clj-files [project manifest]
  (spit (interim-path project "root" "project.clj")
        (prj/root-project-string project manifest))
  (doseq [branch (-> manifest :branches keys)]
    (spit (interim-path project "branches" branch "project.clj")
          (prj/branch-project-string project manifest branch))))

(defn split [project manifest]
  (clean project)
  (create-scaffold project manifest)
  (create-source-files project manifest)
  (create-project-clj-files project manifest))
