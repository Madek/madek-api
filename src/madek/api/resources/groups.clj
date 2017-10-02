(ns madek.api.resources.groups
  (:require
    [madek.api.constants :refer [presence]]
    [madek.api.pagination :as pagination]
    [madek.api.pagination :as pagination]
    [madek.api.resources.media-entries.index :refer [get-index]]
    [madek.api.resources.media-entries.media-entry :refer [get-media-entry]]
    [madek.api.resources.shared :as shared]
    [madek.api.utils.rdbms :as rdbms]
    [madek.api.utils.sql :as sql]

    [clj-uuid]
    [clojure.java.jdbc :as jdbc]
    [compojure.core :as cpj]
    [ring.util.codec :refer [url-decode]]

    [clj-logging-config.log4j :as logging-config]
    [clojure.tools.logging :as logging]
    [logbug.debug :as debug]
    ))


(defn id-where-clause [id]
  (if (re-matches
        #"[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}"
        id)
    (sql/where [:or
                [:= :id id]
                [:= :institutional_id id]])
    (sql/where [:= :institutional_id id])))

(defn jdbc-id-where-clause [id]
  (-> id id-where-clause sql/format
      (update-in [0] #(clojure.string/replace % "WHERE" ""))))


;### create group #############################################################

(defn create-group [request]
  (let [params (as-> (:body request) params
                 (or params {})
                 (assoc params :id (or (:id params) (clj-uuid/v4))))]
    {:body (dissoc
             (->> (jdbc/insert!
                    (rdbms/get-ds) :groups params)
                  first)
             :previous_id :searchable)
     :status 201}))


;### get group ################################################################

(defn find-group-sql [id]
  (-> (id-where-clause id)
      (sql/select :*)
      (sql/from :groups)
      sql/format))

(defn get-group [id-or-institutinal-group-id]
  (if-let [group (->> id-or-institutinal-group-id find-group-sql
                      (jdbc/query (rdbms/get-ds)) first)]
    {:body (dissoc group :previous_id :searchable)}
    {:status 404 :body "No such group found"}))


;### delete group ##############################################################

(defn delete-group [id]
  (if (= 1 (first (jdbc/delete! (rdbms/get-ds)
                                :groups (jdbc-id-where-clause id))))
    {:status 204}
    {:status 404}))


;### patch group ##############################################################

(defn patch-group [{body :body {id :id} :params}]
  (if (= 1 (first (jdbc/update! (rdbms/get-ds) :groups body (jdbc-id-where-clause id))))
    {:body (->> id find-group-sql
                (jdbc/query (rdbms/get-ds)) first)}
    {:status 404}))

;### index ####################################################################

(defn build-index-query [{query-params :query-params}]
  (-> (sql/select :id)
      (sql/from :groups)
      (sql/order-by [:id :asc])
      (pagination/add-offset-for-honeysql query-params)
      sql/format))

(defn index [request]
  {:body
   {:groups (jdbc/query (rdbms/get-ds) (build-index-query request))}})

;### admin check ##############################################################

(defn authorize-admin! [request handler]
  "Checks if the authenticated-entity is an admin by either
  checking (-> request :authenticated-entity :is_admin) if present or performing
  an db query.  If so adds {:is_amdin true} to the requests an calls handler.
  Throws a WebstackException with status 403 otherwise. "
  (handler
    (or
      (if (contains? (-> request :authenticated-entity) :is_admin)
        (when (-> request :authenticated-entity :is_admin) request)
        (when (->> (-> (sql/select [true :is_admin])
                       (sql/from :admins)
                       (sql/merge-where [:= :admins.user_id (-> request :authenticated-entity :id)])
                       sql/format)
                   (jdbc/query (rdbms/get-ds))
                   first :is_admin)
          (assoc-in request [:authenticated-entity :is_admin] true)))
      (throw
        (madek.api.WebstackException.
          "Only administrators are allowed to access this resource."
          {:status 403
           :body "Only administrators are allowed to access this resource." })))))

(defn wrap-authorize-admin! [handler]
  (fn [req]
    (authorize-admin! req handler)))

;### routes ###################################################################

(def routes
  (-> (cpj/routes
        (cpj/GET "/groups/" [] index)
        (cpj/POST "/groups/" [] create-group)
        (cpj/GET "/groups/:id" [id] (get-group id))
        (cpj/DELETE "/groups/:id" [id] (delete-group id))
        (cpj/PATCH "/groups/:id" [] patch-group))
      wrap-authorize-admin!))

;### Debug ####################################################################
;(logging-config/set-logger! :level :debug)
;(logging-config/set-logger! :level :info)
;(debug/debug-ns *ns*)
