(ns leiningen.repack.graph
  (:require [leiningen.repack.classify :as classify]
            [korra.common :refer [*sep*]]
            [korra.resolve :as resolve]))

(defn resolve-with-ns [x dependencies]
  (or (->> dependencies
           (map #(if (resolve/resolve-with-deps x %) %))
           first)
      (if (class? x)
        (let [nms (symbol (.getName (.getPackage String)))]
          (resolve-with-ns nms dependencies)))))

(defn is-clojure? [coordinate]
  (= (first coordinate) 'org.clojure/clojure))

(defn create-branch-lookup [branches]
    (->> branches
         (map #(let [[k v] %]
                 (zipmap (:namespaces v)
                         (repeat k))))
         (apply merge)))

(defn create-branch-coordinate [{:keys [version name group]} package]
  [(symbol (str group *sep* name "." package)) version])

(defn create-root-dependencies [project branches]
  (mapv (fn [k] (create-branch-coordinate project k))
        (keys branches)))

(defn create-branch-dependencies [{:keys [version name group dependencies] :as project}
                                  branches lu dep-namespaces dep-classes]
  (let [own-deps (->> (map lu dep-namespaces)
                      (distinct)
                      (filter identity)
                      (map #(create-branch-coordinate project %)))
        ext-deps (->> (concat dep-namespaces dep-classes)
                      (map #(resolve-with-ns % dependencies))
                      (distinct)
                      (filter identity)
                      (filter (comp not is-clojure?)))]
    [name (concat own-deps ext-deps)]))




(comment
  (require '[leiningen.core.project :as project])
  (require '[leiningen.repack.manifest :as manifest])
  (-> (project/read "example/hara/project.clj")
      (project/unmerge-profiles [:default])
      (classify/split-project-files)
      second
      (classify/classify-modules))
   => {"checkers" {:package "checkers", :namespaces #{hara.checkers}, :dep-namespaces #{hara.common.fn}, :dep-classes #{}, :files ["/Users/Chris/dev/chit/lein-repack/example/hara/src/hara/checkers.clj"], :items [{:ns hara.checkers, :file "/Users/Chris/dev/chit/lein-repack/example/hara/src/hara/checkers.clj", :dep-namespaces [hara.common.fn], :dep-classes []}]}, "collection" {:package "collection", :namespaces #{hara.collection.hash-map hara.collection.data-map}, :dep-namespaces #{hara.common.types hara.common.fn hara.common.keyword hara.common.collection hara.common.error clojure.string clojure.set}, :dep-classes #{}, :files ["/Users/Chris/dev/chit/lein-repack/example/hara/src/hara/collection/data_map.clj" "/Users/Chris/dev/chit/lein-repack/example/hara/src/hara/collection/hash_map.clj"], :items [{:ns hara.collection.data-map, :file "/Users/Chris/dev/chit/lein-repack/example/hara/src/hara/collection/data_map.clj", :dep-namespaces [clojure.set hara.common.error hara.common.fn hara.common.types], :dep-classes []} {:ns hara.collection.hash-map, :file "/Users/Chris/dev/chit/lein-repack/example/hara/src/hara/collection/hash_map.clj", :dep-namespaces [clojure.string clojure.set hara.common.types hara.common.keyword hara.common.collection], :dep-classes []}]}, "common" {:package "common", :namespaces #{hara.common.control hara.common.types hara.common.lettering hara.common.fn hara.common.keyword hara.common.collection hara.common.error hara.common hara.common.constructor hara.common.thread hara.common.debug hara.common.interop hara.common.string}, :dep-namespaces #{clojure.string clojure.set hara.import}, :dep-classes #{}, :files ["/Users/Chris/dev/chit/lein-repack/example/hara/src/hara/common/collection.clj" "/Users/Chris/dev/chit/lein-repack/example/hara/src/hara/common/constructor.clj" "/Users/Chris/dev/chit/lein-repack/example/hara/src/hara/common/control.clj" "/Users/Chris/dev/chit/lein-repack/example/hara/src/hara/common/debug.clj" "/Users/Chris/dev/chit/lein-repack/example/hara/src/hara/common/error.clj" "/Users/Chris/dev/chit/lein-repack/example/hara/src/hara/common/fn.clj" "/Users/Chris/dev/chit/lein-repack/example/hara/src/hara/common/interop.clj" "/Users/Chris/dev/chit/lein-repack/example/hara/src/hara/common/keyword.clj" "/Users/Chris/dev/chit/lein-repack/example/hara/src/hara/common/lettering.clj" "/Users/Chris/dev/chit/lein-repack/example/hara/src/hara/common/string.clj" "/Users/Chris/dev/chit/lein-repack/example/hara/src/hara/common/thread.clj" "/Users/Chris/dev/chit/lein-repack/example/hara/src/hara/common/types.clj" "/Users/Chris/dev/chit/lein-repack/example/hara/src/hara/common.clj"], :items [{:ns hara.common.collection, :file "/Users/Chris/dev/chit/lein-repack/example/hara/src/hara/common/collection.clj", :dep-namespaces [clojure.set hara.common.error hara.common.fn hara.common.types], :dep-classes []} {:ns hara.common.constructor, :file "/Users/Chris/dev/chit/lein-repack/example/hara/src/hara/common/constructor.clj", :dep-namespaces [hara.common.types hara.common.error], :dep-classes []} {:ns hara.common.control, :file "/Users/Chris/dev/chit/lein-repack/example/hara/src/hara/common/control.clj", :dep-namespaces [], :dep-classes []} {:ns hara.common.debug, :file "/Users/Chris/dev/chit/lein-repack/example/hara/src/hara/common/debug.clj", :dep-namespaces [], :dep-classes []} {:ns hara.common.error, :file "/Users/Chris/dev/chit/lein-repack/example/hara/src/hara/common/error.clj", :dep-namespaces [], :dep-classes []} {:ns hara.common.fn, :file "/Users/Chris/dev/chit/lein-repack/example/hara/src/hara/common/fn.clj", :dep-namespaces [hara.common.error hara.common.types], :dep-classes []} {:ns hara.common.interop, :file "/Users/Chris/dev/chit/lein-repack/example/hara/src/hara/common/interop.clj", :dep-namespaces [hara.common.lettering], :dep-classes []} {:ns hara.common.keyword, :file "/Users/Chris/dev/chit/lein-repack/example/hara/src/hara/common/keyword.clj", :dep-namespaces [clojure.string], :dep-classes []} {:ns hara.common.lettering, :file "/Users/Chris/dev/chit/lein-repack/example/hara/src/hara/common/lettering.clj", :dep-namespaces [clojure.string], :dep-classes []} {:ns hara.common.string, :file "/Users/Chris/dev/chit/lein-repack/example/hara/src/hara/common/string.clj", :dep-namespaces [], :dep-classes []} {:ns hara.common.thread, :file "/Users/Chris/dev/chit/lein-repack/example/hara/src/hara/common/thread.clj", :dep-namespaces [], :dep-classes []} {:ns hara.common.types, :file "/Users/Chris/dev/chit/lein-repack/example/hara/src/hara/common/types.clj", :dep-namespaces [], :dep-classes []} {:ns hara.common, :file "/Users/Chris/dev/chit/lein-repack/example/hara/src/hara/common.clj", :dep-namespaces [hara.import], :dep-classes []}]}, "state" {:package "state", :namespaces #{hara.state}, :dep-namespaces #{hara.common.types hara.common.fn clojure.string}, :dep-classes #{}, :files ["/Users/Chris/dev/chit/lein-repack/example/hara/src/hara/state.clj"], :items [{:ns hara.state, :file "/Users/Chris/dev/chit/lein-repack/example/hara/src/hara/state.clj", :dep-namespaces [clojure.string hara.common.fn hara.common.types], :dep-classes []}]}}

  )
