(ns leiningen.repack.analyser
  (:require [clojure.string :as string]))

(defmulti file-info
  (fn [file]
    (-> (str file)
        (clojure.string/split #"\.")
        last
        keyword)))
