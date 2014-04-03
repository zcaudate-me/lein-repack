# lein-repack

Repack your project for deployment and distribution

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
                               [lein-repack "0.1.2"] ;; Current latest version
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
   [im.chit/hara.checkers "1.1.0-SNAPSHOT"]
   [im.chit/hara.collection "1.1.0-SNAPSHOT"]
   [im.chit/hara.common "1.1.0-SNAPSHOT"]
   [im.chit/hara.import "1.1.0-SNAPSHOT"]
   [im.chit/hara.state "1.1.0-SNAPSHOT"]],
  :version "1.1.0-SNAPSHOT",
  :name "hara",
  :group "im.chit"},
 :branches
 {"checkers"
  {:coordinate [im.chit/hara.checkers "1.1.0-SNAPSHOT"],
   :files ["hara/checkers.clj"],
   :dependencies
   [[org.clojure/clojure "1.5.1"]
    [im.chit/hara.common "1.1.0-SNAPSHOT"]],
   :version "1.1.0-SNAPSHOT",
   :name "hara.checkers",
   :group "im.chit"},
  "collection"
  {:coordinate [im.chit/hara.collection "1.1.0-SNAPSHOT"],
   :files
   ["hara/collection/data_map.clj" "hara/collection/hash_map.clj"],
   :dependencies
   [[org.clojure/clojure "1.5.1"]
    [im.chit/hara.common "1.1.0-SNAPSHOT"]],
   :version "1.1.0-SNAPSHOT",
   :name "hara.collection",
   :group "im.chit"},
  "common"
  {:coordinate [im.chit/hara.common "1.1.0-SNAPSHOT"],
   :files
   ["hara/common/collection.clj"
    "hara/common/constructor.clj"
    "hara/common/control.clj"
    "hara/common/debug.clj"
    "hara/common/error.clj"
    "hara/common/fn.clj"
    "hara/common/interop.clj"
    "hara/common/keyword.clj"
    "hara/common/lettering.clj"
    "hara/common/string.clj"
    "hara/common/thread.clj"
    "hara/common/types.clj"
    "hara/common.clj"],
   :dependencies
   [[org.clojure/clojure "1.5.1"]
    [im.chit/hara.import "1.1.0-SNAPSHOT"]],
   :version "1.1.0-SNAPSHOT",
   :name "hara.common",
   :group "im.chit"},
  "import"
  {:coordinate [im.chit/hara.import "1.1.0-SNAPSHOT"],
   :files ["hara/import.clj"],
   :dependencies [[org.clojure/clojure "1.5.1"]],
   :version "1.1.0-SNAPSHOT",
   :name "hara.import",
   :group "im.chit"},
  "state"
  {:coordinate [im.chit/hara.state "1.1.0-SNAPSHOT"],
   :files ["hara/state.clj"],
   :dependencies
   [[org.clojure/clojure "1.5.1"]
    [im.chit/hara.common "1.1.0-SNAPSHOT"]],
   :version "1.1.0-SNAPSHOT",
   :name "hara.state",
   :group "im.chit"}}}
```

### split

Running `lein repack split` will generate scaffolding of the root and branch projects in an interim directory, typically located in `target/interim` of your project directory.


## Contributors

[Kevin Downey](https://github.com/hiredman)

## License

Copyright © 2014 Chris Zheng

Distributed under the MIT License
