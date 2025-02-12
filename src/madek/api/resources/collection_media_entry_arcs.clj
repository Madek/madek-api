(ns madek.api.resources.collection-media-entry-arcs
  (:require
   [clojure.java.jdbc :as jdbc]
   [clojure.tools.logging :as logging]
   [compojure.core :as cpj]
   [logbug.catcher :as catcher]
   [logbug.debug :as debug :refer [I> I>>]]
   [madek.api.constants :refer [presence]]
   [madek.api.pagination :as pagination]
   [madek.api.utils.rdbms :as rdbms]
   [madek.api.utils.sql :as sql]))

(defn arc-query [request]
  (-> (sql/select :*)
      (sql/from :collection_media_entry_arcs)
      (sql/merge-where [:= :id (-> request :params :id)])
      sql/format))

(defn arc [request]
  (when-let [arc (->> (arc-query request)
                      (jdbc/query (rdbms/get-ds))
                      first)]
    {:body arc}))

(defn arcs-query [query-params]
  (let [collection-id (-> query-params :collection_id presence)
        media-entry-id (-> query-params :media_entry_id presence)]
    (-> (sql/select :*)
        (sql/from :collection_media_entry_arcs)
        (#(if collection-id
            (sql/merge-where % [:= :collection_id collection-id]) %))
        (#(if media-entry-id
            (sql/merge-where % [:= :media_entry_id media-entry-id]) %))
        (pagination/add-offset-for-honeysql query-params)
        sql/format)))

(defn arcs [request]
  {:body {:collection-media-entry-arcs
          (jdbc/query (rdbms/get-ds)
                      (arcs-query (:query-params request)))}})

(def routes
  (cpj/routes
   (cpj/GET "/collection-media-entry-arcs/:id" [] #'arc)
   (cpj/GET "/collection-media-entry-arcs/" [] #'arcs)))

;### Debug ####################################################################
;(debug/debug-ns *ns*)
