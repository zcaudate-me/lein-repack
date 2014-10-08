<<<<<<< HEAD
(ns leiningen.repack.analyser.cljs
  (:require [leiningen.repack.analyser.clj :refer [grab-namespaces]]
            [leiningen.repack.analyser.common :as analyser]))

(defmethod analyser/file-info :java [file]
  (let [[_ ns & body] (read-string (slurp file))]
    (analyser/map->FileInfo
      {:type :java
       :class file
       ;;:dep-classes (vec (mapcat grab-classes body))
       :file file})))
=======
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
>>>>>>> flexible
