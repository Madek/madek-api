(ns madek.api.resources.keywords.index
  (:require
   [clojure.java.jdbc :as jdbc]
   [clojure.tools.logging :as logging]
   [logbug.debug :as debug]
   [madek.api.resources.meta-keys.meta-key :as meta-key]
   [madek.api.utils.rdbms :as rdbms :refer [get-ds]]
   [madek.api.utils.sql :as sql]))

(def base-query
  (-> (sql/select :keywords.*)
      (sql/from :keywords)
      (sql/merge-join
       :meta_data_keywords
       [:= :meta_data_keywords.keyword_id :keywords.id])
      (sql/merge-select [:meta_data_keywords.position :position])
      (sql/order-by [:keywords.position :asc]
                    [:keywords.term :asc]
                    [:keywords.id :asc])))

(defn get-index [meta-datum]
  (let [meta-mey (first (jdbc/query (rdbms/get-ds)
                                    (meta-key/build-meta-key-query (:meta_key_id meta-datum))))
        query-base (-> base-query
                       (sql/merge-where
                        [:= :meta_data_keywords.meta_datum_id (:id meta-datum)]))
        query (sql/format (cond-> query-base
                            (:keywords_alphabetical_order meta-mey)
                            (sql/order-by [:keywords.term :asc])))]
    (jdbc/query (rdbms/get-ds) query)))

;### Debug ####################################################################
;(debug/debug-ns *ns*)
