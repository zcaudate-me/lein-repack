(defproject im.chit/repack.advance "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [im.chit/korra "0.1.2"]]
  :profiles {:dev {:plugins [[lein-repack "0.2.5"]]}}
  :repack [{:type :clojure
            :levels 2
            :path "src/clj"
            :standalone #{"web"}}
           {:type :clojure
            :levels 2
            :path "src/cljs"
            :standalone #{"web"}}
           {:subpackage "jvm"
            :path "java/im/chit/repack"
            :distribute {"common" #{"common"}
                         "web"    #{"web"}}}
           {:subpackage "resources"
            :path "resources"
            :distribute {"common" #{"common"}
                         "web"    #{"web"}}
            :dependents #{"core"}}])
