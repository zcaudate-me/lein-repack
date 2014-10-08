# lein-repack

Repack your project for deployment and distribution

### Whats New

#### 0.2.3

 - Overhauled analyser to make it more extensible
 - Support for java, clojurescript and resources
 - Breaking changes to :repack project map. See [example](https://github.com/zcaudate/lein-repack/tree/master/example/repack.advance/project.clj) for typical use cases. 
 - `push` has [stopped working](http://grokbase.com/t/gg/clojure/149rrwe3nw/psa-clojars-scp-disabled-until-further-notice) so `lein repack deploy` has to be used instead.
 
#### 0.1.4

 - Added [feature](https://github.com/zcaudate/lein-repack/issues/3) so that deeply namespaced projects will also be repacked.

#### 0.1.3

 - Added `push` for old school deployment
 - Fix bug where projects are installed multiple times
 
## Motivation

`lein-repack` was written to solve a problem I had with utilities and general purpose libraries. In my experience, clojure libraries are much better when they are distributed in small packages. However, functionality is much easier to develop when the project is big: when we try to build our general purpose libaries in small packages, they become a nightmare to test and deploy.

`lein-repack` redistributes your code base into managable, deployable chunks and analyses your source files to automatically resolve internal and external dependencies. It this way, a big clojure project can now be broken up into sub-packages for deployment and redistribution.

The plugin will:

 - Create sub-projects for all sub-namespaces in a project
 - Will look through source files and figure out project dependencies
 - Will repackage a project into many smaller artifacts for easier deployment and distribution

## Installation

Add `lein-repack` to your project.clj file that you wish to repack:

```clojure
  (defproject lama ....
  
    :profiles {:dev {:plugins [...
                               [lein-repack "0.2.3"] ;; Current latest version
                               ...]}}
                               
    ;; defaults, can be left out
    :repack [{:type :clojure
              :path "src"
              :levels 1}])
```

## Walkthrough

For example, if there were a project called `lama` at version `0.1.0` and the files were organised like this:
 
    - src
       - lama
           - weapons
              - gun.clj
           - food
              - prepare.clj
           - food.clj
           - core.clj

Running `lein repack install` will split the project into four jars and install them in maven.

    lama-0.1.0.jar
    lama-core-0.1.0.jar
    lama-food-0.1.0.jar
    lama-weapons-0.1.0.jar

Running `lein repack deploy` will deploy all four artifacts to clojars. 

Once the artifacts are installed/deployed, they are now ready to be used. For example, if only the functionality for `lama.weapons` were required for another project, it can be imported individually in the project by adding `[lama.weapons "0.1.0"]` to project dependencies. The entire project can be imported my adding `[lama "0.1.0"]` to project dependencies.


## Usage

Once installed in your project.clj (or globally via profile.clj)

    $ lein repack

    Sub-tasks for repackage are available:
    help               Display this message
    manifest           Generate the manifest for repacking jars
    split              Splits the main project into several interim projects
    clean              Removes the interim folder
    install            Install all the interim projects to local repo
    deploy             Deploys all the repackaged jars
    push               Deployment the old-school way

### manifest

An [example](https://github.com/zcaudate/lein-repack/tree/master/example/repack.advance/project.clj) project showing advanced features of `lein-repack` has been added, demonstrating how different types of files (`java`, `clj`, `cljs` as well as resource files) can be developed together and then packaged seperately depending on project specification:

    $ cd example/repack.advanced
    $ lein repack manifest

The output the project manifest for the root project as well as all its branches can be seen below. Note the different types of files in the `web` branch:

```clojure
{:root
 {:files [],
  :dependencies
  [[org.clojure/clojure "1.6.0"]
   [im.chit/korra "0.1.2"]
   [im.chit/repack.advance.resources "0.1.0-SNAPSHOT"]
   [im.chit/repack.advance.jvm "0.1.0-SNAPSHOT"]
   [im.chit/repack.advance.common "0.1.0-SNAPSHOT"]
   [im.chit/repack.advance.core "0.1.0-SNAPSHOT"]
   [im.chit/repack.advance.util.array "0.1.0-SNAPSHOT"]
   [im.chit/repack.advance.util.data "0.1.0-SNAPSHOT"]
   [im.chit/repack.advance.web "0.1.0-SNAPSHOT"]],
  :version "0.1.0-SNAPSHOT",
  :group "im.chit",
  :name "repack.advance"},
 :branches
 {"web"
  {:coordinate [im.chit/repack.advance.web "0.1.0-SNAPSHOT"],
   :files
   ["java/im/chit/repack/web/Client.java"
    "resources/web/a.html"
    "src/cljs/repack/web/client.cljs"
    "src/clj/repack/web/client.clj"
    "src/cljs/repack/web.cljs"
    "resources/web/b.html"
    "src/clj/repack/web.clj"],
   :dependencies
   [[org.clojure/clojure "1.6.0"]
    [im.chit/repack.advance.core "0.1.0-SNAPSHOT"]
    [im.chit/repack.advance.util.array "0.1.0-SNAPSHOT"]
    [im.chit/repack.advance.common "0.1.0-SNAPSHOT"]],
   :version "0.1.0-SNAPSHOT",
   :name "im.chit/repack.advance.web",
   :group "im.chit"},
  "util.data"
  {:coordinate [im.chit/repack.advance.util.data "0.1.0-SNAPSHOT"],
   :files ["src/clj/repack/util/data.clj"],
   :dependencies
   [[org.clojure/clojure "1.6.0"]
    [im.chit/repack.advance.util.array "0.1.0-SNAPSHOT"]],
   :version "0.1.0-SNAPSHOT",
   :name "im.chit/repack.advance.util.data",
   :group "im.chit"},
  "util.array"
  {:coordinate [im.chit/repack.advance.util.array "0.1.0-SNAPSHOT"],
   :files
   ["src/clj/repack/util/array.clj"
    "src/clj/repack/util/array/sort.clj"],
   :dependencies [[org.clojure/clojure "1.6.0"]],
   :version "0.1.0-SNAPSHOT",
   :name "im.chit/repack.advance.util.array",
   :group "im.chit"},
  "core"
  {:coordinate [im.chit/repack.advance.core "0.1.0-SNAPSHOT"],
   :files ["src/clj/repack/core.clj"],
   :dependencies
   [[org.clojure/clojure "1.6.0"]
    [im.chit/repack.advance.resources "0.1.0-SNAPSHOT"]],
   :version "0.1.0-SNAPSHOT",
   :name "im.chit/repack.advance.core",
   :group "im.chit"},
  "common"
  {:coordinate [im.chit/repack.advance.common "0.1.0-SNAPSHOT"],
   :files
   ["resources/common/a.txt"
    "java/im/chit/repack/common/Hello.java"
    "resources/common/b.txt"
    "src/clj/repack/common.clj"],
   :dependencies [[org.clojure/clojure "1.6.0"]],
   :version "0.1.0-SNAPSHOT",
   :name "im.chit/repack.advance.common",
   :group "im.chit"},
  "jvm"
  {:coordinate [im.chit/repack.advance.jvm "0.1.0-SNAPSHOT"],
   :files ["java/im/chit/repack/native/Utils.java"],
   :dependencies [[org.clojure/clojure "1.6.0"]],
   :version "0.1.0-SNAPSHOT",
   :name "im.chit/repack.advance.jvm",
   :group "im.chit"},
  "resources"
  {:coordinate [im.chit/repack.advance.resources "0.1.0-SNAPSHOT"],
   :files ["resources/stuff/y.edn" "resources/stuff/x.edn"],
   :dependencies [[org.clojure/clojure "1.6.0"]],
   :version "0.1.0-SNAPSHOT",
   :name "im.chit/repack.advance.resources",
   :group "im.chit"}}}
```

### split

Running `lein repack split` will generate scaffolding of the root and branch projects in an interim directory, typically located in `target/interim` of your project directory.

### clean, install, deploy

They work the same way as the built in leiningen commands but also generates subprojects

## Contributors

- [Chris Zheng](https://github.com/zcaudate)
- [Kevin Downey](https://github.com/hiredman)
- [Jeroen van Dijk](https://github.com/jeroenvandijk)

## License

Copyright © 2014 Chris Zheng

Distributed under the MIT License
