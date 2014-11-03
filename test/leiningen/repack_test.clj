(ns leiningen.repack-test
  (:use midje.sweet)
  (:require [leiningen.repack :refer :all]
            [leiningen.repack.manifest :as manifest]
            [leiningen.core.project :as project]))

(comment

  (manifest/create
   (-> (project/read "example/repack.advance/project.clj")
       (project/unmerge-profiles [:default])))

  (manifest/create
   (-> (project/read "/Users/Chris/dev/buffer/system/project.clj")
       (project/unmerge-profiles [:default])))

  
  (split
   (-> (project/read "/Users/Chris/dev/chit/hara/project.clj")
       (project/unmerge-profiles [:default])))

  (install
   (-> (project/read "/Users/Chris/dev/chit/hara/project.clj")
       (project/unmerge-profiles [:default])
       )))
