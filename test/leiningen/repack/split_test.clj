(ns leiningen.repack.split-test
  (:use midje.sweet)
  (:require [leiningen.repack.split :refer :all]
            [leiningen.repack.manifest :as manifest]
            [leiningen.repack.analyser :as analyser]
            [leiningen.core.project :as project]))

(comment
  (let [project (-> (project/read "example/repack.simple/project.clj")
                  (project/unmerge-profiles [:default]))]
    (println (:jar-exclusions project))
    (manifest/create project))

  (let [project (-> (project/read "example/repack.advance/project.clj")
                  (project/unmerge-profiles [:default]))]
    (split project))

  (let [project (-> (project/read "example/repack.simple/project.clj")
                  (project/unmerge-profiles [:default]))]
    (split project))

  (let [project (-> (project/read "../hara/project.clj")
                    (project/unmerge-profiles [:default]))]
    (split project))

  (let [project (-> (project/read "../vinyasa/project.clj")
                    (project/unmerge-profiles [:default]))]
    (manifest/create project))

  (analyser/file-info "../vinyasa/src/vinyasa/maven/jar.clj")
  => '{:exports #{[:clj vinyasa.maven.jar]},
       :imports #{[:clj version-clj.core]
                  [:class clojure.lang.Symbol]
                  [:clj clojure.string]
                  [:clj clojure.java.io]
                  [:clj vinyasa.maven.file]}})
