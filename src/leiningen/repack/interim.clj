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

(defn copy-source-files [project manifest]
  (let [source-path (-> project :source-paths first)
        interim-path (interim-path project)]
    (doseq [root-file (-> manifest :root :files)]
      (copy-file root-file source-path (str interim-path *sep* "root" *sep* "src")))
    (doseq [branch (-> manifest :branches keys)]
      (doseq [branch-file (-> manifest :branches (get branch) :files)]
        (copy-file branch-file source-path (str interim-path *sep* "branches" *sep* branch *sep* "src"))))))

(defn interim-project-files [project manifest]
  (interim-project-skeleton project manifest)
  (root-project-file project manifest)
  (doseq [branch (-> manifest :branches keys)]
    (branch-project-file project manifest branch))

  (copy-source-files project manifest))


(comment
  (flatten (sort-branch-deps
            (manifest/create-manifest
             (project/read "example/hara/project.clj"))))

  [[{:id "import", :dependencies [[org.clojure/clojure "1.5.1"]], :coordinate [im.chit/hara.import "1.1.0-SNAPSHOT"]}]
   [{:id "common", :dependencies [[org.clojure/clojure "1.5.1"] [im.chit/hara.import "1.1.0-SNAPSHOT"]],
     :coordinate [im.chit/hara.common "1.1.0-SNAPSHOT"]}]
   [{:id "checkers", :dependencies [[org.clojure/clojure "1.5.1"] [im.chit/hara.common "1.1.0-SNAPSHOT"]],
     :coordinate [im.chit/hara.checkers "1.1.0-SNAPSHOT"]}
    {:id "collection", :dependencies [[org.clojure/clojure "1.5.1"] [im.chit/hara.common "1.1.0-SNAPSHOT"]],
     :coordinate [im.chit/hara.collection "1.1.0-SNAPSHOT"]}
    {:id "state", :dependencies [[org.clojure/clojure "1.5.1"] [im.chit/hara.common "1.1.0-SNAPSHOT"]],
     :coordinate [im.chit/hara.state "1.1.0-SNAPSHOT"]}]]

  (create-branch-steps
   (manifest/create-manifest
    (project/read "example/hara/project.clj")))

  (all-branch-deps
   (manifest/create-manifest
    (project/read "example/hara/project.clj"))))

(defn create-interim-projects [project manifest])

(interim-project-files (project/read "example/hara/project.clj")
                       (manifest/create-manifest
                           (project/read "example/hara/project.clj")))
#_(copy-source-files
 (project/read "example/hara/project.clj")
                       (manifest/create-manifest
                           (project/read "example/hara/project.clj")))




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
