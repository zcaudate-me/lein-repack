(ns leiningen.repack.maven
  (:require [leiningen.repack.common :refer :all]
            [dynapath.util :as dp]
            [iroh.common :refer :all]
            [clojure.java.io :refer [as-file] :as io]
            [clojure.string :as string])
  (:import [java.net URL URLClassLoader]
           [java.util.jar JarFile JarEntry]))

(def ^:dynamic *current-cl* (.getContextClassLoader (Thread/currentThread)))

(def ^:dynamic *lein-jar*
  (->> (string/split (System/getProperty "java.class.path") #":")
     (filter (comp not empty?))
     first))

(def ^:dynamic *local-repo*
  (string/join *sep* [(System/getProperty "user.home") ".m2" "repository"]))

(defn file-by-coordinates [[name version] suffix]
  (let [[group artifact] (string/split (str name) #"/")
        artifact (or artifact
                     group)]
    (string/join *sep*
                 [*local-repo* (.replaceAll group "\\." *sep*)
                  artifact version (str artifact "-" version suffix)] )))

(defn jar-by-coordinates [coordinate]
  (file-by-coordinates coordinate ".jar"))

(defn pom-by-coordinates [coordinate]
  (file-by-coordinates coordinate ".pom"))

(defn jar-contents [jar-path]
  (with-open [zip (java.util.zip.ZipInputStream.
                   (io/input-stream jar-path))]
  (loop [entries []]
    (if-let [e (.getNextEntry zip)]
      (recur (conj entries (.getName e)))
      entries))))

(defn coordinate-contents [coordinate]
  (if-let [jar-path (jar-by-coordinates coordinate)]
    (jar-contents jar-path)))

(defn jar-contains-resource? [jar-path path]
  (->> (jar-contents jar-path)
       (filter #(= path %))
       (empty?)))

(defn coordinate-contains-resource? [coordinate path]
  (if-let [jar-path (jar-by-coordinates coordinate)]
    (jar-contains-resource? jar-path path)))

(defn class-name->jar-resource-path [clsn]
  (str (.replaceAll clsn "\\." *sep*) ".class"))

(defn namespace->jar-resource-path [ns]
  (str (.replaceAll (munge (str ns)) "\\." *sep*) ".clj"))

(defn jar-by-path
  ([path] (jar-by-path path *current-cl*))
  ([path loader]
      (if-let [res (-> path
                       (io/resource loader))]
        (-> (re-find #"file:(.*)" (.getPath res))
            second
            (clojure.string/split #"!/")))))

(defn jar-by-class
  ([cls] (jar-by-class cls *current-cl*))
  ([cls loader]
      (-> (.getName cls)
          (class-name->jar-resource-path)
          (jar-by-path loader))))

(defn jar-by-namespace
  ([ns] (jar-by-namespace ns *current-cl*))
  ([ns loader]
     (-> (namespace->jar-resource-path ns)
         (jar-by-path loader))))

(defn maven-by-path
  ([path] (maven-by-path path *current-cl*))
  ([path loader]
     (when (.startsWith path *local-repo*)
       (let [[_ version artifact & group]
             (-> (subs path (count *local-repo*))
                 (clojure.string/split (re-pattern *sep*))
                 (->> (filter (comp not empty?)))
                 (reverse))]
         (-> (clojure.string/join  "." (reverse group))
             (str *sep* artifact)
             symbol
             (vector version))))))

(defn maven-by-class
  ([cls] (maven-by-class cls *current-cl*))
  ([cls loader]
     (-> (jar-by-class cls loader)
         first
         (maven-by-path loader))))

(defn maven-by-namespace
  ([cls] (maven-by-namespace cls *current-cl*))
  ([cls loader]
     (if-let [jar (first (jar-by-namespace cls loader))]
       (maven-by-path jar loader))))
