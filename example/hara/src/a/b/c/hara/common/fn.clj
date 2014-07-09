(ns hara.common.fn
  (:require [hara.common.error :refer [suppress]]
            [hara.common.types :refer [hash-map? hash-set?]]))

;; ## Calling Conventions
;;
;; Adds more flexibility to how functions can be called.
;; `call` adds a level of indirection and allows the function
;; to not be present, returning nil instead. `msg` mimicks the way
;; that object-orientated languages access their functions.
;;

(defn call
  "Executes `(f v1 ... vn)` if `f` is not nil

    (call nil 1 2 3) ;=> nil

    (call + 1 2 3) ;=> 6
  "
  ([f] (if-not (nil? f) (f)) )
  ([f v] (if-not (nil? f) (f v)))
  ([f v1 v2] (if-not (nil? f) (f v1 v2)))
  ([f v1 v2 v3] (if-not (nil? f) (f v1 v2 v3)))
  ([f v1 v2 v3 v4 ] (if-not (nil? f) (f v1 v2 v3 v4)))
  ([f v1 v2 v3 v4 & vs] (if-not (nil? f) (apply f v1 v2 v3 v4 vs))))

(defn msg
  "Message dispatch for object orientated type calling convention.

    (def obj {:a 10
              :b 20
              :get-sum (fn [this]
                        (+ (:b this) (:a this)))})

    (msg obj :get-sum) ;=> 30
  "
  ([obj kw] (call (obj kw) obj))
  ([obj kw v] (call (obj kw) obj v))
  ([obj kw v1 v2] (call (obj kw) obj v1 v2))
  ([obj kw v1 v2 v3] (call (obj kw) obj v1 v2 v3))
  ([obj kw v1 v2 v3 v4] (call (obj kw) obj v1 v2 v3 v4))
  ([obj kw v1 v2 v3 v4 & vs] (apply call (obj kw) obj v1 v2 v3 v4 vs)))

(defn T [& args] true)

(defn F [& args] false)


;; ## Function Representation
;;
;; Usually in clojure programs, the most common control structure that
;; is used is the `->` and `->>` macros. This is because a function can
;; be view as a series of smaller functional transforms
;;
;; A very important part of this pipeling style of programming can be seen
;; in how predicates are tested. They tend to be quite short, as in:
;;
;;  - `(< x 3)`
;;  - `(< (:a obj) 3)`
;;  - `(-> obj t1 t2 (< 3))`
;;
;; In general, they are written as:
;;
;;  - `(-> x t1 t2 pred)`
;;
;; It is worth keeping the predicates as data structures because
;; as they act as more than just functions. They can be used
;; for conditions, selections and filters when in the right
;; context. Although the form can only represent pipelines, it is enough to
;; cover most predicates and blurs the line between program and data.
;;

(defn call->
  "Indirect call, takes `obj` and a list containing either a function,
   a symbol representing the function or the symbol `?` and any additional
   arguments. Used for calling functions that have been stored as symbols.

     (call-> 1 '(+ 2 3 4)) ;=> 10

     (call-> 1 '(< 2)) ;=> true

     (call-> 1 '(? < 2)) ;=> true

     (call-> {:a {:b 1}} '((get-in [:a :b]) = 1))
     ;=> true
   "
  [obj [ff & args]]
  (cond (nil? ff)     obj
        (list? ff)    (recur (call-> obj ff) args)
        (vector? ff)  (recur (get-in obj ff) args)
        (keyword? ff) (recur (get obj ff) args)
        (fn? ff)      (apply ff obj args)
        (symbol? ff)  (if-let [f (suppress (resolve ff))]
                        (apply call f obj args)
                        (recur (get obj ff) args))
        :else         (recur (get obj ff) args)))

(defn get->
  "Provides a shorthand way of getting a return value.
   `sel` can be a function, a vector, or a value.

    (get-> {:a {:b {:c 1}}} :a) => {:b {:c 1}}

    (get-> {:a {:b {:c 1}}} [:a :b]) => {:c 1}
  "
  [obj sel]
  (cond (nil? sel)    obj
        (list? sel)   (call-> obj sel)
        (vector? sel) (get-in obj sel)
        (symbol? sel) (if-let [f (suppress (resolve sel))]
                        (call f obj)
                        (get obj sel))
        (ifn? sel)    (sel obj)
        :else         (get obj sel)))

(defn make-exp
  "Makes an expression using `sym`

    (make-exp 'y (?? str)) ;=> '(str y)

    (make-exp 'x (?? (inc) (- 2) (+ 2)))
    ;=> '(+ (- (inc x) 2) 2))
  "
  [sym [ff & more]]
  (cond (nil? ff)     sym
        (list? ff)    (recur (make-exp sym ff) more)
        (vector? ff)  (recur (list 'get-in sym ff) more)
        (keyword? ff) (recur (list 'get sym ff) more)
        (fn? ff)      (apply list ff sym more)
        (symbol? ff)  (apply list ff sym more)
        :else         (recur (list 'get sym ff) more)))

(defn make-fn-exp
  "Makes a function expression out of the form

    (make-fn-exp '(+ 2))
    ;=> '(fn [?%] (+ ?% 2))
  "
  [form]
  (apply list 'fn ['?%]
         (list (make-exp '?% form))))

(defn fn->
  "Constructs a function from a form representation.

    ((fn-> '(+ 10)) 10) ;=> 20
  "
  [form]
  (eval (make-fn-exp form)))

(defmacro ?%
  "Constructs a function of one argument, Used for predicate

    ((?% < 4) 3) ;=> true

    ((?% > 2) 3) ;=> true
  "
  [& args]
  (make-fn-exp args))

;; ## Predicate Checking


(defn check
  "Returns `true` when `v` equals `chk`, or if `chk` is a function, `(chk v)`

    (check 2 2) ;=> true

    (check 2 even?) ;=> true

    (check 2 '(< 1)) ;=> true

    (check {:a {:b 1}} (?? ([:a :b]) = 1)) ;=> true
  "
  [obj chk]
  (or (= obj chk)
      (-> (get-> obj chk) not not)))

(defn check->
  "Returns `true` if `(sel obj)` satisfies `check`

    (check-> {:a {:b 1}} :a hash-map?) ;=> true

    (check-> {:a {:b 1}} [:a :b] 1) ;=> true
  "
  [obj sel chk]
  (check (get-> obj sel) chk))

(defn check-all->
  "Returns `true` if `obj` satisfies all pairs of sel and chk

    (check-all-> {:a {:b 1}}
                 [:a {:b 1} :a hash-map?])
    => true
  "
  [obj scv]
  (every? (fn [[sel chk]]
            (check-> obj sel chk))
          (partition 2 scv)))

(defn eq->
  "A shortcut to compare if two vals are equal.

      (eq-> {:id 1 :a 1} {:id 1 :a 2} :id)
      ;=> true

      (eq-> {:db {:id 1} :a 1}
            {:db {:id 1} :a 2} [:db :id])
      ;=> true
  "
  [obj1 obj2 sel]
  (= (get-> obj1 sel) (get-> obj2 sel)))

(defn pcheck->
  "Shorthand ways of checking where `m` fits `prchk`

    (pcheck-> {:a 1} :a) ;=> truthy

    (pcheck-> {:a 1 :val 1} [:val 1]) ;=> true

    (pcheck-> {:a {:b 1}} [[:a :b] odd?]) ;=> true
  "
  [obj pchk]
  (cond (vector? pchk)
        (check-all-> obj pchk)

        (hash-set? pchk)
        (some (map #(pcheck-> obj %) pchk))

        :else
        (check obj pchk)))

(defn suppress-pcheck
  "Tests obj using prchk and returns `obj` or `res` if true

    (suppress-pcheck :3 even?) => nil

    (suppress-pcheck 3 even?) => nil

    (suppress-pcheck 2 even?) => true
  "
  ([obj prchk] (suppress-pcheck obj prchk true))
  ([obj prchk res]
     (suppress (if (pcheck-> obj prchk) res))))
     

(defn arg-counts [f]
 (let [ms (filter #(= "invoke" (.getName %))
                  (.getDeclaredMethods (class f)))
       ps (map (fn [m] (.getParameterTypes m)) ms)]
   (map alength ps)))

(defn varg-count [f]
 (if (some #(= "getRequiredArity" (.getName %))
           (.getDeclaredMethods (class f)))
   (.getRequiredArity f)))


(defn- op-max-args
 ([counts cargs] (op-max-args counts cargs nil))
 ([counts cargs res]
    (if-let [c (first counts)]
      (if (= c cargs) c
          (recur (next counts) cargs
                 (if (and res (> res c))
                   res c)))
      res)))

(defn op [f & args]
 (let [vc    (varg-count f)
       cargs (count args)]
   (if (and vc (> cargs vc))
     (apply f args)
     (let [cs (arg-counts f)
           amax (op-max-args cs cargs)]
       (if amax
         (apply f (take amax args)))))))

