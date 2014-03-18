(ns leiningen.server)




(import org.apache.catalina.startup.Tomcat)

(require '[clojure.java.io :as io])

(def tomcat (Tomcat.))


(do
  (.setBaseDir tomcat "/usr/local/Cellar/tomcat/7.0.52/libexec/webapps")
  (.setHostname tomcat "localhost")
  (.setPort tomcat 9090)
  (.addWebapp tomcat "/cassui" "/usr/local/Cellar/tomcat/7.0.52/libexec/webapps/cassui")
  (.addWebapp tomcat "/vaadin" "/usr/local/Cellar/tomcat/7.0.52/libexec/webapps/cljvaadin")
  (.start tomcat))

;;(.start (.getServer tomcat))
;;(.getState (.getServer tomcat))

(System/getenv)
(-> (System/getProperties) keys sort)
;;(def dir )
(comment
  (.stop tomcat)
  (.destroy tomcat)
  (.* tomcat :name :field)("basedir" "connector" "engine" "host" "hostname" "port" "server" "service" "userPass" "userPrincipals" "userRoles")
  (.* (.getServer tomcat) :name :field)
  (.* (.getEngine tomcat) :name :field)
  (-> (.getEngine tomcat)
      (->> (.$ children))
      (get "localhost")
      (->> (.$ children)))

  ("addContext" "addRole" "addServlet" "addUser" "addWebapp" "basedir" "clone" "connector" "createDefaultRealm" "destroy" "enableNaming" "engine" "equals" "finalize" "getClass" "getConnector" "getDefaultWebXmlListener" "getEngine" "getHost" "getLoggerName" "getServer" "getService" "getWebappConfigFile" "getWebappConfigFileFromDirectory" "getWebappConfigFileFromJar" "hashCode" "host" "hostname" "init" "initBaseDir" "initWebappDefaults" "noDefaultWebXmlPath" "notify" "notifyAll" "port" "server" "service" "setBaseDir" "setConnector" "setHost" "setHostname" "setPort" "setSilent" "silence" "start" "stop" "toString" "userPass" "userPrincipals" "userRoles" "wait")
)
