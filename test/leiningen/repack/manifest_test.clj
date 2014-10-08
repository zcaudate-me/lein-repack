(ns leiningen.repack.manifest-test
  (:use midje.sweet)
  (:require [leiningen.repack.manifest :refer :all]
            [leiningen.repack.graph.external :as external]
            [leiningen.core.project :as project]))

(external/resolve-with-ns
 'korra.common
 (:dependencies
 (-> (project/read "example/repack.advance/project.clj")
     (project/unmerge-profiles [:default])))
 (-> (project/read "example/repack.advance/project.clj")
     (project/unmerge-profiles [:default])))

(-> (create
     (-> (project/read "/Users/Chris/dev/chit/hara/project.clj")
         (project/unmerge-profiles [:default])))
    :branches
    (get "class.reflect"))

(-> (create
     (-> (project/read "example/repack.advance/project.clj")
         (project/unmerge-profiles [:default]))))

(clj-version (project/read "example/repack.advance/project.clj"))
