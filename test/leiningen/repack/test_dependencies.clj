(ns leiningen.repack.test-dependencies
  (:require [leiningen.repack.dependencies :refer :all]
            [korra.resolve :refer :all]
            [midje.sweet :refer :all]))

(System/getProperty "clojure.debug")
(System/getProperty "clojure.compile.path")
(System/getProperty "java.class.path")
":/Users/zhengc/.lein/self-installs/leiningen-2.3.4-standalone.jar"


(defn project-dependencies [file]
  (->> (slurp file)
       (read-string)
       (drop 3)
       (apply hash-map)
       :dependencies))

(project-dependencies "project.clj")

(coordinate-dependencies '[[im.chit/korra "0.1.0"]])

(coordinate-dependencies '[[org.clojure/clojure "1.5.1"]])
=> '[[org.clojure/clojure "1.5.1"]]



[]





(resolve-coordinates 'clojure.string)
