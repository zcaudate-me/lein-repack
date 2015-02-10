# lein-repack

Repack your project for deployment and distribution

### Whats New

#### 0.2.9

- Fix for non-removal of :source-map key

#### 0.2.8

- Using im.chit/vinyasa "0.3.2" instead of korra
- Fix for hypenated namespaces

#### 0.2.7

- Fix for project with no group name
- Fix for sane defaults when repack option has not been specified
- Fix for automatically figuring out :scm repo
- Better documentation for configuration options

#### 0.2.4

- Fixed bug where pprint did not import

#### 0.2.3

 - Overhauled analyser to make it more extensible (.cljx anybody?)
 - Support for java, clojurescript and resource files
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
                               [lein-repack "0.2.8"] ;; Current latest version
                               ...]}}
                               
    ;; specify source folders for repack, defaults can be omitted
    :repack [{:path "src"
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


### configuration
It's quite a difficult task to attempt to split a big project into smaller pieces. There are lots of things to consider, especially with mixed clj/cljs projects - most of all, where are all the different files stored and where are they going to go? Also, there is a problem with dependencies. For example, a file in your source directory may refer to an image file in your resource directory. When the project is split, you'd typically want just the image file to be relocated and not the entire resource directory.

`lein-repack` tries to be as accomodating as possible to these endless possibilities. Of course, you have to be smart about doing so but the tool does offer enough options to split up more intricate projects into bite-sized, more easily distributable pieces.

To be repacked, a project requires either a map or a vector of maps defining the code to be distributed under the `:repack` key in it's `project.clj`. We can look at options for [repack.advance](https://github.com/zcaudate/lein-repack/tree/master/example/repack.advance/project.clj) and how one project will get sliced up into multiple projects. The entire `:repack` options are shown below, with labels (#1 to #4) showing where relevent files are kept. In this case, we have a mixed source project with `.clj`, `.cljs`, `.java` and resources all split into their various directories. The final result is shown in the diagram below:

![Repack Results](https://github.com/zcaudate/lein-repack/raw/master/diagram/repack-diagram.png)

The configuration options that achieved such a result are shown below:

```clojure
:repack [{:path "src/clj"         ;; # 1. Clojure Files
          :levels 2
          :standalone #{"web"}}
         {:path "src/cljs"        ;; # 2. Clojurescript Files
          :levels 2
          :standalone #{"web"}}
         {:subpackage "jvm"       ;; # 3. Java Files
          :path "java/im/chit/repack"
          :distribute {"common" #{"common"}
                       "web"    #{"web"}}}
         {:subpackage "resources" ;; # 4. Resource Files
          :path "resources"
          :distribute {"common" #{"common"}
                       "web"    #{"web"}}
          :dependents #{"core"}}]
```

We look at the first entry (#1) and see that it has keys `:path`, `:levels` and `:standalone`. 

  - `:path` points to `src/clj`, meaning that repack will look inside `src/clj` to find all the relevant files that will need to be distributed. 
	- `:levels` has the value 2, meaning that it will look under 2 levels of folders. Usually, `1` is enough but if the project is large enough, then it is warranted.
	- `:standalone` indicates to repack that the `web` folder should not be split but remain as a single module.
	- given the directory structure under `src/clj` along with the options provided the following modules will be generated:
	  - common
		- core
		- util.data
		- util.array (note that although this has another directory, because `:levels` has been limited to 2 it will not be split any further)
		- web (note that although this is a directory, because we have given the `:standalone` option, it remains a single module)

The second entry (#2) is the same as the first, except that the `:path` key points to `src/cljs`.
  
  - only the `web` module will be generated. However, because this module already exists (due to its creation in #1), all the files from `src/cljs` will just be added to the `web` module.

The third entry (#3) is a little different to the first two because we are defining a subpackage as well as rules for how subfolders have to be distributed across modules. In this case, we have a folder containing `*.java` files. 

  - individual folders within the main `java/im/chit/repack` folder may need to be allocated to seperate modules. This is done using the `:distribute` key which means that
	  - the contents of the `java/im/chit/repack/common` folder will go into the `common` module
	  - the contents of the `java/im/chit/repack/web` folder will go into the `web` module
		- the `native` folder will go into the `jvm` module because it was not listed under the :distribute keyword

The forth entry (#4) is the resource directory and it is very similar to the java source code directory. However, some submodules may depend on resources and so this must be explicitly stated.

   - the `:dependents` key lists all the submodules that depend on resources. They will have a dependency to the resources submodule once it has been repacked.

### manifest


So having attempted to explain what was happening with the configuration options, it's probably much easier to just run the thing. The manifest will show what files in the project will go where depending on how the options play out. The advanced features of `lein-repack` will be demonstrated to show how different types of files (`java`, `clj`, `cljs` as well as resource files) can be developed together and then packaged seperately depending on project specification:

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

Copyright © 2015 Chris Zheng

Distributed under the MIT License
