(ns leiningen.repack.graph.internal-test
  (:use midje.sweet)
  (:require [leiningen.repack.graph.internal :refer :all]
            [leiningen.core.project :as project]
            [leiningen.repack.manifest
             [common :refer [build-manifest]] source]
            [leiningen.repack.analyser java clj cljs]))

(def ^:dynamic *config*
  (-> (project/read "example/repack.advance/project.clj")
      (project/unmerge-profiles [:default])
      :repack))

(def ^:dynamic *files*
  (->> *config*
       (map #(build-manifest "example/repack.advance" %))
       (apply merge-with set/union)))

^{:refer leiningen.repack.graph.internal/find-all-module-dependencies :added "0.1.5"}
(fact "finds all internal module dependencies through analysis of :imports and :exports"

  (find-all-module-dependencies *files*)
  => {"resources" #{},
      "common" #{},
      "core" #{},
      "util.array" #{},
      "util.data" #{},
      "web" #{"core" "util.array"}})

^{:refer leiningen.repack.graph.internal/resource-dependencies :added "0.1.5"}
(fact "looks at the config to see if there are any explicitly stated
  dependencies. Used for packaging the resources folder that may be
  referenced by different modules after repack"

  (resource-dependencies config)
  => {"core" #{"resources"}})
