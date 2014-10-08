(ns leiningen.repack.data.sort)

(defn all-branch-nodes [manifest]
  (->> (:branches manifest)
       (map (fn [[k m]]
              (-> m
                  (select-keys [:coordinate :dependencies])
                  (assoc :id k))))))

(defn all-branch-deps [manifest]
  (->> (:branches manifest)
       (map (fn [[k m]] (:coordinate m)))
       (set)))

(defn topsort-branch-deps-pass
  [all sl]
  (reduce (fn [out i]
            (if (some all (:dependencies i))
              out
              (conj out i))) [] sl))

(defn topsort-branch-deps [manifest]
  (let [sl  (all-branch-nodes manifest)
        all (all-branch-deps manifest)]
    (loop [all all
           sl  sl
           output []]
      (if-not (or (empty? sl)
                  (empty? all))
        (let [pass (topsort-branch-deps-pass all sl)]
          (recur
           (apply disj all (map :coordinate pass))
           (filter (fn [x] (some #(not= x %) pass) ) sl)
           (conj output pass)))
        output))))