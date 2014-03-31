(ns leiningen.repack.test-maven
  (:require [leiningen.repack.maven :refer :all]
            [midje.sweet :refer :all]))

(fact "jar-by-coordinates"
  (jar-by-coordinates '[org.clojure/clojure "1.5.1"])
  => (str *local-repo* "/org/clojure/clojure/1.5.1/clojure-1.5.1.jar"))

(fact "pom-by-coordinates"
  (pom-by-coordinates '[org.clojure/clojure "1.5.1"])
  => (str *local-repo* "/org/clojure/clojure/1.5.1/clojure-1.5.1.pom"))

(fact "jar-contents"
  (count (jar-contents (str *local-repo* "/org/clojure/clojure/1.5.1/clojure-1.5.1.jar")))
  => 3149)

(fact "coordinate-contents"
  (count (coordinate-contents '[org.clojure/clojure "1.5.1"]))
  => 3149)

(fact "class-name->jar-resource-path"
  (class-name->jar-resource-path "java.lang.String")
  => "java/lang/String.class"

  (class-name->jar-resource-path "midje.sweet$fact")
  => "midje/sweet$fact.class")

(fact "namespace->jar-resource-path"
  (namespace->jar-resource-path "clojure.core")
  => "clojure/core.clj"

  (namespace->jar-resource-path "midje.sweet")
  => "midje/sweet.clj"

  (namespace->jar-resource-path "clj-time.core")
  => "clj_time/core.clj")

(fact "jar-by-path"
  (jar-by-path "clojure/core.clj")
  => [*lein-jar* "clojure/core.clj"]

  (jar-by-path "midje/sweet.clj")
  => [(str *local-repo* "/midje/midje/1.6.3/midje-1.6.3.jar") "midje/sweet.clj"])

(fact "jar-by-class"
  (jar-by-class String)
  => [(str (System/getProperty "java.home") "/lib/rt.jar") "java/lang/String.class"]

  (jar-by-class clojure.core$assoc)
  => [*lein-jar* "clojure/core$assoc.class"])

(fact "jar-by-namespace"
  (jar-by-namespace 'clojure.core)
  => [*lein-jar* "clojure/core.clj"])

(fact "jar-by-class"
  (jar-by-class org.sonatype.aether.repository.LocalRepository)
  => [*lein-jar* "org/sonatype/aether/repository/LocalRepository.class"])

(fact "maven-by-namespace"
  (maven-by-namespace 'clojure.core)
  => nil

  (maven-by-namespace 'midje)
  => nil)

(fact "maven-by-jar"
  (maven-by-jar "clojure/core.clj")
  => nil

  (maven-by-namespace 'midje.sweet)
  => '[midje/midje "1.6.3"])
