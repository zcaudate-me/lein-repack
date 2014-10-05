(ns leiningen.repack.manifest.source
  (:require [clojure.java.io :as io]
            [leiningen.repack.manifest :as manifest]
            [leiningen.repack.data.util :as util]
            [clojure.string :as string]))

(defn child-dirs [path]
  (let [children (seq (.list path))]
    (->> children
         (filter (fn [chd] (.isDirectory (io/file path chd)))))))

(defn split-path [path]
  (let [idx (.lastIndexOf path ".")]
    (util/relative-path-vector (.substring path 0 idx))))

(defn group-by-package [opts files]
  (let [lvl (or (:levels opts) 1)]

    (reduce (fn [i f]
              (let [rpath (util/relative-path (:root opts) f)
                    v   (split-path rpath)
                    pkg (take lvl v)
                    pkg (if (get (:package opts) (first pkg))
                          (first pkg)
                          (string/join "." pkg))]
                (update-in i [pkg] (fnil #(conj % rpath) #{rpath} ))))
            {}  files)))

(defn build-manifest [project-dir cfg]
  (let [src-path (:path cfg)
        src-dir (io/file project-dir src-path)
        root-path (or (:root cfg)
                      (let [ds (child-dirs src-dir)]
                        (if (= (count ds) 1)
                          (first ds)
                          (throw (Exception. (str "More than one possible root: " ds))))))
        root-dir (io/file src-dir root-path)]
    (-> (->> root-dir
             (file-seq)
             (filter (fn [f] (not (.isDirectory f))))
             (group-by-package (assoc cfg :root root-dir)))
        (manifest/create-manifest {:root project-dir
                                   :folder (str src-path "/" root-path)
                                   :pnil "default"}))))
