(ns leiningen.repack-test
  (:use midje.sweet)
  (:require [leiningen.repack :refer :all]
            [leiningen.core.project :as project]))

(manifest
 (-> (project/read "example/repack.advance/project.clj")
     (project/unmerge-profiles [:default])))
