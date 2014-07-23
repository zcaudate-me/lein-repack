(ns leiningen.repack.analyser.common
  (:require [clojure.string :as string]))

(defn assoc-if
  ([m k v]
     (assoc-if m k v #(or (and (number? %) (< 0 %))
                          (and (coll? %) (not (empty? %))))))
  ([m k v func]
     (if (func v)
       (assoc m k v)
       m)))

(defrecord FileInfo []
  Object
  (toString [this]
    (str (-> (or (-> this :ns name)
                 (-> this :class name))
             (string/split #"\.")
             last)
         "." (-> this :type name)
         (let [m (-> {}
                     (assoc-if :clj (-> this :dep-clj count))
                     (assoc-if :cljs (-> this :dep-cljs count))
                     (assoc-if :classes (:classes this))
                     (assoc-if :imports (-> this :dep-imports) ))]
           (if (empty? m) "" m)))))

(defmethod print-method FileInfo [v w]
  (.write w (str v)))

(defmulti file-info
  (fn [file]
    (-> (str file)
        (clojure.string/split #"\.")
        last
        keyword)))

(comment
  (require '[clojure.java.io :as io])
  (:dep-clj (file-info (io/file "src/leiningen/repack/manifest.clj")))

  [leiningen.core.project leiningen.repack.manifest.classify leiningen.repack.manifest.graph "korra.common.[*sep*]" korra.resolve]
  (-> (str (io/file "src/leiningen/repack/manifest.clj"))
      (clojure.string/split #"\.")
      last
      keyword)

  (def file-info 1)
  (>refresh)
  )
