(defproject madek-api "1.0.0-ALPHA.0"
  :description "The Madek API providing JSON and JSON-ROA."
  :url "https://github.com/Madek/madek-api"
  :license {:name "GNU GENERAL PUBLIC LICENSE, Version 3"
            :url "https://gnu.org/licenses/gpl-3.0.txt"}
  :dependencies [
                 [camel-snake-kebab "0.3.2"]
                 [cheshire "5.5.0"]
                 [cider-ci/clj-utils "3.2.0"]
                 [cider-ci/open-session "1.1.2"]
                 [clj-http "2.0.0"]
                 [clj-logging-config "1.9.12"]
                 [com.mchange/c3p0 "0.9.5"]
                 [compojure "1.4.0"]
                 [drtom/honeysql "1.3.0-beta.2"]
                 [drtom/logbug "1.3.0"]
                 [environ "1.0.1"]
                 [inflections "0.9.14"]
                 [json-roa/clj-utils "1.0.0"]
                 [log4j/log4j "1.2.17" :exclusions [javax.mail/mail javax.jms/jms com.sun.jdmk/jmxtools com.sun.jmx/jmxri]]
                 [org.clojars.hozumi/clj-commons-exec "1.2.0"]
                 [org.clojure/clojure "1.6.0"]
                 [org.clojure/java.jdbc "0.4.1"]
                 [org.clojure/tools.logging "0.3.1"]
                 [org.postgresql/postgresql "9.4-1201-jdbc41"]
                 [org.slf4j/slf4j-log4j12 "1.7.12"]
                 [pg-types "1.0.0"]
                 [ring-basic-authentication "1.0.5"]
                 [ring/ring-jetty-adapter "1.4.0"]
                 [ring/ring-json "0.4.0"]
                 ]
  :aot [madek.api.main]
  :main madek.api.main
  ;:source-paths ["src" "tmp/logbug/src"]
  )
