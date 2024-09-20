(ns madek.api.resources.vocabularies.index
  (:require
   [clojure.java.jdbc :as jdbc]
   [clojure.tools.logging :as logging]
   [logbug.catcher :as catcher]
   [logbug.debug :as debug]
   [madek.api.resources.vocabularies.permissions :as permissions]
   [madek.api.utils.rdbms :as rdbms :refer [get-ds]]
   [madek.api.utils.sql :as sql]))

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
  (-> (sql/select :id)
      (sql/from :vocabularies)
      (sql/merge-where (where-clause user-id))
      sql/format))

(defn- query-index-resources [request]
  (let [user-id (-> request :authenticated-entity :id)]
    (jdbc/query (rdbms/get-ds) (base-query user-id))))

(defn get-index [request]
  (catcher/with-logging {}
    {:body
     {:vocabularies
      (query-index-resources request)}}))

;### Debug ####################################################################
;(debug/debug-ns *ns*)
