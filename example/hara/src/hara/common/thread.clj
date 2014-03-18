(ns hara.common.thread)

(defn current-thread
  "Returns the currenly executing thread."
  []
  (Thread/currentThread))

(defn sleep
  "Shortcut for Thread/sleep.

    (sleep 100) ;=> <sleeps for 100ms>.
  "
  [ms]
  (Thread/sleep ms))

(defn yield
  "Yields control of the currently executing thread."
  []
  (Thread/yield))

(defn interrupt
  "Interrupts a `thd` or the current thread
   if no arguments are given.
  "
  ([] (interrupt (current-thread)))
  ([thd] (.interrupt thd)))