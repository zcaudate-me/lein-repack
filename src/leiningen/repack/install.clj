(ns leiningen.repack.install
  (:require [leiningen.repack.util :refer :all]
            [leiningen.repack.split :refer [split]]
            [leiningen.core.project :as project]
            [leiningen.install :as install]))

(defn install [project manifest]
  (split project manifest)
  (let [subprojects (flatten (topsort-branch-deps manifest))]
    (doseq [entry subprojects]
      (let [sproject (project/read (interim-path project "branches" (:id entry) "project.clj"))]
        (install/install sproject)))
    (let [rproject (project/read (interim-path project "root" "project.clj"))]
      (install/install rproject))))
