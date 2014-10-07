(ns leiningen.repack.analyser.java
  (:require [clojure.java.io :as io]
            [clojure.string :as string]
            [leiningen.repack.analyser :as analyser]))

(defn get-class [file]
  (let [pkg (-> (->> (io/reader file)
                     (line-seq)
                     (filter #(.startsWith % "package") )
                     (first))
                (string/split #"[ ;]")
                (second))
        nm  (let [nm (.getName file)]
              (subs nm 0 (- (count nm) 5)))]
    (symbol (str pkg "." nm))))

(defn get-imports [file]
  (->> (io/reader file)
       (line-seq)
       (filter #(.startsWith % "import") )
       (map #(string/split % #"[ ;]"))
       (map second)
       (map symbol)))

(defmethod analyser/file-info :java [file]
  {:file file
   :exports #{[:class (get-class file)]}
   :imports (set (map (fn [jv] [:class jv]) (get-imports file)))})
