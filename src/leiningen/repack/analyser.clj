(ns leiningen.repack.analyser
  (:require [clojure.string :as string]
            [leiningen.repack.data.file-info :as info]))

(defn file-type [file]
  (-> (str file)
      (clojure.string/split #"\.")
      last
      keyword))

;;(def file-info 1)
(defmulti file-info file-type)

(defmethod file-info :default
  [file]
  {:file file
   :exports #{}
   :imports #{}})
