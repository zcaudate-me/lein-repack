(ns leiningen.repack.data.dep-info
  (:require [clojure.string :as string]))

(defrecord DepInfo []
  Object
  (toString [this]
    (str (-> (or (-> this :ns name)
                 (-> this :class name))
             (string/split #"\.")
             last)
         "." (-> this :type name))))

(defmethod print-method DepInfo [v w]
  (.write w (str v)))
