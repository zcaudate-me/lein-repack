(ns leiningen.repack.analyser.cljs
  (:require [leiningen.repack.analyser.clj :refer [get-namespaces]]
            [leiningen.repack.analyser :as analyser]
            [clojure.set :as set]
            [clojure.java.io :as io]))

(defmethod analyser/file-info :cljs
  [file]
  (let [[[_ ns & body] & forms]
        (read-string (str "[" (-> file io/reader slurp) "]"))]
    {:exports #{[:cljs ns]}
     :imports (set/union (->> body
                              (mapcat #(get-namespaces % [:use :require]))
                              (map (fn [clj] [:cljs clj]))
                              set)
                         (->> body
                              (mapcat #(get-namespaces % [:use-macros :require-macros]))
                              (map (fn [clj] [:clj clj]))
                              set))}))
