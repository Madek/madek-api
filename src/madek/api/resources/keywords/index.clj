(ns madek.api.resources.keywords.index
  (:require
    [madek.api.utils.rdbms :as rdbms :refer [get-ds]]
    [clj-logging-config.log4j :as logging-config]
    [clojure.java.jdbc :as jdbc]
    [clojure.tools.logging :as logging]
    [honeysql.sql :refer :all]
    [logbug.debug :as debug]
    [madek.api.resources.meta-keys.meta-key :as meta-key]
    ))

(defn get-index [meta-datum]
  (let [meta-mey (first (jdbc/query (rdbms/get-ds)
                                    (meta-key/build-meta-key-query (:meta_key_id meta-datum))))
        query-base (-> (sql-select :keywords.*)
                       (sql-from :keywords)
                       (sql-merge-join
                         :meta_data_keywords
                         [:= :meta_data_keywords.keyword_id :keywords.id])
                       (sql-merge-where
                         [:= :meta_data_keywords.meta_datum_id (:id meta-datum)]))
        query (sql-format (cond-> query-base
                            (:keywords_alphabetical_order meta-mey)
                            (sql-order-by [:keywords.term :asc])))]
    (jdbc/query (rdbms/get-ds) query)))


;### Debug ####################################################################
;(logging-config/set-logger! :level :debug)
;(logging-config/set-logger! :level :info)
;(debug/debug-ns *ns*)


