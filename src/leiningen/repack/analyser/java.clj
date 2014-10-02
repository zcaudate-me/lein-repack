(ns leiningen.repack.analyser.java
  (:require [clojure.java.io :as io]
            [clojure.string :as string]
            [leiningen.repack.analyser :as analyser]
            [leiningen.repack.data.file-info :refer [map->FileInfo]]))

(defn get-class [file]
  (let [pkg (-> (->> (slurp file)
                  (line-seq)
                  (filter #(.startsWith % "package") )
                  (first))
                (string/split #"[ ;]")
                (second))
        nm  (.getName file)]
    (symbol (str pkg "." nm))))

(defn get-imports [file]
  (->> (slurp file)
       (line-seq)
       (filter #(.startsWith % "import") )
       (map #(string/split % #"[ ;]"))
       (map second)
       (map symbol)))

(defmethod analyser/file-info :java [file]
  (map->FileInfo
   {:type :java
    :file file
    :expose #{[:java (get-class file)]}
    :import (set (map (fn [jv] [:java jv] (get-imports file))))}))
