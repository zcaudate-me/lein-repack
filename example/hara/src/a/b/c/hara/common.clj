;; ## Common Paradigms
;;
;; `a.b.c.hara.common` provides methods, macros and utility
;; functions that complements `clojure.core` and makes programming
;; in clojure more "clojurish". Each function is not that useful
;; on its own but together, they span a number of paradigms and
;; adds flexibility to program structure and control. The main
;; functionality are:

(ns a.b.c.hara.common
  (:require [a.b.c.hara.import :refer [import]]
            [a.b.c.hara.common.collection]
            [a.b.c.hara.common.constructor]
            [a.b.c.hara.common.control]    
            [a.b.c.hara.common.debug]      
            [a.b.c.hara.common.error]      
            [a.b.c.hara.common.fn]         
            [a.b.c.hara.common.interop]    
            [a.b.c.hara.common.keyword]    
            [a.b.c.hara.common.lettering]  
            [a.b.c.hara.common.string]     
            [a.b.c.hara.common.thread]     
            [a.b.c.hara.common.types])
  (:refer-clojure :exclude [import send if-let]))

(import a.b.c.hara.common.collection  :all
        a.b.c.hara.common.constructor :all
        a.b.c.hara.common.control     :all
        a.b.c.hara.common.debug       :all
        a.b.c.hara.common.error       :all
        a.b.c.hara.common.fn          :all
        a.b.c.hara.common.interop     :all
        a.b.c.hara.common.keyword     :all
        a.b.c.hara.common.lettering   :all
        a.b.c.hara.common.string      :all
        a.b.c.hara.common.thread      :all
        a.b.c.hara.common.types       :all)
