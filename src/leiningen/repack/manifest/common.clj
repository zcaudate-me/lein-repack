(ns leiningen.repack.manifest.common
  (:require [clojure.java.io :as io]
            [leiningen.repack.manifest :as manifest]
            [leiningen.repack.data.util :as util]))

(defn build-manifest [project-dir cfg]
  (let [res-folder (:path cfg)
        res-path (io/file project-dir res-folder)]
    (-> (->> (file-seq res-path)
             (filter #(not (.isDirectory %)) )
             (map #(util/relative-path res-path %))
             (util/group-by-distribution (:distribute cfg)))
        (manifest/create-manifest {:root project-dir
                                   :folder res-folder
                                   :pnil (:subpackage cfg)}))))
