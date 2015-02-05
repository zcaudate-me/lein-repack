(ns leiningen.repack.graph.external-test
  (:use midje.sweet)
  (:require [clojure.set :as set]
            [leiningen.repack.graph
             [internal :as internal]
             [external :refer :all]]
            [leiningen.core.project :as project]
            [leiningen.repack.manifest [common :refer [build-filemap]] source]))

(def ^:dynamic *project*
  (-> (project/read "example/repack.advance/project.clj")
      (project/unmerge-profiles [:default])))

(def ^:dynamic *filemap*
  (->> (:repack *project*)
       (map #(build-filemap (:root *project*) %))
       (apply merge-with set/union)))

(def ^:dynamic *i-deps*
  (merge-with set/union
              (internal/resource-dependencies (:repack *project*))
              (internal/find-all-module-dependencies *filemap*)))

^{:refer leiningen.repack.graph.external/to-jar-entry :added "0.1.5"}
(fact "constructs a jar entry"

  (to-jar-entry '[:clj korra.common])
  => "korra/common.clj"

  (to-jar-entry '[:cljs korra.common])
  => "korra/common.cljs")


^{:refer leiningen.repack.graph.external/resolve-with-ns :added "0.1.5"}
(fact "finds the maven coordinate for a given namespace"
  (resolve-with-ns '[:clj korra.common]
                   (:dependencies *project*)
                   *project*)
  => '[im.chit/korra "0.1.2"])

^{:refer leiningen.repack.graph.external/find-external-imports :added "0.1.5"}
(fact "finds external imports for a given submodule"
  (find-external-imports *filemap* *i-deps* "core")
  => '#{[:clj korra.common]})

^{:refer leiningen.repack.graph.external/find-all-external-imports :added "0.1.5"}
(fact  "finds external imports for the filemap"
  (find-all-external-imports *filemap* *i-deps* *project*)
  => {"web" #{}, "util.data" #{}, "util.array" #{}, "jvm" #{} "core" '#{[im.chit/korra "0.1.2"]}, "common" #{}, "resources" #{}})
