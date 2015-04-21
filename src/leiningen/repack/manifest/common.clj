(ns leiningen.repack.manifest.common
  (:require [clojure.java.io :as io]
            [clojure.set :as set]
            [leiningen.repack.analyser :as analyser]
            [leiningen.repack.analyser [clj cljs java]]
            [leiningen.repack.data.file-info :refer [map->FileInfo]]
            [leiningen.repack.data.util :as util]))

(defn create-filemap
  ([files] (create-filemap files {:pnil "default"}))
  ([files opts]
     (reduce-kv (fn [m k v]
                  (let [grp (or k
                                (:pnil opts))
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

(defmulti build-filemap (fn [project-dir cfg] (:type cfg)))

(defmethod build-filemap :default
  [project-dir cfg]
  (let [res-path   (:path cfg)
        res-folder (io/file project-dir res-path)
        subpackage (:subpackage cfg) 
        distro     (->> (file-seq res-folder)
                        (filter #(not (.isDirectory %)))
                        (filter (fn [f] (not (some #(re-find % (.getPath f)) 
                                                   (:jar-exclusions cfg)))))
                        (map #(util/relative-path res-folder %))
                        (util/group-by-distribution (:distribute cfg)))]
    (create-filemap distro {:root   project-dir 
                            :folder res-path
                            :pnil   subpackage})))
