(ns leiningen.repack.deploy
  (:require [leiningen.repack.util :refer :all]
            [leiningen.repack.split :refer [split]]
            [leiningen.repack.install :as install]
            [leiningen.core.project :as project]
            [leiningen.deploy :as deploy]))

(defn deploy [project manifest repo]
  (split project manifest)
  (let [subprojects (flatten (topsort-branch-deps manifest))]
    (doseq [entry subprojects]
      (let [sproject (project/read (interim-path project "branches" (:id entry) "project.clj"))]
        (deploy/deploy sproject repo)))
    (let [rproject (project/read (interim-path project "root" "project.clj"))]
      (deploy/deploy rproject repo))))
