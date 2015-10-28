(ns madek.api.resources.licenses.index
  (:require
    [cider-ci.utils.rdbms :as rdbms :refer [get-ds]]
    [clj-logging-config.log4j :as logging-config]
    [clojure.java.jdbc :as jdbc]
    [clojure.tools.logging :as logging]
    [honeysql.sql :refer :all]
    [logbug.debug :as debug]
    ))

(defn get-index [meta-datum]
  (let [query (-> (sql-select :licenses.*)
                  (sql-from :licenses)
                  (sql-merge-join
                    :meta_data_licenses
                    [:= :meta_data_licenses.license_id :licenses.id])
                  (sql-merge-where
                    [:= :meta_data_licenses.meta_datum_id (:id meta-datum)])
                  (sql-format))]
    (jdbc/query (rdbms/get-ds) query)))


;### Debug ####################################################################
;(logging-config/set-logger! :level :debug)
;(logging-config/set-logger! :level :info)
;(debug/debug-ns *ns*)


