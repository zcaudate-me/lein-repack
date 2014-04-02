(ns leiningen.repack.split
  (:require [rewrite-clj.zip :as z]
            [korra.common :refer [*sep*]]
            [clojure.java.io :as io]
            [leiningen.repack.manifest :as manifest]
            [leiningen.jar :as jar]
            [leiningen.install :as install]
            [leiningen.core.project :as project]))