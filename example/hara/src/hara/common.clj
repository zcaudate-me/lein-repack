;; ## Common Paradigms
;;
;; `hara.common` provides methods, macros and utility
;; functions that complements `clojure.core` and makes programming
;; in clojure more "clojurish". Each function is not that useful
;; on its own but together, they span a number of paradigms and
;; adds flexibility to program structure and control. The main
;; functionality are:

(ns hara.common
  (:require [hara.import :refer [import]])
  (:refer-clojure :exclude [import send if-let]))

(import hara.common.collection  :all
        hara.common.constructor :all
        hara.common.control     :all
        hara.common.debug       :all
        hara.common.error       :all
        hara.common.fn          :all
        hara.common.interop     :all
        hara.common.keyword     :all
        hara.common.lettering   :all
        hara.common.string      :all
        hara.common.thread      :all
        hara.common.types       :all)
