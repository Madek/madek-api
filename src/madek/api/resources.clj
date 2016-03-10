(ns madek.api.resources
  (:require
    [cider-ci.utils.rdbms :as rdbms :refer [get-ds]]
    [clj-logging-config.log4j :as logging-config]
    [clojure.java.jdbc :as jdbc]
    [clojure.tools.logging :as logging]
    [compojure.core :as cpj]
    [logbug.catcher :as catcher]
    [logbug.debug :as debug]
    [madek.api.authorization :refer [authorized?]]
    [madek.api.resources.auth-info :as auth-info]
    [madek.api.resources.collections :as collections]
    [madek.api.resources.filter-sets :as filter-sets]
    [madek.api.resources.media-files :as media-files]
    [madek.api.resources.media-entries :as media-entries]
    [madek.api.resources.media-entries.media-entry :refer [get-media-entry-for-preview]]
    [madek.api.resources.meta-data :as meta-data]
    [madek.api.resources.meta-keys :as meta-keys]
    [madek.api.resources.keywords :as keywords]
    [madek.api.resources.licenses :as licenses]
    [madek.api.resources.people :as people]
    [madek.api.resources.previews :as previews]
    [madek.api.resources.shared :as shared]
    [madek.api.resources.vocabularies :as vocabularies]
    ))


;### wrap media resource ######################################################

(defn- get-media-resource
  ([request]
   (catcher/with-logging {}
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
    (cpj/GET "/filter-sets/:filter_set_id*" _ get-media-resource)
    (cpj/GET "/previews/:preview_id*" _ #(assoc (get-media-entry-for-preview %)
                                                :type "MediaEntry"
                                                :table-name "media_entries"))))

(defn- add-media-resource [request handler]
  (if-let [media-resource (get-media-resource-dispatcher request)]
    (let [request-with-media-resource (assoc request :media-resource media-resource)]
      (logging/info 'request-with-media-resource request-with-media-resource)
      (handler request-with-media-resource))
    (handler request)))

(defn- wrap-add-media-resource [handler]
  (fn [request]
    (add-media-resource request handler)))

;### wrap meta-datum with media-resource#######################################

(defn query-meta-datum [request]
  (let [id (-> request :params :meta_datum_id)]
    (or (-> (jdbc/query (get-ds)
                        [(str "SELECT * FROM meta_data "
                              "WHERE id = ? ") id])
            first)
        (throw (IllegalStateException. (str "We expected to find a MetaDatum for "
                                            id " but did not."))))))

(defn- query-media-resource-for-meta-datum [meta-datum]
  (or (when-let [id (:media_entry_id meta-datum)]
        (get-media-resource {:params {:media_entry_id id}}
                            :media_entry_id "media_entries" "MediaEntry"))
      (when-let [id (:collection_id meta-datum)]
        (get-media-resource {:params {:collection_id id}}
                            :collection_id "collections" "Collection"))
      (when-let [id (:filter_set_id meta-datum)]
        (get-media-resource {:params {:filter_set_id id}}
                            :filter_set_id "filter_sets" "FilterSet"))
      (throw (IllegalStateException. (str "Getting the resource for "
                                          meta-datum "
                                          is not implemented yet.")))))

(def ^:private query-meta-datum-dispatcher
  (cpj/routes
    (cpj/GET "/meta-data/:meta_datum_id*" [meta_datum_id] query-meta-datum)))

(defn- add-meta-datum-with-media-resource [request handler]
  (if-let [meta-datum (query-meta-datum-dispatcher request)]
    (let [media-resource (query-media-resource-for-meta-datum meta-datum)]
      (handler (assoc request
                      :meta-datum meta-datum
                      :media-resource media-resource)))
    (handler request)))

(defn- wrap-add-meta-datum-with-media-resource [handler]
  (fn [request]
    (add-meta-datum-with-media-resource request handler)))



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
    (let [response  {:status 500 :body "No media-resource in request."}]
      (logging/warn 'authorize-request-for-handler response [request handler])
      response)))

(defn- dispatch-authorize [request handler]
  ((cpj/routes
     (cpj/GET "/media-entries/:media_entry_id*" _ #(authorize-request-for-handler % handler))
     (cpj/GET "/collections/:collection_id*" _ #(authorize-request-for-handler % handler))
     (cpj/GET "/filter-sets/:filter_set_id*" _ #(authorize-request-for-handler % handler))
     (cpj/GET "/meta-data/:meta_datum_id*" _ #(authorize-request-for-handler % handler))
     (cpj/GET "/previews/:preview_id*" _ #(authorize-request-for-handler % handler))
     (cpj/ANY "*" _ handler)) request))

(defn- wrap-authorization [handler]
  (fn [request]
    (dispatch-authorize request handler)))

;### ##### ####################################################################

(defn wrap-api-routes [default-handler]
  (-> (cpj/routes
        (cpj/GET "/auth-info" _ auth-info/routes)
        (cpj/ANY "/:media_resource_type/:id/meta-data/" _ meta-data/routes)
        (cpj/ANY "/collections*" _ collections/routes)
        (cpj/ANY "/filter-sets*" _ filter-sets/routes)
        (cpj/ANY "/licenses/:license_id*" _ licenses/routes)
        (cpj/ANY "/keywords/:keyword_id*" _ keywords/routes)
        (cpj/ANY "/media-entries*" _ media-entries/routes)
        (cpj/ANY "/media-files/:media_file_id*" _ media-files/routes)
        (cpj/ANY "/meta-data/:meta_datum_id" _ meta-data/routes)
        (cpj/ANY "/meta-keys/*" _ meta-keys/routes)
        (cpj/ANY "/people/:person_id*" _ people/routes)
        (cpj/ANY "/previews/:preview_id*" _ previews/routes)
        (cpj/ANY "/vocabularies/*" _ vocabularies/routes)
        (cpj/ANY "*" _ default-handler)
        )
      wrap-authorization
      wrap-add-media-resource
      wrap-add-meta-datum-with-media-resource))

;### Debug ####################################################################
;(logging-config/set-logger! :level :debug)
;(logging-config/set-logger! :level :info)
;(debug/debug-ns *ns*)
