(ns madek.api.resources.people.person
  (:require
    [madek.api.utils.rdbms :as rdbms :refer [get-ds]]
    [madek.api.utils.sql :as sql]

    [clj-logging-config.log4j :as logging-config]
    [clojure.java.jdbc :as jdbc]
    [clojure.tools.logging :as logging]

    [logbug.debug :as debug]
    ))

(defn get-person [request]
  (let [id (-> request :params :id)
        query (-> (sql/select :*)
                  (sql/from :people)
                  (sql/merge-where
                    [:= :people.id id])
                  (sql/format))]
    {:body (select-keys (first (jdbc/query (rdbms/get-ds) query))
                        [:id
                         :first_name
                         :last_name
                         :pseudonym
                         :subtype
                         :date_of_birth
                         :date_of_death])}))

;### Debug ####################################################################
;(logging-config/set-logger! :level :debug)
;(logging-config/set-logger! :level :info)
;(debug/debug-ns *ns*)

