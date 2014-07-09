(ns hara.common.string)

;; ## String
;;
;; Functions that should be in `clojure.string` but are not.
;;

(defn replace-all
  "Returns a string with all instances of `old` in `s` replaced with
   the value of `new`.

    (replace-all \"hello there, hello again\"
                   \"hello\" \"bye\")
    ;=> \"bye there, bye again\"
  "
  [s old new]
  (.replaceAll s old new))

(defn starts-with?
  "Returns `true` if `s` begins with `pre`.

    (starts-with? \"prefix\" \"pre\") ;=> true

    (starts-with? \"prefix\" \"suf\") ;=> false
  "
  [s pre]
  (.startsWith s pre))

(defn ends-with?
  "Returns `true` if `s` begins with `pre`.

    (ends-with? \"suffix\" \"fix\") ;=> true
  "
  [s suf]
  (.endsWith s suf))