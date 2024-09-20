(ns madek.api.resources.keywords.keyword
  (:require
   [clojure.java.jdbc :as jdbc]
   [clojure.tools.logging :as logging]
   [logbug.debug :as debug]
   [madek.api.utils.rdbms :as rdbms :refer [get-ds]]
   [madek.api.utils.sql :as sql]))

(defn find-keyword-sql
  [id]
  (->
   (sql/select :*)
   (sql/from :keywords)
   (sql/merge-where [:= :keywords.id id])
   sql/format))

(defn get-keyword
  [request]
  (let [id
        (->
         request
         :params
         :id)
        keyword (first (jdbc/query (rdbms/get-ds) (find-keyword-sql id)))]
    {:body
     (->
      keyword
      (select-keys
       [:id :meta_key_id :term :description :external_uris :rdf_class
        :created_at])
      (assoc ; support old (singular) version of field
       :external_uri (first (keyword :external_uris))))}))

;### Debug ####################################################################
;(debug/debug-ns *ns*)
