(defproject lein-repack "0.1.5-SNAPSHOT"
  :description "Repack your project for deployment"
  :url "https://www.github.com/zcaudate/lein-repack"
  :license {:name "The MIT License"
            :url "http://http://opensource.org/licenses/MIT"}
  ;;:eval-in-leiningen true
  :dependencies [[im.chit/korra "0.1.2"]
                 [im.chit/hara.sort "2.1.3"]
                 [lein-clojars "0.9.1"]
                 [rewrite-clj "0.3.9"]]
  :profiles {:dev {:dependencies [[midje "1.6.3"]
                                  [leiningen "2.4.3"]]
                   :plugins [[lein-midje "3.1.3"]]}})
