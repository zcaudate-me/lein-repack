(ns leiningen.repack
  (:require [leiningen.core.project :as project]
            [leiningen.repack.manifest :as manifest]))


(defn help [project]
  (println "\nSub-tasks for repackage are available:\n")
  (println "help               Display this message")
  (println "manifest           Generate the manifest for repacking jars")
  (println "install            Installs to local repo")
  (println "jar                Creates repackages jars")
  (println "pom                Creates repackaged poms")
  (println "deply              Deploys repackaged jars"))

(defn manifest [project])

(defn install [])

(defn jar [])

(defn pom [])

(defn push [])


(defn repack [project & [sub & more]]
  (condp = sub
    nil        (apply help project more)
    "help"     (apply help project more)
    "manifest" (apply manifest project more)
    "install"  (apply install project more)
    "jar"      (apply jar project more)
    "pom"      (apply pom project more)
    "push"     (apply push project more)))


(repack nil)
