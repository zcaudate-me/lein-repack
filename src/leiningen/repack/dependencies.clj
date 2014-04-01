(ns leiningen.repack.dependencies
  (:require [leiningen.core.project :as project]))

(defn
  (leiningen.core.project/unmerge-profiles
   (leiningen.core.project/read "project.clj") [:default]))
