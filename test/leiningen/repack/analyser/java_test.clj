(ns leiningen.repack.analyser.java-test
  (:use midje.sweet)
  (:require [leiningen.repack.analyser.java :refer :all]
            [leiningen.repack.analyser :as analyser]
            [clojure.java.io :as io]))

^{:refer leiningen.repack.analyser.java/get-class :added "0.1.5"}
(fact "grabs the symbol of the class in the java file"
  (get-class (io/file "example/repack.advance/java/im/chit/repack/common/Hello.java"))
  => 'im.chit.repack.common.Hello)

^{:refer leiningen.repack.analyser.java/get-imports :added "0.1.5"}
(fact "grabs the symbol of the class in the java file"
  (get-imports (io/file "example/repack.advance/java/im/chit/repack/common/Hello.java"))
  => ()

  (get-imports (io/file "example/repack.advance/java/im/chit/repack/web/Client.java"))
  => '(im.chit.repack.common.Hello))

^{:refer leiningen.repack.analyser/file-info :added "0.1.5"}
(fact "behavior of the java analyser"
  (analyser/file-info
   (io/file "example/repack.advance/java/im/chit/repack/web/Client.java"))
  => (contains '{:exports #{[:class im.chit.repack.web.Client]}
                 :imports #{[:class im.chit.repack.common.Hello]}}))
