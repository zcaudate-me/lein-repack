(ns leiningen.repack.data.file-info
  (:require [clojure.string :as string]))

(defrecord FileInfo []
  Object
  (toString [this] (-> this :path)))

(defmethod print-method FileInfo [v w]
  (.write w (str v)))
