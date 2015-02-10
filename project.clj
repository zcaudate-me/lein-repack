(defproject lein-repack "0.2.9"
  :description "Repack your project for deployment"
  :url "https://www.github.com/zcaudate/lein-repack"
  :license {:name "The MIT License"
            :url "http://http://opensource.org/licenses/MIT"}
  :eval-in-leiningen true
  :dependencies [[im.chit/vinyasa.maven "0.3.2"]
	               [version-clj "0.1.2"]
                 [rewrite-clj "0.3.9"]
								 ;;[rewrite-clj "0.4.11"]
								 ]
  :profiles {:dev {:dependencies [[midje "1.6.3"]]
                   :plugins [[lein-midje "3.1.3"]]}})
