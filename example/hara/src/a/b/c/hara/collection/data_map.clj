(ns hara.collection.data-map
  (:require [clojure.set :as set]
            [hara.common.error :refer [error]]
            [hara.common.fn :refer [eq-> check pcheck->]]
            [hara.common.types :refer [hash-map? hash-set?]]))

(defn combine-obj
  "Looks for the value within the set `s` that matches `v` when
   `sel` is applied to both.

    (combine-obj #{1 2 3} 2 identity)
    ;=> 2

    (combine-obj #{{:id 1}} {:id 1 :val 1} :id)
    ;=> {:id 1}
  "
  [s v sel]
  (if-let [sv (first s)]
    (if (eq-> sv v sel)
      sv
      (recur (next s) v sel))))

(defn combine-to-set
  "Returns `s` with either v added or combined to an existing set member.

    (combine-to-set #{{:id 1 :a 1} {:id 2}}
                    {:id 1 :b 1}
                    :id merge)
    ;=> #{{:id 1 :a 1 :b} {:id 2}}
  "
  [s v sel rd]
  (if-let [sv (combine-obj s v sel)]
    (conj (disj s sv) (rd sv v))
    (conj s v)))

(defn combine-sets
  "Returns the combined set of `s1` and `s2`.

    (combine-sets #{{:id 1} {:id 2}}
                  #{{:id 1 :val 1} {:id 2 :val 2}}
                  :id merge)
    ;=> #{{:id 1 :val 1} {:id 2 :val 2}}
  "
  [s1 s2 sel rd]
  (if-let [v (first s2)]
    (recur (combine-to-set s1 v sel rd) (next s2) sel rd)
    s1))

(defn combine-internal
  "Combines elements in `s` using rules defined by `sel` and `rd`.

    (combine-internal #{{:id 1} {:id 2} {:id 1 :val 1} {:id 2 :val 2}}
                      :id merge)
    ;=> #{{:id 1 :val 1} {:id 2 :val 2}}
  "
  [s sel rd]
  (if-not (hash-set? s) s
          (combine-sets #{} s sel rd)))

(defn combine
  "Generic function that looks at `v1` and `v2`, which can be either
   values or sets of values and merges them into a new set.

    (combine 1 2) ;=> #{1 2}

    (combine #{1} 1) ;=> #{1}

    (combine #{{:id 1} {:id 2}}
             #{{:id 1 :val 1} {:id 2 :val 2}}
             :id merge)
    ;=> #{{:id 1 :val 1} {:id 2 :val 2}}

   "
  ([v1 v2]
     (cond (nil? v2) v1
           (nil? v1) v2
           (hash-set? v1)
           (cond (hash-set? v2)
                 (set/union v1 v2)
                 :else (conj v1 v2))
           :else
           (cond (hash-set? v2)
                 (conj v2 v1)

                 (= v1 v2) v1
                 :else #{v1 v2})))
  ([v1 v2 sel rd]
     (-> (cond (nil? v2) v1
               (nil? v1) v2
               (hash-set? v1)
               (cond (hash-set? v2)
                     (combine-sets v1 v2 sel rd)

                     :else (combine-to-set v1 v2 sel rd))
               :else
               (cond (hash-set? v2)
                     (combine-to-set v2 v1 sel rd)

                     (eq-> v1 v2 sel)
                     (rd v1 v2)

                     (= v1 v2) v1
                     :else #{v1 v2}))
         (combine-internal sel rd))))

(defn decombine
  "Returns `v` without every single member of `dv`.

    (decombine 1 1) => nil

    (decombine 1 2) => 1

    (decombine #{1} 1) => nil

    (decombine #{1 2 3 4} #{1 2}) => #{3 4}

    (decombine #{1 2 3 4} even?) => #{1 3}
  "
  [v dv]
  (cond (hash-set? v)
        (let [res (cond (hash-set? dv)
                        (set/difference v dv)

                        (ifn? dv)
                        (set (filter (complement dv) v))

                        :else (disj v dv))]
          (if-not (empty? res) res))
        :else
        (if-not (check v dv) v)))

(defn merges
  "Like `merge` but works across sets and will also
   combine duplicate key/value pairs together into sets of values.

    (merges {:a 1} {:a 2}) ;=> {:a #{1 2}}

    (merges {:a #{{:id 1 :val 1}}}
            {:a {:id 1 :val 2}}
            :id merges)
    ;=> {:a #{{:id 1 :val #{1 2}}}}

  "
  ([m1 m2] (merges m1 m2 identity combine nil))
  ([m1 m2 sel] (merges m1 m2 sel combine nil))
  ([m1 m2 sel rd] (merges m1 m2 sel rd nil))
  ([m1 m2 sel rd output]
     (if-let [[k v] (first m2)]
       (recur (dissoc m1 k) (rest m2) sel rd
              (assoc output k (combine (get m1 k) v sel rd)))
       (merge m1 output))))

(defn merges-in
  "Like `merges` but works on nested maps

    (merges-in {:a {:b 1}} {:a {:b 2}})
    ;=> {:a {:b #{1 2}}}

    (merges-in {:a #{{:foo #{{:bar #{{:baz 1}}}}}}}
               {:a #{{:foo #{{:bar #{{:baz 2}}}}}}}
               hash-map?
               merges-in)
    => {:a #{{:foo #{{:bar #{{:baz 2}}}
                     {:bar #{{:baz 1}}}}}}}
  "
  ([m1 m2] (merges-in m1 m2 identity combine {}))
  ([m1 m2 sel] (merges-in m1 m2 sel combine {}))
  ([m1 m2 sel rd] (merges-in m1 m2 sel rd {}))
  ([m1 m2 sel rd output]
     (if-let [[k v2] (first m2)]
       (let [v1 (m1 k)]
         (cond (not (and (hash-map? v1) (hash-map? v2)))
               (recur (dissoc m1 k) (rest m2) sel rd
                      (assoc output k (combine v1 v2 sel rd)))
               :else
               (recur (dissoc m1 k) (rest m2) sel rd
                            (assoc output k (merges-in v1 v2 sel rd)))))
       (merge m1 output))))

(defn merges-in*
  "Like `merges-in but can recursively merge nested sets.

    h/merges-in* {:a #{{:id 1 :foo
                              #{{:id 2 :bar
                                       #{{:id 3 :baz 1}}}}}}}
                 {:a #{{:id 1 :foo
                              #{{:id 2 :bar
                                       #{{:id 3 :baz 2}}}}}}}
                 :id)
    ;=> {:a #{{:id 1 :foo
                     #{{:id 2 :bar
                              #{{:id 3 :baz #{1 2}}}}}}}}
"
  ([m1 m2] (merges-in* m1 m2 hash-map? combine {}))
  ([m1 m2 sel] (merges-in* m1 m2 sel combine {}))
  ([m1 m2 sel rd] (merges-in* m1 m2 sel rd {}))
  ([m1 m2 sel rd output]
     (cond (hash-map? m1)
           (if-let [[k v2] (first m2)]
             (let [v1 (m1 k)
                   nm1 (dissoc m1 k)
                   nm2 (rest m2)]
               (cond (and (hash-map? v1) (hash-map? v2))
                     (recur nm1 nm2 sel rd
                            (assoc output k (merges-in* v1 v2 sel rd)))

                     (or (hash-set? v1) (hash-set? v2))
                     (recur nm1 nm2 sel rd
                            (assoc output k
                                   (combine v1 v2 sel
                                            #(merges-in* %1 %2 sel rd))))
                     :else
                     (recur nm1 nm2 sel rd
                            (assoc output k (rd v1 v2)))))
             (merge m1 output))

           :else
           (combine m1 m2))))

(defn assocs
  "Similar to `assoc` but conditions of association is specified
  through `sel` (default: `identity`) and well as merging specified
  through `rd` (default: `combine`).

    (assocs {:a #{1}} :a #{2 3 4}) ;=> {:a #{1 2 3 4}}

    (assocs {:a {:id 1}} :a {:id 1 :val 1} :id merge)
    ;=> {:a {:val 1, :id 1}}

    (assocs {:a #{{:id 1 :val 2}
                  {:id 1 :val 3}}} :a {:id 1 :val 4} :id merges)
    ;=> {:a #{{:id 1 :val #{2 3 4}}}})

  "
  ([m k v] (assocs m k v identity combine))
  ([m k v sel rd]
     (let [z (get m k)]
       (cond (nil? z) (assoc m k v)
             :else
             (assoc m k (combine z v sel rd))))))

(defn dissocs
  "Similar to `dissoc` but allows dissassociation of sets of values from a map.

    (dissocs {:a 1} :a) ;=> {}

    (dissocs {:a #{1 2}} [:a #{0 1}]) ;=> {:a #{2}}

    (dissocs {:a #{1 2}} [:a #{1 2}]) ;=> {}
  "
  [m k]
  (cond (vector? k)
        (let [[k v] k
              z (get m k)
              res (decombine z v)]
          (if (nil? res)
            (dissoc m k)
            (assoc m k res)))
        :else
        (dissoc m k)))

(defn gets
  "Returns the associated values either specified by a key or a key and predicate pair.

    (gets {:a 1} :a) => 1

    (gets {:a #{0 1}} [:a zero?]) => #{0}

    (gets {:a #{{:b 1} {}}} [:a :b]) => #{{:b 1}}
  "
  [m k]
  (if-not (vector? k) (get m k)
          (let [[k prchk] k
                val (get m k)]
            (if-not (hash-set? val) val
                    (-> (filter #(pcheck-> % prchk) val) set)))))

(declare gets-in gets-in-loop)

(defn gets-in
  "Similar in style to `get-in` with operations on sets. Returns a set of values.

    (gets-in {:a 1} [:a]) => #{1}

    (gets-in {:a 1} [:b]) => #{}

    (gets-in {:a #{{:b 1} {:b 2}}} [:a :b]) => #{1 2}
  "
  [m ks]
  (-> (gets-in-loop m ks) set (disj nil)))

(defn- gets-in-loop
  [m [k & ks :as all-ks]]
  (cond (nil? ks)
        (let [val (gets m k)]
          (cond (hash-set? val) val
                :else (list val)))
        :else
        (let [val (gets m k)]
          (cond (hash-set? val)
                (apply concat (map #(gets-in-loop % ks) val))
                :else (gets-in-loop val ks)))))

(declare assocs-in
         assocs-in-keyword assocs-in-filtered)

(defn assocs-in
  "Similar to assoc-in but with power of moving through sets

    (h/assocs-in {:a {:b 1}} [:a :b] 2)
    ;=> {:a {:b #{1 2}}}

    (h/assocs-in {:a #{{:b 1}}} [:a :b] 2)
    ;=> {:a #{{:b #{1 2}}}}

    (h/assocs-in {:a #{{:b {:id 1}} {:b {:id 2}}}}
                 [:a [:b [:id 1]] :c] 2)
    ;=> {:a #{{:b {:id 1 :c 2}} {:b {:id 2}}}}
  "
  ([m all-ks v] (assocs-in m all-ks v identity combine))
  ([m [k & ks :as all-ks] v sel rd]
     (cond (nil? ks)
           (cond (vector? k) (error "cannot allow vector-form on last key " k)
                 (or (nil? m) (hash-map? m)) (assocs m k v sel rd)
                 (nil? k) (combine m v sel rd)
                 :else (error m " is not an associative map"))

           (or (nil? m) (hash-map? m))
           (cond (vector? k) (assocs-in-filtered m all-ks v sel rd)
                 :else
                 (let [val (get m k)]
                   (cond (hash-set? val)
                         (assoc m k (set (map #(assocs-in % ks v sel rd) val)))
                         :else (assoc m k (assocs-in val ks v sel rd)))))
           :else (error m " is required to be a map"))))

(defn assocs-in-filtered
  ([m all-ks v] (assocs-in-filtered m all-ks v identity combine))
  ([m [[k prchk] & ks :as all-ks] v sel rd]
     (let [subm (get m k)]
       (cond (nil? subm) m

             (and (hash-set? subm) (every? hash-map? subm))
             (let [ori-set (set (filter #(pcheck-> % prchk) subm))
                   new-set (set (map #(assocs-in % ks v sel rd) ori-set))]
               (assoc m k (-> subm
                              (set/difference ori-set)
                              (set/union new-set))))

             (hash-map? subm)
             (if (pcheck-> subm prchk)
               (assoc m k (assocs-in subm ks v sel rd))
               m)

             :else (error subm "needs to be hash-map or hash-set")))))

(declare dissocs-in dissocs-in-filtered)

(defn dissocs-in
  "Similiar to `dissoc-in` but with sets manipulation.

    (dissocs-in {:a #{{:b 1 :c 1} {:b 2 :c 2}}}
                [:a :b])
    ;=> {:a #{{:c 1} {:c 2}}}

    (dissocs-in {:a #{{:b #{1 2 3} :c 1}
                      {:b #{1 2 3} :c 2}}}
                [[:a [:c 1]] [:b 1]])
    ;=> {:a #{{:b #{2 3} :c 1} {:b #{1 2 3} :c 2}}}
  "
  [m [k & ks :as all-ks]]
  (cond (nil? ks) (dissocs m k)

        (vector? k) (dissocs-in-filtered m all-ks)

        :else
        (let [val (get m k)]
          (cond (hash-set? val)
                (assoc m k (set (map #(dissocs-in % ks) val)))
                :else (assoc m k (dissocs-in m ks))))))

(defn dissocs-in-filtered
  ([m [[k prchk] & ks :as all-ks]]
     (let [subm (get m k)]
       (cond (nil? subm) m
             (and (hash-set? subm) (every? hash-map? subm))
             (let [ori-set (set (filter #(pcheck-> % prchk) subm))
                   new-set (set (map #(dissocs-in % ks) ori-set))]
               (assoc m k (-> subm
                              (set/difference ori-set)
                              (set/union new-set))))

             (hash-map? subm)
             (if (pcheck-> subm prchk)
               (assoc m k (dissocs-in subm ks))
               m)

             :else (error subm "needs to be hash-map or hash-set")))))
