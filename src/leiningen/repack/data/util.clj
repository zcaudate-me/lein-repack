(ns leiningen.repack.data.util
  (:require [clojure.java.io :as io]
            [clojure.string :as string]))

(defn interpret-dots
  ([v] (interpret-dots v []))
  ([v output]
     (if-let [s (first v)]
       (condp = s
         "."  (recur (next v) output)
         ".." (recur (next v) (pop output))
         (recur (next v) (conj output s)))
       output)))

(defn drop-while-matching [u v]
  (cond (or (empty? u) (empty? v)) [u v]

        (= (first u) (first v))
        (recur (rest u) (rest v))

        :else [u v]))

(def path-pattern "Raw backslashes aren't valid regular expressions (Windows file separator). Quote ensures correct quoting." 
  (re-pattern (java.util.regex.Pattern/quote (System/getProperty "file.separator"))))
  
(defn path-vector [path]
  (string/split (.getAbsolutePath (io/file path)) path-pattern))

(defn relative-path-vector [path]
  (string/split path path-pattern)) 

(defn relative-path [root other]
  (let [[base rel] (drop-while-matching (interpret-dots (path-vector root))
                                        (interpret-dots (path-vector other)))]
    (if (and (empty? base) (empty? rel))
      "."
      (->> (-> (count base)
               (repeat "..")
               (concat rel))
           (string/join (System/getProperty "file.separator"))))))

(defn best-match [file distribution]
  (let [file-vec (relative-path-vector file)
        file-count (count file-vec)]
    (reduce-kv (fn [i k v]
                 (reduce (fn [[sym cnt] ele]
                           (let [tcnt (->> (relative-path-vector ele)
                                           (drop-while-matching file-vec)
                                           (first)
                                           (count)
                                           (- file-count))]
                             (if (> tcnt cnt)
                               [k tcnt]
                               [sym cnt])))
                         i v))
               [nil 0]
               distribution)))

(defn group-by-distribution [distribution files]
  (reduce (fn [i f]
            (let [[sym _] (best-match f distribution)]
              (update-in i [sym] (fnil #(conj % f) #{f}))))
          {}
          files))
