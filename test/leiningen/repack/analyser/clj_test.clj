(ns leiningen.repack.analyser.clj-test
  (:use midje.sweet)
  (:require [leiningen.repack.analyser.clj :refer :all]
            [leiningen.repack.analyser :as analyser]
            [clojure.java.io :as io]))

^{:refer leiningen.repack.analyser.clj/get-namespaces :added "0.1.5"}
(fact "gets the namespaces of a clojure s declaration"

  (get-namespaces '(:require repack.util.array
                             [repack.util.data]) [:use :require])
  => '(repack.util.array repack.util.data)

  (get-namespaces '(:require [repack.util.array :refer :all])
                  [:use :require])
  => '(repack.util.array)

  (get-namespaces '(:require [repack.util
                              [array :as array]
                              data]) [:use :require])
  => '(repack.util.array repack.util.data))

^{:refer leiningen.repack.analyser.clj/get-imports :added "0.1.5"}
(fact "gets the class imports of a clojure ns declaration"

  (get-imports '(:import java.lang.String
                         java.lang.Class))
  => '(java.lang.String java.lang.Class)

  (get-imports '(:import [java.lang String Class]))
  => '(java.lang.String java.lang.Class))


^{:refer leiningen.repack.analyser.clj/get-genclass :added "0.1.5"}
(fact "gets the gen-class of a clojure ns declaration"

  (get-genclass 'hello '[(:gen-class :name im.chit.hello.MyClass)])
  => '[im.chit.hello.MyClass]

  (get-genclass 'hello '[(:import im.chit.hello.MyClass)])
  => nil)

^{:refer leiningen.repack.analyser.clj/get-defclass :added "0.1.5"}
(fact "gets all the defclass and deftype definitions in a set of forms"

  (get-defclass 'hello '[(deftype Record [])
                         (defrecord Database [])])
  => '(hello.Record hello.Database))


^{:refer leiningen.repack.analyser.clj/file-info :added "0.1.5"}
(fact "behavior of the clj analyser"
  (analyser/file-info
   (io/file "example/repack.advance/src/clj/repack/web/client.clj"))
  => '{:exports #{[:clj repack.web.client]
                  [:java repack.web.client.Main]
                  [:java repack.web.client.Client]}
       :imports #{[:clj repack.core]}})
