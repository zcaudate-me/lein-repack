(ns leiningen.repack.data.file-info
  (:require [clojure.string :as string]))

(defrecord FileInfo []
  Object
  (toString [this]
    (str (-> (or (-> this :ns name)
                 (-> this :class name))
             (string/split #"\.")
             last)
         "." (-> this :type name))))

(defmethod print-method FileInfo [v w]
  (.write w (str v)))
