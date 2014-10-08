(ns leiningen.repack.analyser.cljs-test
  (:use midje.sweet)
  (:require [leiningen.repack.analyser.cljs :refer :all]
            [leiningen.repack.analyser :as analyser]
            [clojure.java.io :as io]))

(fact "behavior of the cljs analyser"
  (analyser/file-info
   (io/file "example/repack.advance/src/cljs/repack/web.cljs"))
  => '{:exports #{[:cljs repack.web]}
       :imports #{[:cljs repack.web.client] [:clj repack.core]}})
