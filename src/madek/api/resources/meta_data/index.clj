(ns madek.api.resources.meta-data.index
  (:require
   [clojure.java.jdbc :as jdbc]
   [clojure.tools.logging :as logging]
   [compojure.core :as cpj]
   [logbug.catcher :as catcher]
   [logbug.debug :as debug]
   [madek.api.authorization :as authorization]
   [madek.api.constants :as constants]
   [madek.api.pagination :as pagination]
   [madek.api.resources.shared :as shared]
   [madek.api.resources.vocabularies.permissions :as permissions]
   [madek.api.utils.rdbms :as rdbms :refer [get-ds]]
   [madek.api.utils.sql :as sql]
   [taoensso.timbre :refer [debug info warn error spy]]))

(defn- where-clause
  [user-id]
  (let [vocabulary-ids (permissions/accessible-vocabulary-ids user-id)]
    (if (empty? vocabulary-ids)
      [:= :vocabularies.enabled_for_public_view true]
      [:or
       [:= :vocabularies.enabled_for_public_view true]
       [:in :vocabularies.id vocabulary-ids]])))

(defn- base-query
  [user-id]
  (-> (sql/select :meta_data.id :meta_data.type :meta_data.meta_key_id)
      (sql/from :meta_data)
      (sql/merge-where [:in :meta_data.type
                        constants/SUPPORTED_META_DATA_TYPES])
      (sql/merge-join :meta_keys [:= :meta_data.meta_key_id :meta_keys.id])
      (sql/merge-join :vocabularies [:= :meta_keys.vocabulary_id :vocabularies.id])
      (sql/merge-where (where-clause user-id))
      (sql/order-by [:vocabularies.position :asc]
                    [:meta_keys.position :asc]
                    [:meta_data.id :asc])))

(defn- meta-data-query-for-media-entry [media-entry-id user-id]
  (-> (base-query user-id)
      (sql/merge-where [:= :meta_data.media_entry_id media-entry-id])))

(defn- meta-data-query-for-collection [collection-id user-id]
  (-> (base-query user-id)
      (sql/merge-where [:= :meta_data.collection_id collection-id])))

(defn filter-meta-data-by-meta-key-ids [query request]
  (if-let [meta-keys (-> request :query-params :meta_keys)]
    (do
      (when-not (seq? meta-keys)
        String (throw (ex-info (str "The value of the meta-keys parameter"
                                    " must be a json encoded list of strings.")
                               {:status 422})))
      (sql/merge-where query [:in :meta_key_id meta-keys]))
    query))

(defn build-query [request base-query]
  (-> base-query
      (filter-meta-data-by-meta-key-ids request)
      sql/format))

(defn get-meta-data [request media-resource]
  (let [user-id (-> request :authenticated-entity :id)]
    (when-let [id (:id media-resource)]
      (->> (case (:type media-resource)
             "MediaEntry" (meta-data-query-for-media-entry id user-id)
             "Collection" (meta-data-query-for-collection id user-id))
           (build-query request)
           (jdbc/query (get-ds))))))

(defn get-index [request]
  (if-let [media-resource (:media-resource request)]
    (when-let [meta-data (get-meta-data request media-resource)]
      {:body
       (conj
        {:meta-data meta-data}
        (case (:type media-resource)
          "MediaEntry" {:media_entry_id (:id media-resource)}
          "Collection" {:collection_id (:id media-resource)}))})))

;### Debug ####################################################################
;(debug/debug-ns *ns*)
