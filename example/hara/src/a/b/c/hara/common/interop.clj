(ns hara.common.interop
  (:require [hara.common.lettering :as l]))

(defn invoke [obj method & args]
  (clojure.lang.Reflector/invokeInstanceMethod obj method (into-array Object args)))

(defn- into-object-pair [entry]
  [(->> entry first name l/capital-camel-case (str "set"))
   (second entry)])

(defn into-object [obj coll]
  (let [entries (map into-object-pair coll)]
    (doseq [[setter v] entries]
      (invoke obj setter v))
    obj))
