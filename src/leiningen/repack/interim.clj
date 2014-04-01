(ns leiningen.repack.interim
  (:require [rewrite-clj.zip :as z]
            [korra.common :refer [*sep*]]
            [clojure.java.io :as io]
            [leiningen.repack.manifest :as manifest]
            [leiningen.jar :as jar]
            [leiningen.install :as install]
            [leiningen.core.project :as project]))

(defn delete-file-recursively
  "Delete file f. If it's a directory, recursively delete all its contents.
Raise an exception if any deletion fails unless silently is true."
  [f & [silently]]
  (let [f (io/file f)]
    (if (.isDirectory f)
      (doseq [child (.listFiles f)]
        (delete-file-recursively child silently)))
    (io/delete-file f silently)))

(defn interim-path [project]
  (str (:target-path project) *sep* "interim"))

(defn interim-project-skeleton [project manifest]
  (let [interim-path (interim-path project)
        interim-dir  (io/file interim-path)
        _            (if (.exists interim-dir)
                       (delete-file-recursively interim-dir))]
    (do (io/make-parents interim-path)
        (.mkdir (io/file interim-path))
        (.mkdir (io/file (str interim-path *sep* "root")))
        (.mkdir (io/file (str interim-path *sep* "branches"))))
    (doseq [branch (-> manifest :branches keys)]
      (.mkdir (io/file (str interim-path *sep* "branches" *sep* branch))))))


(defn project-zip [project]
  (-> (z/of-file (str (:root project) *sep* "project.clj"))
      (z/find-value z/next 'defproject)))

(defn replace-project-value [zipper key value]
  (-> zipper
      (z/find-value key)
      (z/right)
      (z/replace value)
      (z/up)
      (z/find-value z/next 'defproject)))

(defn update-project-value [zipper key f]
  (let [pos (-> zipper
                (z/find-value key)
                (z/right))
        val (z/sexpr pos)]
    (-> pos
        (z/replace (f val))
        (z/up)
        (z/find-value z/next 'defproject))))

(defn remove-project-key [zipper key]
  (-> zipper
      (z/find-value key)
      (z/remove*)
      (z/right)
      (z/remove*)
      (z/up)
      (z/find-value z/next 'defproject)))

(defn root-project-file [project manifest]
  (-> (project-zip project)
      (replace-project-value :dependencies
                             (-> manifest :root :dependencies))
      (remove-project-key :profiles)
      (remove-project-key :repack)
      z/print-root
      with-out-str
      (->> (spit (str (interim-path project) *sep* "root" *sep* "project.clj")))))

(defn branch-project-file [project manifest name]
  (-> (project-zip project)
      (update-project-value 'defproject (fn [x] (symbol (str x "." name))))
      (update-project-value :description
                            (fn [x] (or (-> manifest :branches (get name) :description) x)))
      (replace-project-value :dependencies
                             (-> manifest :branches (get name) :dependencies))
      (remove-project-key :profiles)
      (remove-project-key :repack)
      z/print-root
      with-out-str
      (->> (spit (str (interim-path project) *sep* "branches" *sep* name *sep* "project.clj")))))

(defn branch-project-files [project manifest]
  (doseq [branch (-> manifest :branches keys)]
    (branch-project-file project manifest branch)))

(defn interim-project-files [project manifest])


(defn create-interim-projects [project manifest])

(interim-project-skeleton (project/read "example/hara/project.clj")
                          (manifest/create-manifest
                           (project/read "example/hara/project.clj")))

(root-project-file (project/read "example/hara/project.clj")
                   (manifest/create-manifest
                    (project/read "example/hara/project.clj")))

(branch-project-files (project/read "example/hara/project.clj")
                   (manifest/create-manifest
                    (project/read "example/hara/project.clj"))
                   )

#_(spit "example/hara/target/interim/root/pom.xml"
      (leiningen.pom/make-pom (project/read "example/hara/target/interim/root/project.clj")))

#_(leiningen.install/install (project/read "example/hara/target/interim/root/project.clj"))


(comment

  (:target-path (project/read "example/hara/project.clj"))
  => "/Users/Chris/dev/chit/lein-repack/example/hara/target"

  (defn create-project )


  (def prj-map (z/find-value data z/next 'defproject))
  (def descr (-> prj-map (z/find-value 'defproject) z/right z/sexpr))
  (def descr (-> prj-map (z/find-value :description) z/right))

  (-> prj-map
      (z/find-value 'defproject)
      z/right
      (z/replace 'lein-repack.hello)
      (z/find-value :description)
      z/right
      (z/replace "My first Project.")
      z/print-root)
  (with-out-str
    (-> descr
        (z/replace "My first Project.")) z/print-root)
)
