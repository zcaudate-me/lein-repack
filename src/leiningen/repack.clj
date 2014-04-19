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
  (clojure.pprint/pprint
    (manifest/create-manifest project)))

(defn split [project]
  (split/split project
               (manifest/create-manifest project))
  (println "Your project files have been split into an interim directory."))

(defn clean [project]
  (split/clean project)
  (println "The interim directory has been successfully deleted."))

(defn install [project]
  (install/install project
                   (manifest/create-manifest project))
  (println "The repacked jars have been successfully istalled."))

(defn deploy [project & [repo]]
  (deploy/deploy project
                 (manifest/create-manifest project)
                 (or repo "clojars"))
  (println "The repacked jars have been successfully deployed."))

(defn push [project]
  (push/push project
                 (manifest/create-manifest project))
  (println "The repacked jars have been successfully deployed."))
  
(defn repack [project & [sub & more]]
  (condp = sub
    nil        (apply help project more)
    "help"     (apply help project more)
    "manifest" (manifest project)
    "split"    (split project)
    "clean"    (clean project)
    "install"  (install project)
    "deploy"   (apply deploy project more)
    "push"     (push project)
    (apply help project more)))
