(ns leiningen.repack.analyser.cljs
  (:require [leiningen.repack.analyser.clj :refer [grab-namespaces]]
            [leiningen.repack.analyser.common :as analyser]))

(defmethod analyser/file-info :java [file]
  (let [[_ ns & body] (read-string (slurp file))]
    (analyser/map->FileInfo
      {:type :java
       :class file
       ;;:dep-classes (vec (mapcat grab-classes body))
       :file file})))
