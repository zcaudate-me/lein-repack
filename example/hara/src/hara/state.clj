(ns hara.state
  (:require [clojure.string :as st]
            [hara.common.fn :refer [get-> eq->]]
            [hara.common.types :refer [atom? aref?]]))

;; ## IRef Functions

(defn hash-code
  "Returns the hash-code of the object

    (hash-code 1) => 1

    (hash-code :1) => 1013907437

    (hash-code \"1\") => 49
  "
  [obj]
  (.hashCode obj))

(defn hash-keyword
  "Returns a keyword repesentation of the hash-code.
   For use in generating internally unique keys

    (h/hash-keyword 1)
    ;=> :__1__
  "
  [obj & ids]
  (keyword (str "__" (st/join "_" (concat (map str ids) [(hash-code obj)])) "__")))

(defn hash-pair
  "Combines the hash of two objects together.

    (hash-pair 1 :1)
    ;=> :__1_1013907437__
  "
  [v1 v2]
  (hash-keyword v2 (hash-code v1)))

(defn set-value!
  "Change the value contained within a ref or atom.

    @(set-value! (atom 0) 1)
    ;=> 1

    @(set-value! (ref 0) 1)
    ;=> 1
  "
  [rf obj]
  (cond (atom? rf) (reset! rf obj)
        (aref? rf) (dosync (ref-set rf obj)))
  rf)

(defn alter!
  "Updates the value contained within a ref or atom using `f`.

    @(alter! (atom 0) inc)
    ;=> 1

    @(alter! (ref 0) inc)
    ;=> 1
  "
  [rf f & args]
  (cond (atom? rf) (apply swap! rf f args)
        (aref? rf) (dosync (apply alter rf f args)))
  rf)

(defn dispatch!
  "Updates the value contained within a ref or atom using another thread.

    (dispatch! (atom 0)
                (fn [x] (Thread/sleep 1000)
                        (inc x)))
    ;=> <future_call>
  "
  [ref f & args]
  (future
    (apply alter! ref f args)))

(declare add-change-watch
         make-change-watch)

(defn add-change-watch
  "Adds a watch function that only triggers when there is change
   in `(sel <value>)`.

    (def subject (atom {:a 1 :b 2}))
    (def observer (atom nil)
    (add-change-watch subject :clone
        :b (fn [& _] (reset! observer @a)))

    (swap! subject assoc :a 0)
    @observer => nil

    (swap! subject assoc :b 1)
    @observer => {:a 0 :b 1}
  "
  ([rf k f] (add-change-watch rf k identity f))
  ([rf k sel f]
     (add-watch rf k (make-change-watch sel f))))

(defn make-change-watch
  [sel f]
  (fn [k rf p n]
    (let [pv (get-> p sel)
          nv (get-> n sel)]
      (if-not (or (= pv nv) (nil? nv))
        (f k rf pv nv)))))

;; ## Latching

(defn latch-transform-fn
  [rf f]
  (fn [_ _ _ v]
    (set-value! rf (f v))))

(defn latch
  "Latches two irefs together so that when `master`
   changes, the `slave` will also be updated

    (def master (atom 1))
    (def slave (atom nil))

    (latch master slave #(* 10 %)
    (swap! master inc)
    @master ;=> 2
    @slave ;=> 20
  "
  ([master slave] (latch master slave identity))
  ([master slave f]
     (add-watch master
                (hash-pair master slave)
                (latch-transform-fn slave f))))

(defn latch-changes
  "Same as latch but only changes in `(sel <val>)` will be propagated
    (def master (atom {:a 1))
    (def slave (atom nil))

    (latch-changes master slave :a #(* 10 %)
    (swap! master update-in [:a] inc)
    @master ;=> {:a 2}
    @slave ;=> 20
  "
  ([master slave] (latch-changes master slave identity identity))
  ([master slave sel] (latch-changes master slave sel identity))
  ([master slave sel f]
     (add-change-watch master (hash-pair master slave)
                       sel (latch-transform-fn slave f))))

(defn delatch
  "Removes the latch so that updates will not be propagated"
  [master slave]
  (remove-watch master (hash-pair master slave)))

;; ## Concurrency Watch
(defn run-notify
  "Adds a notifier to a long running function so that it returns
   a promise that is accessible when the function has finished.
   updating the iref.

    (let [res (run-notify
             #(do (sleep 200)
                  (alter! % inc))
                  (atom 1)
                  notify-on-all)]
    res ;=> promise?
    @res ;=> atom?
    @@res ;=> 2)
  "
  [mtf ref notify-fn]
  (let [p (promise)
        pk (hash-keyword p)]
    (add-watch ref pk (notify-fn p pk))
    (mtf ref)
    p))

(defn notify-on-all
  "Returns a watch-callback function that waits
   for the ref to be updated then removes itself
   and delivers the promise"
  [p pk]
  (fn [_ ref _ _]
    (remove-watch ref pk)
    (deliver p ref)))

(defn notify-on-change
  "Returns a watch-callback function that waits
   for the ref to be updated, checks if the `(sel <value>)`
   has been updated then removes itself and delivers the promise"
  ([] (notify-on-change identity))
  ([sel]
     (fn [p pk]
       (fn [k ref old new]
         (when-not (eq-> old new sel)
           (remove-watch ref pk)
           (deliver p ref))))))

(defn wait-deref
  "A nicer interface for `deref`"
  ([p] (wait-deref p nil nil))
  ([p ms] (wait-deref p ms nil))
  ([p ms ret]
     (cond (nil? ms) (deref p)
           :else (deref p ms ret))))

(defn wait-for
  "Waits for a long running multithreaded function to update the ref.
   Used for testing purposes

    (def atm (atom 1))
    ;; concurrent call
    (def f #(dispatch! % slow-inc))
    (def ret (wait-for f atm))

    @atm ;=> 2
    @ret ;=> 2
  "
  ([mtf ref] (wait-for mtf ref notify-on-all nil nil))
  ([mtf ref notifier ms] (wait-for mtf ref notifier ms nil))
  ([mtf ref notifier ms ret]
     (wait-deref (run-notify mtf ref notifier) ms ret)))

(defn wait-on
  "A redundant function. Used for testing purposes. The same as
   `(alter! ref f & args)` but the function is wired with the
   notification scheme.

    (def atm (wait-on slow-inc (atom 1)))
    (@atm => 2)
  "
  [f ref & args]
  (wait-for #(apply dispatch! % f args) ref))
