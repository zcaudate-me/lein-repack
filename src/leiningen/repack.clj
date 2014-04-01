(ns leiningen.repack
  (:require [leiningen.core.project :as project]
            [leiningen.repack.manifest :as manifest]
            [leiningen.repack.pom :as pom]
            [leiningen.repack.jar :as jar]))

(defn help [project]
  (println "\nSub-tasks for repackage are available:\n")
  (println "help               Display this message")
  (println "manifest           Generate the manifest for repacking jars")
  (println "install            Installs to local repo")
  (println "jar                Creates repackages jars")
  (println "pom                Creates repackaged poms")
  (println "deply              Deploys repackaged jars"))

(defn manifest [project]
  (manifest/create-manifest project))

(defn project [project])

(defn install [project])

(defn jar [project]
  (let [manifest (manifest/create-manifest project)]
    (jar/create-jars project manifest)))

(defn pom [project]
  (let [manifest (manifest/create-manifest project)]
    (pom/create-poms project manifest)))

(defn push [])


(defn repack [project & [sub & more]]
  (condp = sub
    nil        (apply help project more)
    "help"     (apply help project more)
    "manifest" (manifest project)
    "jar"      (jar project)
    "pom"      (pom project)
    "install"  (apply install project more)
    "push"     (apply push project more)))


(repack nil)
