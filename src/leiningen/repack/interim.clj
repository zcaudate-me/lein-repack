(ns leiningen.repack.interim
  (:require [rewrite-clj.zip :as z]
            [korra.common :refer [*sep*]]
            [clojure.java.io :as io]
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

(defn interim-project-skeleton [project manifest]
  (let [interim-path (interim-path project)
        interim-dir  (io/file interim-path)
        _            (if (.exists interim-dir)
                       (delete-file-recursively interim-dir))]
    (do (io/make-parents interim-path)
        (.mkdir (io/file interim-path))
        (.mkdir (io/file (str interim-path *sep* "root")))
        (.mkdir (io/file (str interim-path *sep* "branches"))))
    (doseq [branch (-> manifest :branches keys)]
      (.mkdir (io/file (str interim-path *sep* "branches" *sep* branch))))))

(defn project-zip [project]
  (-> (z/of-file (str (:root project) *sep* "project.clj"))
      (z/find-value z/next 'defproject)))

(defn replace-project-value [zipper key value]
  (if-let [pos (-> zipper
                   (z/find-value key))]
    (-> pos
        (z/right)
        (z/replace value)
        (z/up)
        (z/find-value z/next 'defproject))
    zipper))

(defn update-project-value [zipper key f]
  (if-let [pos (-> zipper
                   (z/find-value key))]
    (let [pos (z/right pos)
          val (z/sexpr pos)]
      (-> pos
          (z/replace (f val))
          (z/up)
          (z/find-value z/next 'defproject)))
    zipper))

(defn remove-project-key [zipper key]
  (if-let [pos (-> zipper
                   (z/find-value key))]
    (-> pos
        (z/remove*)
        (z/right)
        (z/remove*)
        (z/up)
        (z/find-value z/next 'defproject))
    zipper))

(defn root-project-file [project manifest]
  (-> (project-zip project)
      (replace-project-value :dependencies
                             (-> manifest :root :dependencies))
      (remove-project-key :profiles)
      (remove-project-key :source-paths)
      (remove-project-key :repack)
      z/print-root
      with-out-str
      (->> (spit (str (interim-path project) *sep* "root" *sep* "project.clj")))))

(defn branch-project-file [project manifest name]
  (-> (project-zip project)
      (update-project-value 'defproject (fn [x] (symbol (str x "." name))))
      (update-project-value :description
                            (fn [x] (or (-> manifest :branches (get name) :description) x)))
      (replace-project-value :dependencies
                             (-> manifest :branches (get name) :dependencies))
      (remove-project-key :profiles)
      (remove-project-key :source-paths)
      (remove-project-key :repack)
      z/print-root
      with-out-str
      (->> (spit (str (interim-path project) *sep* "branches" *sep* name *sep* "project.clj")))))

(defn shortlist-branches [manifest]
  (->> (:branches manifest)
       (map (fn [[k m]]
              (-> m
                  (select-keys [:coordinate :dependencies])
                  (assoc :id k))))))

(defn all-branch-deps [manifest]
  (->> (:branches manifest)
       (map (fn [[k m]]
              (:coordinate m)))
       (set)))

(defn sort-branch-deps-pass
  [all sl]
  (reduce (fn [out i]
            (if (some all (:dependencies i))
              out
              (conj out i))) [] sl))

(defn sort-branch-deps [manifest]
  (let [sl (shortlist-branches manifest)
        all (all-branch-deps manifest)]
    (loop [all all
           sl  sl
           output []]
      (if-not (or (empty? sl)
                  (empty? all))
        (let [pass (sort-branch-deps-pass all sl)]
          (recur
           (apply disj all (map :coordinate pass))
           (filter (fn [x] (some #(not= x %) pass) ) sl)
           (conj output pass)))
        output))))

(defn copy-file [rel-path source sink]
  (let [source-file (io/as-file (str source *sep* rel-path))
        sink-file   (io/as-file (str sink *sep* rel-path))]
    (io/make-parents sink-file)
    (io/copy source-file sink-file)))

(defn copy-source-files [project manifest]
  (let [source-path (-> project :source-paths first)
        interim-path (interim-path project)]
    (doseq [root-file (-> manifest :root :files)]
      (copy-file root-file source-path (str interim-path *sep* "root" *sep* "src")))
    (doseq [branch (-> manifest :branches keys)]
      (doseq [branch-file (-> manifest :branches (get branch) :files)]
        (copy-file branch-file source-path (str interim-path *sep* "branches" *sep* branch *sep* "src"))))))

(defn interim-project-files [project manifest]
  (interim-project-skeleton project manifest)
  (root-project-file project manifest)
  (doseq [branch (-> manifest :branches keys)]
    (branch-project-file project manifest branch))

  (copy-source-files project manifest))


(comment
  (flatten (sort-branch-deps
            (manifest/create-manifest
             (project/read "example/hara/project.clj"))))

  [[{:id "import", :dependencies [[org.clojure/clojure "1.5.1"]], :coordinate [im.chit/hara.import "1.1.0-SNAPSHOT"]}]
   [{:id "common", :dependencies [[org.clojure/clojure "1.5.1"] [im.chit/hara.import "1.1.0-SNAPSHOT"]],
     :coordinate [im.chit/hara.common "1.1.0-SNAPSHOT"]}]
   [{:id "checkers", :dependencies [[org.clojure/clojure "1.5.1"] [im.chit/hara.common "1.1.0-SNAPSHOT"]],
     :coordinate [im.chit/hara.checkers "1.1.0-SNAPSHOT"]}
    {:id "collection", :dependencies [[org.clojure/clojure "1.5.1"] [im.chit/hara.common "1.1.0-SNAPSHOT"]],
     :coordinate [im.chit/hara.collection "1.1.0-SNAPSHOT"]}
    {:id "state", :dependencies [[org.clojure/clojure "1.5.1"] [im.chit/hara.common "1.1.0-SNAPSHOT"]],
     :coordinate [im.chit/hara.state "1.1.0-SNAPSHOT"]}]]

  (create-branch-steps
   (manifest/create-manifest
    (project/read "example/hara/project.clj")))

  (all-branch-deps
   (manifest/create-manifest
    (project/read "example/hara/project.clj"))))

(defn create-interim-projects [project manifest])

(interim-project-files (project/read "example/hara/project.clj")
                       (manifest/create-manifest
                           (project/read "example/hara/project.clj")))
#_(copy-source-files
 (project/read "example/hara/project.clj")
                       (manifest/create-manifest
                           (project/read "example/hara/project.clj")))




#_(spit "example/hara/target/interim/root/pom.xml"
      (leiningen.pom/make-pom (project/read "example/hara/target/interim/root/project.clj")))

#_(leiningen.install/install (project/read "example/hara/target/interim/root/project.clj"))


(comment

  (:target-path (project/read "example/hara/project.clj"))
  => "/Users/Chris/dev/chit/lein-repack/example/hara/target"

  (defn create-project )


  (def prj-map (z/find-value data z/next 'defproject))
  (def descr (-> prj-map (z/find-value 'defproject) z/right z/sexpr))
  (def descr (-> prj-map (z/find-value :description) z/right))

  (-> prj-map
      (z/find-value 'defproject)
      z/right
      (z/replace 'lein-repack.hello)
      (z/find-value :description)
      z/right
      (z/replace "My first Project.")
      z/print-root)
  (with-out-str
    (-> descr
        (z/replace "My first Project.")) z/print-root)
)
