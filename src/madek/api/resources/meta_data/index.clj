(ns madek.api.resources.meta-data.index
  (:require

    [madek.api.authorization :as authorization]
    [madek.api.pagination :as pagination]
    [madek.api.resources.shared :as shared]


    [cider-ci.utils.rdbms :as rdbms :refer [get-ds]]
    [clojure.java.jdbc :as jdbc]
    [compojure.core :as cpj]
    [honeysql.sql :refer :all]

    [clj-logging-config.log4j :as logging-config]
    [clojure.tools.logging :as logging]
    [logbug.catcher :as catcher]
    [logbug.debug :as debug]

    )

  (:import
    [madek.api WebstackException]
    ))

(defn meta-data-query-for-media-entry [media-entry-id]
  (-> (sql-select :id :type, :meta_key_id)
      (sql-from :meta_data)
      (sql-merge-where [:= :meta_data.media-entry-id media-entry-id])))

(defn filter-meta-data-by-meta-key-ids [query request]
  (if-let [meta-keys (-> request :query-params :meta_keys)]
    (do
      (when-not (seq? meta-keys)
        String (throw (WebstackException. (str "The value of the meta-keys parameter"
                                               " must be a json encoded list of strings." )
                                          {:status 422})))
      (sql-merge-where query [:in :meta_key_id meta-keys]))
    query))

(defn build-query [request base-query]
  (-> base-query
      (filter-meta-data-by-meta-key-ids request)
      sql-format))

(defn get-meta-data [request media-resource]
  (->> (case (:type media-resource)
         "MediaEntry" (meta-data-query-for-media-entry
                        (:id media-resource)))
       (build-query request)
       (jdbc/query (get-ds))))

(defn get-index [request]
  (if-let [media-resource (:media-resource request)]
    (let [meta-data (get-meta-data request media-resource)]
      {:body
       (conj
         {:meta-data meta-data}
         (case (:type media-resource)
           "MediaEntry" {:media_entry_id (:id media-resource)}))})))

;### Debug ####################################################################
;(logging-config/set-logger! :level :debug)
;(logging-config/set-logger! :level :info)
;(debug/debug-ns *ns*)
