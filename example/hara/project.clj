(defproject im.chit/hara "1.1.0-SNAPSHOT"
  :description "General purpose utilities library "
  :url "http://github.com/zcaudate/hara"
  :license {:name "The MIT License"
            :url "http://http://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.5.1"]]
  :codox {:include [a.b.c.hara.common]}
  :repack {:root a
           :exclude []
           :levels 4
           :name-fn (fn [name package] (clojure.string/join "." (cons name (drop 3 (clojure.string/split package #"\.")))))}
  :profiles {:dev {:dependencies [[midje "1.6.3"]
                                  [clj-time "0.6.0"]
                                  [im.chit/korra "0.1.0"]]
                   :plugins [[lein-midje "3.1.3"]
                             [lein-repack "0.1.4-SNAPSHOT"]]}})
