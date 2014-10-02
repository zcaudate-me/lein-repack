(ns leiningen.repack.analyser.clj
  (:require [leiningen.repack.analyser :as analyser]
            [leiningen.repack.data.file-info :refer [map->FileInfo]]
            [clojure.set :as set]))

(defn grab-namespaces [form fsyms]
  (when (some #(= % (first form)) fsyms)
    (mapcat (fn [x]
              (cond (symbol? x) [x]

                    (or (vector? x) (list? x))
                    (if (and (some vector? x)
                             (not (some keyword? x)))
                      (map #(-> (first x) (str "." %))
                           (filter vector? x))
                      [(first x)])))
            (next form))))

(defn grab-dep-imports [form]
  (when (= :import (first form))
    (mapcat (fn [x]
              (cond (symbol? x) [x]

                    (or (vector? x) (list? x))
                    (map #(symbol (str (first x) "." %))
                         (rest x))))
            (next form))))

(defn grab-gen-class [ns body]
  (if-let [gen-form (->> body
                         (filter (fn [form]
                                   (= :gen-class (first form))))
                         first)]
    [(or (->> gen-form next
               (apply hash-map)
               :name)
          ns)]))

(defmethod analyser/file-info :clj
  [file]
  (let [[_ ns & body] (read-string (slurp file))]
    (map->FileInfo
     {:type :clj
      :file file
      :expose (set/union #{[:clj ns]}
                         (map (fn [cls] [:java cls]) (grab-gen-class ns body)))
      :import (set/union (->> body
                              (mapcat #(grab-namespaces % [:use :require]))
                              (map (fn [clj] [:clj clj]))
                              set)
                         (->> body
                              (mapcat grab-dep-imports)
                              (map (fn [clj] [:java clj]))))})))

(comment
  (require '[clojure.java.io :as io])
  (into {} (analyser/file-info (io/file "src/leiningen/repack/analyser/clj.clj"))))
