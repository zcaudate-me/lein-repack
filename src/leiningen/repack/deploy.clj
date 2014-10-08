(ns leiningen.repack.deploy
  (:require [leiningen.core.project :as project]
            [leiningen.repack.data.sort :as sort]
            [leiningen.repack.data.io :as io]
            [leiningen.repack.split :as split]
            [leiningen.repack.manifest :as manifest]
            [leiningen.repack.install :as install]
            [leiningen.deploy :as deploy]))

(defn deploy [project repo]
  (split/split project)
  (let [manifest (manifest/create project)
        subprojects (-> manifest sort/topsort-branch-deps flatten distinct)]
    (doseq [entry subprojects]
      (let [sproject (project/read (io/interim-path project "branches" (:id entry) "project.clj"))]
        (deploy/deploy sproject repo)))
    (let [rproject (project/read (io/interim-path project "root" "project.clj"))]
      (deploy/deploy rproject repo))))
