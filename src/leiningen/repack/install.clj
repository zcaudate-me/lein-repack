(ns leiningen.repack.install
  (:require [leiningen.repack.data.io :as io]
            [leiningen.repack.data.sort :as sort]
            [leiningen.repack.split :as split]
            [leiningen.repack.manifest :as manifest]
            [leiningen.core.project :as project]
            [leiningen.install :as install]))

(defn install [project]
  (split/split project)
  (let [manifest    (manifest/create project)
        subprojects (-> manifest sort/topsort-branch-deps flatten distinct)]
    (doseq [entry subprojects]
      (println "\nInstalling" (:id entry))
      (let [sproject (project/read (io/interim-path project "branches" (:id entry) "project.clj"))]
        (install/install sproject)))

    (println "\nInstalling Root")
    (let [rproject (project/read (io/interim-path project "root" "project.clj"))]
      (install/install rproject))))
