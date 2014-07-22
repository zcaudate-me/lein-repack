(ns leiningen.repack.analyser.common)

(defrecord FileInfo [type])

(defmulti file-info
  (fn [file])
  )