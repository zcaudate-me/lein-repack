(ns leiningen.repack
  (:require [leiningen.core.project :as project]
            [leiningen.repack.manifest :as manifest]
            [leiningen.repack.split :as split]
            [leiningen.repack.install :as install]
            [leiningen.repack.deploy :as deploy]
            [leiningen.repack.push :as push]
            [clojure.pprint :as pprint]))

(defn help [project & args]
  (println "\nSub-tasks for repackage are available:\n")
  (println "help               Display this message")
  (println "manifest           Generate the manifest for repacking jars")
  (println "split              Splits the main project into several interim projects")
  (println "clean              Removes the interim folder")
  (println "install            Install all the interim projects to local repo")
  (println "deploy             Deploys all the repackaged jars")
  (println "push               Deployment the old-school way"))

(defn manifest [project]
  (pprint/pprint
   (manifest/create project)))

(defn split [project]
  (split/split project)
  (println "\nYour project files have been split into an interim directory."))

(defn clean [project]
  (split/clean project)
  (println "\nThe interim directory has been successfully deleted."))

(defn install [project]
  (install/install project)
  (println "\nThe repacked jars have been successfully installed."))

(defn deploy [project & [repo]]
  (deploy/deploy project (or repo "clojars"))
  (println "\nThe repacked jars have been successfully deployed."))

(defn push [project]
  (push/push project)
  (println "\nThe repacked jars have been successfully deployed."))

(defn repack [project & [sub & more]]
  (let [project (project/unmerge-profiles project [:default])]
    (condp = sub
      nil        (apply help project more)
      "help"     (apply help project more)
      "manifest" (manifest project)
      "split"    (split project)
      "clean"    (clean project)
      "install"  (install project)
      "deploy"   (apply deploy project more)
      "push"     (push project)
      (apply help project more))))
