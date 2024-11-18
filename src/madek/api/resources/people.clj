(ns madek.api.resources.people
  (:require
   [clj-uuid]
   [clojure.java.jdbc :as jdbc]
   [clojure.tools.logging :as logging]
   [compojure.core :as cpj]
   [logbug.debug :as debug]
   [madek.api.constants :refer [presence]]
   [madek.api.pagination :as pagination]
   [madek.api.pagination :as pagination]
   [madek.api.resources.media-entries.index :refer [get-index]]
   [madek.api.resources.media-entries.media-entry :refer [get-media-entry]]
   [madek.api.resources.shared :as shared]
   [madek.api.utils.auth :refer [wrap-authorize-admin!]]
   [madek.api.utils.rdbms :as rdbms]
   [madek.api.utils.sql :as sql]
   [ring.util.codec :refer [url-decode]]
   [taoensso.timbre :as timbre :refer [debug info]]))

(defn id-where-clause
  [id]
  (if
   (re-matches
    #"[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}"
    id)
    (sql/where [:or [:= :id id] [:= :institutional_id id]])
    (sql/where [:= :institutional_id id])))

(defn jdbc-id-where-clause
  [id]
  (->
   id
   id-where-clause
   sql/format
   (update-in [0] #(clojure.string/replace % "WHERE" ""))))

;### create person
;#############################################################

(defn create-person
  [request]
  (let [params
        (as-> (:body request) params
          (or params {})
          (assoc params :id (or (:id params) (clj-uuid/v4))))]
    {:body
     (dissoc
      (->>
       (jdbc/insert! (rdbms/get-ds) :people params)
       first)
      :previous_id
      :searchable),
     :status 201}))

;### get person
;################################################################

(defn find-person-sql
  [id]
  (->
   (id-where-clause id)
   (sql/select :*)
   (sql/from :people)
   sql/format))

(defn get-person
  [{{id-or-institutinal-person-id :id} :route-params :as request}]
  (debug "get-person" request)
  (if-let [person
           (->>
            id-or-institutinal-person-id
            find-person-sql
            (jdbc/query (rdbms/get-ds))
            first)]
    {:body
     (->
      person
      (assoc ; support old (singular) version of field
       :external_uri (first (person :external_uris)))
      (dissoc :previous_id :searchable))}
    {:status 404, :body "No such person found"}))

;### delete person
;##############################################################

(defn delete-person
  [id]
  (if
   (=
    1
    (first (jdbc/delete! (rdbms/get-ds) :people (jdbc-id-where-clause id))))
    {:status 204}
    {:status 404}))

;### patch person
;##############################################################

(defn patch-person
  [{body :body, {id :id} :params}]
  (if
   (=
    1
    (first
     (jdbc/update! (rdbms/get-ds) :people body (jdbc-id-where-clause id))))
    {:body
     (->>
      id
      find-person-sql
      (jdbc/query (rdbms/get-ds))
      first)}
    {:status 404}))

;### index ####################################################################

(defn build-index-query
  [{query-params :query-params}]
  (->
   (sql/select :id)
   (sql/from :people)
   (sql/order-by [:id :asc])
   (pagination/add-offset-for-honeysql query-params)
   sql/format))

(defn index
  [request]
  {:body {:people (jdbc/query (rdbms/get-ds) (build-index-query request))}})

;### routes ###################################################################

(def admin-protected-routes
  (->
   (cpj/routes
    (cpj/GET "/people/" [] index)
    (cpj/POST "/people/" [] create-person)
    (cpj/DELETE "/people/:id" [id] (delete-person id))
    (cpj/PATCH "/people/:id" [] patch-person))
   wrap-authorize-admin!))

(def routes
  (->
   (cpj/routes
    (cpj/GET "/people/:id" [id] get-person)
    (cpj/ANY "*" _ admin-protected-routes))))

;### Debug ####################################################################
;(debug/debug-ns *ns*)
