(ns hara.import
  (:refer-clojure :exclude [import]))

(defn import-var [name ^clojure.lang.Var var]
  (if (.hasRoot var)
    (intern *ns* (with-meta name (merge (meta var)
                                        (meta name)))
            @var)))

(defn import-namespace
  ([ns] (import-namespace ns nil))
  ([ns vars]
     (let [all-vars (ns-publics ns)
           selected-vars (if vars
                            (select-keys all-vars vars)
                            all-vars)]
       (doseq [[n v] selected-vars]
         (import-var n v)))))

(defmacro import [nsp vars & more]
  `(do
     (require (quote ~nsp))
     (import-namespace
      (quote ~nsp)
      ~(if-not (= :all vars)
         `(quote ~vars)))
     ~(if more
        `(import ~@more))))

(defn ns-vars [ns]
  (vec (sort (keys (ns-publics ns)))))
