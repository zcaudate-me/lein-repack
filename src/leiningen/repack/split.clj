(ns leiningen.repack.split
  (:require [clojure.java.io :as io]
            [clojure.string :as string]
            [korra.common :refer [*sep*]]
            [leiningen.repack.split.rewrite :as rewrite]
            [leiningen.repack.manifest :as manifest]
            [leiningen.repack.data.sort :as sort]
            [leiningen.repack.data.io :refer [delete-file-recursively copy-file interim-path]]
            [leiningen.core.project :as project]))

(defn clean [project]
  (let [interim (interim-path project)
        interim-dir  (io/file interim)]
    (if (.exists interim-dir)
      (delete-file-recursively interim-dir))))

(defn create-scaffold [project manifest]
  (let [interim (interim-path project)]
    (do (io/make-parents interim)
        (.mkdir (io/file interim))
        (.mkdir (io/file (str interim *sep* "root")))
        (.mkdir (io/file (str interim *sep* "branches"))))
    (doseq [branch (-> manifest :branches keys)]
      (.mkdir (io/file (str interim *sep* "branches" *sep* branch))))))

(defn copy-files [files source target]
  (doseq [f files]
    (copy-file f source target)))

(defn create-files [project manifest]
  (let [interim (interim-path project)]
    (copy-files (-> manifest :root :files) (io/file (:root project)) (io/file interim "root"))
    (doseq [branch (-> manifest :branches keys)]
      (copy-files (-> manifest :branches (get branch) :files)
                  (io/file (:root project))
                  (io/file interim "branches" branch)))))

(defn create-project-clj-files [project manifest]
  (spit (interim-path project "root" "project.clj")
        (rewrite/root-project-string project manifest))
  (doseq [branch (-> manifest :branches keys)]
    (spit (interim-path project "branches" branch "project.clj")
          (rewrite/branch-project-string project manifest branch))))

(defn split [project]
  (let [manifest (manifest/create project)]
    (clean project)
    (create-scaffold project manifest)
    (create-files project manifest)
    (create-project-clj-files project manifest)
    (println "\nAll Submodules:")
    (println (->> manifest sort/topsort-branch-deps
                  flatten
                  distinct
                  (map :id)))))
                  
(comment
  (require '[leiningen.repack.manifest :as manifest])
  (def project (project/read "example/hara/project.clj"))
  (def manifest (manifest/create-manifest project))
  (clean project)
  (create-scaffold project manifest)
  (create-source-files project manifest)
  (create-project-clj-files project manifest))
