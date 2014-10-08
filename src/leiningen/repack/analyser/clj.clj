(ns leiningen.repack.analyser.clj
  (:require [leiningen.repack.analyser :as analyser]
            [clojure.java.io :as io]
            [clojure.set :as set]))

(defn get-namespaces [form fsyms]
  (when (some #(= % (first form)) fsyms)
    (mapcat (fn [x]
              (cond (symbol? x) [x]

                    (or (vector? x) (list? x))
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
  (when (= :import (first form))
    (mapcat (fn [x]
              (cond (symbol? x) [x]

                    (or (vector? x) (list? x))
                    (map #(symbol (str (first x) "." %))
                         (rest x))))
            (next form))))

(defn get-genclass [ns body]
  (if-let [gen-form (->> body
                         (filter (fn [form]
                                   (= :gen-class (first form))))
                         first)]
    [(or (->> gen-form next
               (apply hash-map)
               :name)
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
                         (set (map (fn [cls] [:class cls]) (get-genclass ns body)))
                         (set (map (fn [cls] [:class cls]) (get-defclass ns forms))))
     :imports (set/union (->> body
                              (mapcat #(get-namespaces % [:use :require]))
                              (map (fn [clj] [:clj clj]))
                              set)
                         (->> body
                              (mapcat get-imports)
                              (map (fn [clj] [:class clj]))
                              set))}))

(comment
  (require '[clojure.java.io :as io])
  (into {} (analyser/file-info
            (io/file "src/leiningen/repack/analyser/clj.clj"))))
