(ns hara.common.error)

;; ## Errors
;;
;; If we place too much importance on exceptions, exception handling code
;; starts littering through the control code. Most internal code
;; do not require definition of exception types as exceptions are
;; meant for the programmer to look at and handle.
;;
;; Therefore, the exception mechanism should get out of the way
;; of the code. The noisy `try .... catch...` control structure
;; can be replaced by a `suppress` statement so that errors can be
;; handled seperately within another function or ignored completely.
;;

(defmacro error
  "Throws an exception when called.

    (error \"This is an error\")
    ;=> (throws Exception)
  "
  ([e] `(throw (Exception. (str ~e))))
  ([e & more]
     `(throw (Exception. (str ~e ~@more)))))

(defn error-message
  "Returns the the error message associated with `e`.

    (error-message (Exception. \"error\")) => \"error\"
  "
  [e]
  (.getMessage e))

(defn error-stacktrace
  "Returns the the error message associated with `e`.
  "
  [e]
  (.getStackTrace e))

(defmacro suppress
  "Suppresses any errors thrown.

    (suppress (error \"Error\")) ;=> nil

    (suppress (error \"Error\") :error) ;=> :error
  "
  ([body]
     `(try ~body (catch Throwable ~'t)))
  ([body catch-val]
     `(try ~body (catch Throwable ~'t
                   (cond (fn? ~catch-val)
                         (~catch-val ~'t)
                         :else ~catch-val)))))