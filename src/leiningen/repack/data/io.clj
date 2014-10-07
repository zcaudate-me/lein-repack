(ns leiningen.repack.data.io)

(defn delete-file-recursively
  "Delete file f. If it's a directory, recursively delete all its contents.
Raise an exception if any deletion fails unless silently is true."
  [f & [silently]]
  (let [f (io/file f)]
    (if (.isDirectory f)
      (doseq [child (.listFiles f)]
        (delete-file-recursively child silently)))
    (io/delete-file f silently)))

(defn copy-file [rel-path source sink]
  (let [source-file (io/as-file (str source *sep* rel-path))
        sink-file   (io/as-file (str sink *sep* rel-path))]
    (io/make-parents sink-file)
    (io/copy source-file sink-file)))