(defproject madek-api "1.0.0-PRE.0"
  :description "The Madek API providing JSON and JSON-ROA."
  :url "https://github.com/Madek/madek-api"
  :license {:name "GNU GENERAL PUBLIC LICENSE, Version 3"
            :url "https://gnu.org/licenses/gpl-3.0.txt"}
  :dependencies [
                 [drtom/clj-uuid  "0.1.7"]
                 [cider-ci/open-session "1.3.0"] ; upgrade will break
                 [json-roa/clj-utils "1.0.0"]
                 [logbug "4.2.2"]
                 [honeysql "0.9.6"]
                 [org.clojure/core.match "0.3.0"]

                 ;[ring/ring-jetty-adapter "1.6.0"]
                 [aleph "0.4.6"]
                 [camel-snake-kebab "0.4.0"]
                 [cheshire "5.9.0"]
                 [clj-http "3.10.0"]
                 [clj-logging-config "1.9.12"]
                 [clj-yaml "0.4.0"]
                 [clojure-humanize "0.2.2"]
                 [com.github.mfornos/humanize-slim "1.2.2"]
                 [com.mchange/c3p0 "0.9.5.4"]
                 [compojure "1.6.1"]
                 [environ "1.1.0"]
                 [inflections "0.13.2"]
                 [log4j/log4j "1.2.17" :exclusions [javax.mail/mail javax.jms/jms com.sun.jdmk/jmxtools com.sun.jmx/jmxri]]
                 [me.raynes/fs "1.4.6"]
                 [org.apache.commons/commons-lang3 "3.9"]
                 [org.clojars.hozumi/clj-commons-exec "1.2.0"]
                 [org.clojure/clojure "1.8.0"]
                 [org.clojure/java.jdbc "0.7.10"]
                 [org.clojure/tools.logging "0.5.0"]
                 [org.clojure/tools.nrepl "0.2.13"]
                 [org.slf4j/slf4j-log4j12 "1.7.28"]
                 [org.yaml/snakeyaml "1.25"]
                 [pg-types "2.4.0-PRE.1"]
                 [ring-basic-authentication "1.0.5"]
                 [ring-cors "0.1.13"]
                 [ring/ring-core "1.7.1"]
                 [ring/ring-json "0.5.0"]
                 [uritemplate-clj "1.2.3"]


                 ]
  :aot :all
  :uberjar-name "api.jar"
  :main madek.api.main
  :java-source-paths ["java"]
  ;:source-paths ["src" "tmp/logbug/src"]

  ; jdk 9|10 needs ["--add-modules" "java.xml.bind"]
  :jvm-opts #=(eval (if (re-matches #"^(9|10)\..*" (System/getProperty "java.version"))
                      ["--add-modules" "java.xml.bind"]
                      []))


  ; :javac-options ["-target" "1.8" "-source" "1.8" "-Xlint:-options"]

  )
