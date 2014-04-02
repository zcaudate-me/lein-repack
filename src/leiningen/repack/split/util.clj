(ns leiningen.repack.common
  (:require [clojure.java.io :as io]
            [leiningen.repack.manifest :as manifest]
            [leiningen.jar :as jar]
            [leiningen.install :as install]
            [leiningen.core.project :as project]))

(defn delete-file-recursively
  "Delete file f. If it's a directory, recursively delete all its contents.
Raise an exception if any deletion fails unless silently is true."
  [f & [silently]]
  (let [f (io/file f)]
    (if (.isDirectory f)
      (doseq [child (.listFiles f)]
        (delete-file-recursively child silently)))
    (io/delete-file f silently)))

(defn interim-path [project]
  (str (:target-path project) *sep* "interim"))