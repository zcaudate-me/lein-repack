(ns leiningen.repack.analyser.cljs
  (:require [leiningen.repack.analyser.clj :refer [grab-namespaces]]
            [leiningen.repack.analyser :as analyser]
            [leiningen.repack.data.file-info :refer [map->FileInfo]]
            [clojure.set :as set]))

(defmethod analyser/file-info :cljs
  [file]
  (let [[_ ns & body] (read-string (slurp file))]
    (map->FileInfo
     {:type :cljs
      :file file
      :expose #{[:cljs ns]}
      :import (set/union (->> body
                              (mapcat #(grab-namespaces % [:use :require]))
                              (map (fn [clj] [:cljs clj]))
                              set)
                         (->> body
                              (mapcat #(grab-namespaces % [:use-macros :require-macros]))
                              (map (fn [clj] [:clj clj]))))})))
