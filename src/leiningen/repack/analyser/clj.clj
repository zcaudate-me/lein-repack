(ns leiningen.repack.analyser.clj
  (:require [leiningen.repack.analyser.common :as analyser]))

(defn grab-namespaces [form fsyms]
  (when (some #(= % (first form)) fsyms)
    (mapcat (fn [x]
              (cond (symbol? x) [x]

                    (or (vector? x) (list? x))
                    (if (some vector? x)
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

(defn grab-def-classes [file]
  '[java.lang.String])

(defmethod analyser/file-info :clj
  [file]
  (let [[_ ns & body] (read-string (slurp file))]
    (analyser/map->FileInfo
      {:type :clj
       :ns ns
       :classes (concat (grab-gen-class ns body)
                        (grab-def-classes file))
       :file file
       :dep-clj  (vec (mapcat #(grab-namespaces % [:use :require]) body))
       :dep-imports (vec (mapcat grab-dep-imports body))})))
