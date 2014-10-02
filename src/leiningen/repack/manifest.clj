(ns leiningen.repack.manifest)

(def *example*
  {:resources {:subpackage 'resources
               :path "resources"
               :distribute {'common #{"common"}
                            'web    #{"web"}}
               :dependents #{'core}}

   :java      {:subpackage 'jvm
               :path "java"
               :root 'im.chit.iroh
               :distribute {'common #{"common"}
                            'web    #{"web"}}}

   :clojure   {:default {:levels 1
                         :root 'iroh
                         :exclude []}

               :clj     {:path "src/clj"}
               :cljs    {:path "src/cljs"}
               :cljx    {:path "src/cljx"}}})
