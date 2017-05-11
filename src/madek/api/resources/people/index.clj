(ns madek.api.resources.people.index
  (:require
    [madek.api.utils.rdbms :as rdbms :refer [get-ds]]
    [clj-logging-config.log4j :as logging-config]
    [clojure.java.jdbc :as jdbc]
    [clojure.tools.logging :as logging]
    [honeysql.sql :refer :all]
    [logbug.debug :as debug]
    ))

(defn get-index [meta-datum]
  (let [query (-> (sql-select :people.*)
                  (sql-from :people)
                  (sql-merge-join
                    :meta_data_people
                    [:= :meta_data_people.person_id :people.id])
                  (sql-merge-where
                    [:= :meta_data_people.meta_datum_id (:id meta-datum)])
                  (sql-format))]
    (jdbc/query (rdbms/get-ds) query)))


;### Debug ####################################################################
;(logging-config/set-logger! :level :debug)
;(logging-config/set-logger! :level :info)
;(debug/debug-ns *ns*)


