(ns leiningen.repack.manifest
  (:require [clojure.java.io :as io]
            [clojure.set :as set]
            [leiningen.repack.analyser :as analyser]
            [leiningen.repack.data.file-info :refer [map->FileInfo]]
            [leiningen.repack.data.util :as util]))

(defn create-manifest
  ([files] (create-manifest files {:pnil "default"}))
  ([files opts]
     (reduce-kv (fn [m k v]
                  (let [grp (or k (:pnil opts))
                        root-dir (or (:root opts) "")]
                    (->> v
                         (map (fn [ele]
                                (let [fele  (io/file root-dir (or (:folder opts) ".") ele)
                                      finfo  (analyser/file-info fele)]
                                  (-> finfo
                                      (assoc
                                          :type (analyser/file-type fele)
                                          :path (util/relative-path root-dir fele))
                                      (map->FileInfo)))))
                         (set)
                         (assoc m grp))))
                {}
                files)))


(defn merge-manifests [& manifests]
  (apply merge-with set/union manifests))
