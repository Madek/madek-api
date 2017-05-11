(defproject madek-api "1.0.0-PRE.0"
  :description "The Madek API providing JSON and JSON-ROA."
  :url "https://github.com/Madek/madek-api"
  :license {:name "GNU GENERAL PUBLIC LICENSE, Version 3"
            :url "https://gnu.org/licenses/gpl-3.0.txt"}
  :dependencies [
                 [drtom/clj-uuid  "0.1.7"]
                 [cider-ci/open-session "1.3.0"]
                 [json-roa/clj-utils "1.0.0"]
                 [logbug "4.2.2"]
                 [drtom/honeysql "2.0.0-ALPHA+1"]

                 [aleph "0.4.1"]
                 [camel-snake-kebab "0.3.2"]
                 [cheshire "5.5.0"]
                 [clj-http "2.1.0"]
                 [clj-logging-config "1.9.12"]
                 [clj-yaml "0.4.0"]
                 [clojure-humanize "0.2.0"]
                 [com.github.mfornos/humanize-slim "1.2.2"]
                 [com.mchange/c3p0 "0.9.5.2"]
                 [compojure "1.5.0"]
                 [environ "1.0.2"]
                 [environ "1.0.2"]
                 [inflections "0.12.1"]
                 [log4j/log4j "1.2.17" :exclusions [javax.mail/mail javax.jms/jms com.sun.jdmk/jmxtools com.sun.jmx/jmxri]]
                 [me.raynes/fs "1.4.6"]
                 [org.apache.commons/commons-lang3 "3.4"]
                 [org.clojars.hozumi/clj-commons-exec "1.2.0"]
                 [org.clojure/clojure "1.8.0"]
                 [org.clojure/java.jdbc "0.5.0"]
                 [org.clojure/tools.logging "0.3.1"]
                 [org.clojure/tools.nrepl "0.2.12"]
                 [org.slf4j/slf4j-log4j12 "1.7.19"]
                 [org.yaml/snakeyaml "1.17"]
                 [pg-types "2.2.0"]
                 [ring-basic-authentication "1.0.5"]
                 [ring/ring-jetty-adapter "1.4.0"]
                 [ring/ring-json "0.4.0"]
                 [uritemplate-clj "1.1.1"]

                 ]
  :aot :all
  :uberjar-name "api.jar"
  :main madek.api.main
  :java-source-paths ["java"]
  ;:source-paths ["src" "tmp/logbug/src"]
  )
