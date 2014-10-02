# lein-repack

Repack your project for deployment and distribution

### Whats New

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
                               [lein-repack "0.1.4"] ;; Current latest version
                               ...]}}
                               
    ;; defaults, can be left out
    :repack {:root lama    ;; by default, it assumes that the root namespace is the same as the name of the project
             :exclude []   ;; by default, it will package all files. Add modules to only in the root project only
             :levels 1     ;; by default, it will package one level of directory nesting. This should be enough for most projects}
)
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

## Limitations

 - Currently only work with clojure code bases 
 - Uses :require and :use forms for working out dependencies
 - Not working with clojurescript (Yet)

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

### clean, install, deploy

They work the same way as the built in leiningen commands but also generates subprojects

### manifest

I developed `lein-repack` by using an existing project to test its behavior. `hara` is included in the [examples/hara](https://github.com/zcaudate/lein-repack/tree/master/example/hara) directory. Running the following commands

    $ cd example/hara
    $ lein repack manifest

Will output the project manifest for the root project as well as all its branches:

```clojure
{:root
 {:files [],
  :dependencies
  [[org.clojure/clojure "1.5.1"]
   [im.chit/hara.common.collection "1.1.0-SNAPSHOT"]
   [im.chit/hara.common.error "1.1.0-SNAPSHOT"]
   [im.chit/hara.import "1.1.0-SNAPSHOT"]
   [im.chit/hara.common.keyword "1.1.0-SNAPSHOT"]
   [im.chit/hara.common "1.1.0-SNAPSHOT"]
   [im.chit/hara.common.fn "1.1.0-SNAPSHOT"]
   [im.chit/hara.common.thread "1.1.0-SNAPSHOT"]
   [im.chit/hara.common.debug "1.1.0-SNAPSHOT"]
   [im.chit/hara.state "1.1.0-SNAPSHOT"]
   [im.chit/hara.common.string "1.1.0-SNAPSHOT"]
   [im.chit/hara.common.types "1.1.0-SNAPSHOT"]
   [im.chit/hara.common.constructor "1.1.0-SNAPSHOT"]
   [im.chit/hara.collection.data_map "1.1.0-SNAPSHOT"]
   [im.chit/hara.common.lettering "1.1.0-SNAPSHOT"]
   [im.chit/hara.common.control "1.1.0-SNAPSHOT"]
   [im.chit/hara.common.interop "1.1.0-SNAPSHOT"]
   [im.chit/hara.collection.hash_map "1.1.0-SNAPSHOT"]
   [im.chit/hara.checkers "1.1.0-SNAPSHOT"]],
  :version "1.1.0-SNAPSHOT",
  :name "hara",
  :group "im.chit"},
 :branches
 {"common.collection"
  {:coordinate [im.chit/hara.common.collection "1.1.0-SNAPSHOT"],
   :files ["a/b/c/hara/common/collection.clj"],
   :dependencies
   [[org.clojure/clojure "1.5.1"]
    [im.chit/hara.common.fn "1.1.0-SNAPSHOT"]
    [im.chit/hara.common.error "1.1.0-SNAPSHOT"]
    [im.chit/hara.common.types "1.1.0-SNAPSHOT"]],
   :version "1.1.0-SNAPSHOT",
   :name "hara.common.collection",
   :group "im.chit"},
  "common.error"
  {:coordinate [im.chit/hara.common.error "1.1.0-SNAPSHOT"],
   :files ["a/b/c/hara/common/error.clj"],
   :dependencies [[org.clojure/clojure "1.5.1"]],
   :version "1.1.0-SNAPSHOT",
   :name "hara.common.error",
   :group "im.chit"},
  "import"
  {:coordinate [im.chit/hara.import "1.1.0-SNAPSHOT"],
   :files ["a/b/c/hara/import.clj"],
   :dependencies [[org.clojure/clojure "1.5.1"]],
   :version "1.1.0-SNAPSHOT",
   :name "hara.import",
   :group "im.chit"},
  "common.keyword"
  {:coordinate [im.chit/hara.common.keyword "1.1.0-SNAPSHOT"],
   :files ["a/b/c/hara/common/keyword.clj"],
   :dependencies [[org.clojure/clojure "1.5.1"]],
   :version "1.1.0-SNAPSHOT",
   :name "hara.common.keyword",
   :group "im.chit"},
  "common"
  {:coordinate [im.chit/hara.common "1.1.0-SNAPSHOT"],
   :files ["a/b/c/hara/common.clj"],
   :dependencies
   [[org.clojure/clojure "1.5.1"]
    [im.chit/hara.common.fn "1.1.0-SNAPSHOT"]
    [im.chit/hara.common.control "1.1.0-SNAPSHOT"]
    [im.chit/hara.common.constructor "1.1.0-SNAPSHOT"]
    [im.chit/hara.common.interop "1.1.0-SNAPSHOT"]
    [im.chit/hara.import "1.1.0-SNAPSHOT"]
    [im.chit/hara.common.string "1.1.0-SNAPSHOT"]
    [im.chit/hara.common.thread "1.1.0-SNAPSHOT"]
    [im.chit/hara.common.keyword "1.1.0-SNAPSHOT"]
    [im.chit/hara.common.lettering "1.1.0-SNAPSHOT"]
    [im.chit/hara.common.error "1.1.0-SNAPSHOT"]
    [im.chit/hara.common.debug "1.1.0-SNAPSHOT"]
    [im.chit/hara.common.types "1.1.0-SNAPSHOT"]
    [im.chit/hara.common.collection "1.1.0-SNAPSHOT"]],
   :version "1.1.0-SNAPSHOT",
   :name "hara.common",
   :group "im.chit"},
  "common.fn"
  {:coordinate [im.chit/hara.common.fn "1.1.0-SNAPSHOT"],
   :files ["a/b/c/hara/common/fn.clj"],
   :dependencies
   [[org.clojure/clojure "1.5.1"]
    [im.chit/hara.common.error "1.1.0-SNAPSHOT"]
    [im.chit/hara.common.types "1.1.0-SNAPSHOT"]],
   :version "1.1.0-SNAPSHOT",
   :name "hara.common.fn",
   :group "im.chit"},
  "common.thread"
  {:coordinate [im.chit/hara.common.thread "1.1.0-SNAPSHOT"],
   :files ["a/b/c/hara/common/thread.clj"],
   :dependencies [[org.clojure/clojure "1.5.1"]],
   :version "1.1.0-SNAPSHOT",
   :name "hara.common.thread",
   :group "im.chit"},
  "common.debug"
  {:coordinate [im.chit/hara.common.debug "1.1.0-SNAPSHOT"],
   :files ["a/b/c/hara/common/debug.clj"],
   :dependencies [[org.clojure/clojure "1.5.1"]],
   :version "1.1.0-SNAPSHOT",
   :name "hara.common.debug",
   :group "im.chit"},
  "state"
  {:coordinate [im.chit/hara.state "1.1.0-SNAPSHOT"],
   :files ["a/b/c/hara/state.clj"],
   :dependencies
   [[org.clojure/clojure "1.5.1"]
    [im.chit/hara.common.fn "1.1.0-SNAPSHOT"]
    [im.chit/hara.common.types "1.1.0-SNAPSHOT"]],
   :version "1.1.0-SNAPSHOT",
   :name "hara.state",
   :group "im.chit"},
  "common.string"
  {:coordinate [im.chit/hara.common.string "1.1.0-SNAPSHOT"],
   :files ["a/b/c/hara/common/string.clj"],
   :dependencies [[org.clojure/clojure "1.5.1"]],
   :version "1.1.0-SNAPSHOT",
   :name "hara.common.string",
   :group "im.chit"},
  "common.types"
  {:coordinate [im.chit/hara.common.types "1.1.0-SNAPSHOT"],
   :files ["a/b/c/hara/common/types.clj"],
   :dependencies [[org.clojure/clojure "1.5.1"]],
   :version "1.1.0-SNAPSHOT",
   :name "hara.common.types",
   :group "im.chit"},
  "common.constructor"
  {:coordinate [im.chit/hara.common.constructor "1.1.0-SNAPSHOT"],
   :files ["a/b/c/hara/common/constructor.clj"],
   :dependencies
   [[org.clojure/clojure "1.5.1"]
    [im.chit/hara.common.error "1.1.0-SNAPSHOT"]
    [im.chit/hara.common.types "1.1.0-SNAPSHOT"]],
   :version "1.1.0-SNAPSHOT",
   :name "hara.common.constructor",
   :group "im.chit"},
  "collection.data_map"
  {:coordinate [im.chit/hara.collection.data_map "1.1.0-SNAPSHOT"],
   :files ["a/b/c/hara/collection/data_map.clj"],
   :dependencies
   [[org.clojure/clojure "1.5.1"]
    [im.chit/hara.common.fn "1.1.0-SNAPSHOT"]
    [im.chit/hara.common.error "1.1.0-SNAPSHOT"]
    [im.chit/hara.common.types "1.1.0-SNAPSHOT"]],
   :version "1.1.0-SNAPSHOT",
   :name "hara.collection.data_map",
   :group "im.chit"},
  "common.lettering"
  {:coordinate [im.chit/hara.common.lettering "1.1.0-SNAPSHOT"],
   :files ["a/b/c/hara/common/lettering.clj"],
   :dependencies [[org.clojure/clojure "1.5.1"]],
   :version "1.1.0-SNAPSHOT",
   :name "hara.common.lettering",
   :group "im.chit"},
  "common.control"
  {:coordinate [im.chit/hara.common.control "1.1.0-SNAPSHOT"],
   :files ["a/b/c/hara/common/control.clj"],
   :dependencies [[org.clojure/clojure "1.5.1"]],
   :version "1.1.0-SNAPSHOT",
   :name "hara.common.control",
   :group "im.chit"},
  "common.interop"
  {:coordinate [im.chit/hara.common.interop "1.1.0-SNAPSHOT"],
   :files ["a/b/c/hara/common/interop.clj"],
   :dependencies
   [[org.clojure/clojure "1.5.1"]
    [im.chit/hara.common.lettering "1.1.0-SNAPSHOT"]],
   :version "1.1.0-SNAPSHOT",
   :name "hara.common.interop",
   :group "im.chit"},
  "collection.hash_map"
  {:coordinate [im.chit/hara.collection.hash_map "1.1.0-SNAPSHOT"],
   :files ["a/b/c/hara/collection/hash_map.clj"],
   :dependencies
   [[org.clojure/clojure "1.5.1"]
    [im.chit/hara.common.keyword "1.1.0-SNAPSHOT"]
    [im.chit/hara.common.types "1.1.0-SNAPSHOT"]
    [im.chit/hara.common.collection "1.1.0-SNAPSHOT"]],
   :version "1.1.0-SNAPSHOT",
   :name "hara.collection.hash_map",
   :group "im.chit"},
  "checkers"
  {:coordinate [im.chit/hara.checkers "1.1.0-SNAPSHOT"],
   :files ["a/b/c/hara/checkers.clj"],
   :dependencies
   [[org.clojure/clojure "1.5.1"]
    [im.chit/hara.common.fn "1.1.0-SNAPSHOT"]],
   :version "1.1.0-SNAPSHOT",
   :name "hara.checkers",
   :group "im.chit"}}}
```

### split

Running `lein repack split` will generate scaffolding of the root and branch projects in an interim directory, typically located in `target/interim` of your project directory.

## Contributors

- [Chris Zheng](https://github.com/zcaudate)
- [Kevin Downey](https://github.com/hiredman)
- [Jeroen van Dijk](https://github.com/jeroenvandijk)

## License

Copyright © 2014 Chris Zheng

Distributed under the MIT License
