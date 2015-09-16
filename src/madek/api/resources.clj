(ns madek.api.resources
  (:require
    [cider-ci.utils.rdbms :as rdbms :refer [get-ds]]
    [clj-logging-config.log4j :as logging-config]
    [clojure.java.jdbc :as jdbc]
    [clojure.tools.logging :as logging]
    [compojure.core :as cpj]
    [drtom.logbug.catcher :as catcher]
    [drtom.logbug.debug :as debug]
    [drtom.logbug.ring :refer [wrap-handler-with-logging]]
    [madek.api.authorization :refer [authorized?]]
    [madek.api.resources.auth-info :as auth-info]
    [madek.api.resources.collections :as collections]
    [madek.api.resources.media-entries :as media-entries]
    [madek.api.resources.meta-data :as meta-data]
    [madek.api.resources.shared :as shared]
    ))

;### wrap media resource ######################################################

(defn- get-media-resource
  ([request]
   (catcher/wrap-with-log-error
     (or (get-media-resource request :media_entry_id "media_entries" "MediaEntry")
         (get-media-resource request :collection_id "collections" "Collection")
         (get-media-resource request :filter_set_id "filter_sets" "FilterSet"))))
  ([request id-key table-name type]
   (when-let [id (-> request :params id-key)]
     (when-let [resource (-> (jdbc/query (get-ds)
                                         [(str "SELECT * FROM " table-name "
                                               WHERE id = ?") id]) first)]
         (assoc resource :type type :table-name table-name)))))

(def ^:private get-media-resource-dispatcher
  (cpj/routes
    (cpj/GET "/media-entries/:media_entry_id*" _ get-media-resource)
    (cpj/GET "/collections/:collection_id*" _ get-media-resource)
    (cpj/GET "/filter-sets/:filter_set_id*" _ get-media-resource)))

(defn- add-media-resource [request handler]
  (if-let [media-resource (get-media-resource-dispatcher request)]
    (let [request-with-media-resource (assoc request :media-resource media-resource)]
      (logging/info 'request-with-media-resource request-with-media-resource)
      (handler request-with-media-resource))
    (handler request)))

(defn- wrap-add-media-resource [handler]
  (fn [request]
    (add-media-resource request handler)))

;### wrap authorize ###########################################################

(defn- public? [resource]
  (-> resource :get_metadata_and_previews boolean))

(defn- authorize-request-for-handler [request handler]
  (if-let [media-resource (:media-resource request)]
    (if (public? media-resource)
      (handler request)
      (if-let [auth-entity (:authenticated-entity request)]
        (if (authorized? auth-entity media-resource)
          (handler request)
          {:status 403})
        {:status 401}))
    {:status 500 :body "No media-resource in request."}))

(defn- dispatch-authorize [request handler]
  ((cpj/routes
     (cpj/GET "/media-entries/*" _ #(authorize-request-for-handler % handler))
     (cpj/GET "/collections/*" _ #(authorize-request-for-handler % handler))
     (cpj/GET "/filter-sets/*" _ #(authorize-request-for-handler % handler))
     (cpj/ANY "*" _ handler)) request))

(defn- wrap-authorization [handler]
  (fn [request]
    (dispatch-authorize request handler)))

;### ##### ####################################################################

(defn wrap-api-routes [default-handler]
  (-> (cpj/routes
        (cpj/ANY "/media-entries/:media_entry_id/meta-data/" _ meta-data/routes)
        (cpj/ANY "/media-entries*" _ media-entries/routes)
        (cpj/ANY "/collections*" _ collections/routes)
        (cpj/GET "/auth-info" _ auth-info/routes)
        (cpj/ANY "*" _ default-handler))
      wrap-authorization
      wrap-add-media-resource))

;### Debug ####################################################################
;(logging-config/set-logger! :level :debug)
;(logging-config/set-logger! :level :info)
;(debug/debug-ns *ns*)
