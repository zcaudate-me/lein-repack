(ns leiningen.repack.test-maven
  (:require [leiningen.repack.maven :refer :all]
            [midje.sweet :refer :all]))

(fact "class-name->jar-resource-path"
  (class-name->jar-resource-path "java.lang.String")
  => "java/lang/String.class"

  (class-name->jar-resource-path "midje.sweet$fact")
  => "midje/sweet$fact.class")

(fact "namespace->jar-resource-path"
  (namespace->jar-resource-path "clojure.core")
  => "clojure/core.clj"

  (namespace->jar-resource-path "midje.sweet")
  => "midje/sweet.clj"

  (namespace->jar-resource-path "clj-time.core")
  => "clj_time/core.clj")

(fact "jar-by-path"
  (jar-by-path "clojure/core.clj")
  => [*lein-jar* "clojure/core.clj"]

  (jar-by-path "midje/sweet.clj")
  => [(str *local-repo* "/midje/midje/1.6.3/midje-1.6.3.jar") "midje/sweet.clj"])

(fact "jar-by-class"
  (jar-by-class String)
  => [(str (System/getProperty "java.home") "/lib/rt.jar") "java/lang/String.class"]

  (jar-by-class clojure.core$assoc)
  => [*lein-jar* "clojure/core$assoc.class"])

(fact "jar-by-namespace"
  (jar-by-namespace 'clojure.core)
  => [*lein-jar* "clojure/core.clj"])

(fact "maven-by-class"
  (jar-by-class org.sonatype.aether.repository.LocalRepository)
  nil)

(fact "maven-by-namespace"
  (maven-by-namespace 'clojure.core)
  => nil

  (maven-by-namespace )
  => '[midje/midje "1.6.3"])

(fact "maven-by-path"
  (maven-by-namespace 'clojure.core)
  => nil

  (maven-by-namespace 'midje.sweet)
  => '[midje/midje "1.6.3"])


(System/getenv)
{"TERM" "dumb", "LEIN_JVM_OPTS" "-XX:+TieredCompilation -XX:TieredStopAtLevel=1", "SHLVL" "1", "LEIN_HOME" "/Users/zhengc/.lein", "__CF_USER_TEXT_ENCODING" "0x6B6EE6F8:0:0", "MANPATH" "/usr/share/man:/usr/local/share/man", "DRIP_INIT" "-e\nnil", "JAVA_CMD" "java", "PWD" "/Users/zhengc/dev/libs/lein-repack", "JAVA_ARCH" "x86_64", "LOGNAME" "zhengc", "SSH_AUTH_SOCK" "/tmp/launch-jhs9VW/Listeners", "__CHECKFIX1436934" "1", "DRIP_INIT_CLASS" "clojure.main", "SHELL" "/bin/zsh", "TMPDIR" "/var/folders/dd/qfdy6sbn3mlgk20vcxc3j0ljnpxsqr/T/", "TRAMPOLINE_FILE" "/tmp/lein-trampoline-8m8ZcFeTvjLBc", "SECURITYSESSIONID" "186a4", "CLASSPATH" ":/Users/zhengc/.lein/self-installs/leiningen-2.3.4-standalone.jar", "PATH" "/usr/bin:/bin:/usr/sbin:/sbin:/usr/local/bin:/Users/zhengc/bin", "LEIN_VERSION" "2.3.4", "JVM_OPTS" "", "COMMAND_MODE" "unix2003", "DISPLAY" "chris-apdm.local", "USER" "zhengc", "com.apple.java.jvmTask" "CommandLine", "JAVA_MAIN_CLASS_462" "clojure.main", "HOME" "/Users/zhengc", "LEIN_JAVA_CMD" "/Users/zhengc/.leindrip/drip", "Apple_PubSub_Socket_Render" "/tmp/launch-gpZVWZ/Render"}

{"java.runtime.name" "Java(TM) SE Runtime Environment", "sun.boot.library.path" "/Library/Java/JavaVirtualMachines/jdk1.7.0_60.jdk/Contents/Home/jre/lib", "java.vm.version" "24.60-b03", "user.country.format" "AU", "gopherProxySet" "false", "leiningen.script" "/Users/zhengc/bin/lein", "java.vm.vendor" "Oracle Corporation", "java.vendor.url" "http://java.oracle.com/", "path.separator" ":", "java.vm.name" "Java HotSpot(TM) 64-Bit Server VM", "file.encoding.pkg" "sun.io", "user.country" "US", "sun.java.launcher" "SUN_STANDARD", "sun.os.patch.level" "unknown", "java.vm.specification.name" "Java Virtual Machine Specification", "user.dir" "/Users/zhengc/dev/libs/lein-repack", "java.runtime.version" "1.7.0_60-ea-b01", "java.awt.graphicsenv" "sun.awt.CGraphicsEnvironment", "java.endorsed.dirs" "/Library/Java/JavaVirtualMachines/jdk1.7.0_60.jdk/Contents/Home/jre/lib/endorsed", "os.arch" "x86_64", "clojure.debug" "false", "java.io.tmpdir" "/var/folders/dd/qfdy6sbn3mlgk20vcxc3j0ljnpxsqr/T/", "line.separator" "\n", "java.vm.specification.vendor" "Oracle Corporation", "os.name" "Mac OS X", "sun.jnu.encoding" "UTF-8", "java.library.path" "/Users/zhengc/Library/Java/Extensions:/Library/Java/Extensions:/Network/Library/Java/Extensions:/System/Library/Java/Extensions:/usr/lib/java:.", "java.specification.name" "Java Platform API Specification", "java.class.version" "51.0", "sun.management.compiler" "HotSpot 64-Bit Tiered Compilers", "os.version" "10.9.2", "clojure.compile.path" "/Users/zhengc/dev/libs/lein-repack/target/classes", "user.home" "/Users/zhengc", "user.timezone" "Australia/Melbourne", "aether.connector.userAgent" "Leiningen/2.3.4 (Java Java HotSpot(TM) 64-Bit Server VM; Mac OS X 10.9.2; x86_64)", "java.awt.printerjob" "sun.lwawt.macosx.CPrinterJob", "leiningen.original.pwd" "/Users/zhengc/dev/libs/lein-repack", "file.encoding" "UTF-8", "java.specification.version" "1.7", "maven.wagon.http.ssl.easy" "false", "java.class.path" ":/Users/zhengc/.lein/self-installs/leiningen-2.3.4-standalone.jar", "user.name" "zhengc", "java.vm.specification.version" "1.7", "sun.java.command" "clojure.main -m leiningen.core.main repl :headless", "java.home" "/Library/Java/JavaVirtualMachines/jdk1.7.0_60.jdk/Contents/Home/jre", "sun.arch.data.model" "64", "user.language" "en", "java.specification.vendor" "Oracle Corporation", "awt.toolkit" "sun.lwawt.macosx.LWCToolkit", "java.vm.info" "mixed mode", "java.version" "1.7.0_60-ea", "java.ext.dirs" "/Users/zhengc/Library/Java/Extensions:/Library/Java/JavaVirtualMachines/jdk1.7.0_60.jdk/Contents/Home/jre/lib/ext:/Library/Java/Extensions:/Network/Library/Java/Extensions:/System/Library/Java/Extensions:/usr/lib/java", "sun.boot.class.path" "/Library/Java/JavaVirtualMachines/jdk1.7.0_60.jdk/Contents/Home/jre/lib/resources.jar:/Library/Java/JavaVirtualMachines/jdk1.7.0_60.jdk/Contents/Home/jre/lib/rt.jar:/Library/Java/JavaVirtualMachines/jdk1.7.0_60.jdk/Contents/Home/jre/lib/sunrsasign.jar:/Library/Java/JavaVirtualMachines/jdk1.7.0_60.jdk/Contents/Home/jre/lib/jsse.jar:/Library/Java/JavaVirtualMachines/jdk1.7.0_60.jdk/Contents/Home/jre/lib/jce.jar:/Library/Java/JavaVirtualMachines/jdk1.7.0_60.jdk/Contents/Home/jre/lib/charsets.jar:/Library/Java/JavaVirtualMachines/jdk1.7.0_60.jdk/Contents/Home/jre/lib/jfr.jar:/Library/Java/JavaVirtualMachines/jdk1.7.0_60.jdk/Contents/Home/jre/lib/JObjC.jar:/Library/Java/JavaVirtualMachines/jdk1.7.0_60.jdk/Contents/Home/jre/classes:/Users/zhengc/.lein/self-installs/leiningen-2.3.4-standalone.jar", "java.vendor" "Oracle Corporation", "lein-repack.version" "0.1.0", "file.separator" "/", "java.vendor.url.bug" "http://bugreport.sun.com/bugreport/", "sun.io.unicode.encoding" "UnicodeBig", "sun.cpu.endian" "little", "sun.cpu.isalist" ""}
