(ns leiningen.repack.split-test
  (:use midje.sweet)
  (:require [leiningen.repack.split :refer :all]
            [leiningen.core.project :as project]))

(let [project (-> (project/read "example/repack.advance/project.clj")
                  (project/unmerge-profiles [:default]))]
   (split project))
