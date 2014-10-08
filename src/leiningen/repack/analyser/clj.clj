(ns leiningen.repack.analyser.clj
<<<<<<< HEAD
  (:require [leiningen.repack.analyser.common :as analyser]))

(defn grab-namespaces [form fsyms]
=======
  (:require [leiningen.repack.analyser :as analyser]
            [clojure.java.io :as io]
            [clojure.set :as set]))

(defn get-namespaces [form fsyms]
>>>>>>> flexible
  (when (some #(= % (first form)) fsyms)
    (mapcat (fn [x]
              (cond (symbol? x) [x]

                    (or (vector? x) (list? x))
<<<<<<< HEAD
                    (if (some vector? x)
                      (map #(-> (first x) (str "." %))
                           (filter vector? x))
                      [(first x)])))
            (next form))))

(defn grab-dep-imports [form]
=======
                    (let [[rns & more] x]
                      (if (or (empty? more)
                              (some keyword? more))
                        [rns]
                        (->> more
                             (map (fn [y] (-> rns
                                             (str "."
                                                  (if (vector? y) (first y) y))
                                             symbol)))

                             )))))
            (next form))))

(defn get-imports [form]
>>>>>>> flexible
  (when (= :import (first form))
    (mapcat (fn [x]
              (cond (symbol? x) [x]

                    (or (vector? x) (list? x))
                    (map #(symbol (str (first x) "." %))
                         (rest x))))
            (next form))))

<<<<<<< HEAD
(defn grab-gen-class [ns body]
=======
(defn get-genclass [ns body]
>>>>>>> flexible
  (if-let [gen-form (->> body
                         (filter (fn [form]
                                   (= :gen-class (first form))))
                         first)]
    [(or (->> gen-form next
               (apply hash-map)
               :name)
<<<<<<< HEAD
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
=======
         ns)]))

(defn get-defclass [ns forms]
  (->> forms
       (keep (fn [form]
               (and (list? form)
                    ('#{deftype defrecord} (first form))
                    (second form))))
       (map (fn [ele] (symbol (str ns "." ele))))))

(defmethod analyser/file-info :clj
  [file]
  (let [[[_ ns & body] & forms]
        (read-string (str "[" (-> file io/reader slurp) "]"))]
    {:exports (set/union #{[:clj ns]}
                         (set (map (fn [cls] [:java cls]) (get-genclass ns body)))
                         (set (map (fn [cls] [:java cls]) (get-defclass ns forms))))
     :imports (set/union (->> body
                              (mapcat #(get-namespaces % [:use :require]))
                              (map (fn [clj] [:clj clj]))
                              set)
                         (->> body
                              (mapcat get-imports)
                              (map (fn [clj] [:java clj]))
                              set))}))

(comment
  (require '[clojure.java.io :as io])
  (into {} (analyser/file-info
            (io/file "src/leiningen/repack/analyser/clj.clj"))))
>>>>>>> flexible
