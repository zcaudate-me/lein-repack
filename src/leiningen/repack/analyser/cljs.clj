(ns leiningen.repack.analyser.cljs
  (:require [leiningen.repack.analyser.clj :refer [grab-namespaces]]
            [leiningen.repack.analyser.common :as analyser]))

(defmethod analyser/file-info :cljs [file]
  (let [[_ ns & body] (read-string (slurp file))]
    (analyser/map->FileInfo
      {:type :cljs
       :ns ns
       :file file
       :dep-clj   (vec (mapcat #(grab-namespaces % [:use-macros :require-macros]) body))
       :dep-cljs  (vec (mapcat #(grab-namespaces % [:use :require]) body))})))