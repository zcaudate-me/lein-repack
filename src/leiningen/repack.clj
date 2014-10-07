(ns leiningen.repack
  (:require [leiningen.repack.manifest :as manifest]))

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
    (manifest/create project)))

(defn repack [project & [sub & more]]
  (condp = sub
    nil        (apply help project more)
    ;;"help"     (apply help project more)
    "manifest" (manifest project)
    ;;"split"    (split project)
    ;;"clean"    (clean project)
    ;;"install"  (install project)
    ;;"deploy"   (apply deploy project more)
    ;;"push"     (push project)
    (apply help project more)))
