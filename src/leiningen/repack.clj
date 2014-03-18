(ns leiningen.repack
  (:use midje.sweet)
  (:require [leiningen.core.project :as project]
            [leiningen.pom :as pom]
            [clojure.java.io :as io]
            [clojure.string :as string]
            [leiningen.repack.common :refer :all]))

(defn name->path [name]
  (.replaceAll (munge (str name)) "\\." *sep*))

(defn list-clojure-files [dir]
  (filter (fn [f] (re-find #"^[^\.].*\.cljs?$" (.getName f)))
        (file-seq (io/file dir))))

(defn submodule-file? [file root-dir base excludes]
  (let [base-path (name->path base)
        exclude-paths (map name->path excludes)
        file-path (.getAbsolutePath file)]
    (cond (some #(.startsWith file-path
                              (string/join *sep* [root-dir base-path %]))
                exclude-paths)
          false

          (.startsWith file-path
                       (string/join *sep* [root-dir base-path]))
          true

          :else false)))

(defn classify-file [file root-path base level]
  (let [base-path (name->path base)
        file-path (.getAbsolutePath file)]
    (-> (subs file-path (+ 2 (count root-path) (count base-path)))
        (->> (re-find #"^(.*)\.cljs?$"))
        (second)
        (string/split (re-pattern *sep*))
        (->> (take level)
             (string/join ".")))))

(defn grab-namespaces [form]
  (when (or (= :use (first form))
            (= :require (first form)))
    (map (fn [x]
           (cond (symbol? x) x

                 (or (vector? x) (list? x))
                 (first x)))
         (next form))))

(defn grab-classes [form]
  (when (= :import (first form))
    (mapcat (fn [x]
              (cond (symbol? x) [x]

                    (or (vector? x) (list? x))
                    (map #(symbol (str (first x) "." %))
                         (rest x))))
            (next form))))

(defn read-file-namespace [file]
  (let [[_ ns & body] (read-string (slurp file))]
    {:ns ns
     :file file
     :dep-namespaces (mapcat grab-namespaces body)
     :dep-classes (mapcat grab-classes body)}))

(defn split-project-files [root-dir base level excludes]
  (let [all-files (->> (list-clojure-files root-dir)
                       (group-by #(submodule-file?
                                   % root-dir base excludes)))
        parent-files (vec (get all-files false))
        module-files (->> (get all-files true)
                          (group-by #(classify-file % root-dir base level)))]
    [parent-files module-files]))

(defn classify-info [module-files]
  (->> module-files
       (map (fn [[k v]]
              (let [items (mapv read-file-namespace v)]
                [k {:package k
                    :namespaces (set (map :ns items))
                    :dep-namespaces (set (mapcat :dep-namespaces items))
                    :dep-classes   (set (mapcat :dep-classes items))
                    :files (map :file items)
                    :items items}])))
       (into {})))



(defn package-dependencies [pkg pkgs]
  )

(fact "sort-module-files"
  (package-info
   (second (split-project-files "/Users/zhengc/dev/chit/hara/src" "hara" 1 []))
   )
  )

(fact "split-project-files"
  (keys (second (split-project-files "/Users/zhengc/dev/chit/hara/src" "hara" 1 [])))
  => '("checkers" "collection" "common" "import" "state")

  (keys (second (split-project-files "/Users/zhengc/dev/chit/hara/src" "hara" 1 ["common"])))
  => '("checkers" "collection" "import" "state"))

(fact "read-file-namespace"
  (read-file-namespace
   (io/as-file "/Users/zhengc/dev/chit/hara/src/hara/common.clj"))
  => (just {:ns 'hara.common
            :file anything
            :dep-namespaces '(hara.import),
            :dep-classes ()})
  (read-file-namespace
   (io/as-file "/Users/zhengc/dev/chit/hara/src/hara/common/collection.clj"))
  => (just {:ns 'hara.common.collection
            :file anything
            :dep-namespaces '(clojure.set hara.common.error hara.common.fn hara.common.types),
            :dep-classes ()}))


(fact "classify-file"
  (classify-file (io/as-file "/Users/zhengc/dev/chit/hara/src/hara/common.clj")
                 "/Users/zhengc/dev/chit/hara/src"
                 "hara"
                 1)
  => "common"

  (classify-file (io/as-file "/Users/zhengc/dev/chit/hara/src/hara/common/control.clj")
                 "/Users/zhengc/dev/chit/hara/src"
                 "hara"
                 1)
  => "common"

  (classify-file (io/as-file "/Users/zhengc/dev/chit/hara/src/hara/common/control.clj")
                 "/Users/zhengc/dev/chit/hara/src"
                 "hara"
                 2)
  => "common.control")

(fact "submodule-file?"
  (submodule-file? (io/as-file "/Users/zhengc/dev/chit/hara/src/hara/common.clj")
                   "/Users/zhengc/dev/chit/hara/src"
                   "hara"
                   ["common"])
  => false

  (submodule-file? (io/as-file "/Users/zhengc/dev/chit/hara/src/hara/checkers.clj")
                   "/Users/zhengc/dev/chit/hara/src"
                   "hara"
                   ["common"])
  => true)

(fact "list-clojure-files"
  (count (list-clojure-files "/Users/zhengc/dev/chit/hara/src")) => 18)


(fact "name->path"
  (name->path "clj-time.core")
 => "clj_time/core")


(comment lein repack jar
         lein repack pom
         lein repack deploy

         (binding [leiningen.core.main/*cwd* "/Users/zhengc/dev/chit/hara"]
           (project/read "/Users/zhengc/dev/chit/hara/project.clj"))

         (-> (project/read "/Users/zhengc/dev/chit/hara/project.clj")
             :repack)
         '{:parent [core], :levels 2, :base hara}

         (defn find-groups [src])

         (-> (project/read "project.clj") :source-paths)

         (-> (project/read "project.clj") keys sort)

         (-> (project/read "project.clj") :name)

         (-> (project/read "project.clj") :group)


         (defn repack
           "I don't do a lot."
           [project & args]
           (println "Hi!"))


         (comment )
)
