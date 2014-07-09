(ns hara.common.debug)

;; ## Threads
;;
;; Simple functions for thread that increase readability.
;;

(defmacro time-ms
  "Evaluates expr and outputs the time it took.  Returns the time in ms

    (time-ms (inc 1)) ;=> 0.008

    (time-ms (Thread/sleep 100)) ;=> 100.619
  "
  [expr]
  `(let [start# (. System (nanoTime))
         ret# ~expr]
     (/ (double (- (. System (nanoTime)) start#)) 1000000.0)))