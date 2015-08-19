(ns madek.api.resources.meta-data.index
  (:require
    [madek.api.authorization :as authorization]
    [cider-ci.utils.rdbms :as rdbms :refer [get-ds]]
    [clj-logging-config.log4j :as logging-config]
    [clojure.java.jdbc :as jdbc]
    [clojure.tools.logging :as logging]
    [compojure.core :as cpj]
    [drtom.logbug.debug :as debug]
    [drtom.logbug.ring :refer [wrap-handler-with-logging]]
    [madek.api.pagination :as pagination]
    [madek.api.resources.shared :as shared]
    [madek.api.sql :refer :all]
    [drtom.logbug.catcher :as catcher]
    ))

(defn get-media-resource
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
         (assoc resource :type type)))))


(defn get-meta-data [media-resource]
  (case (:type media-resource)
    "MediaEntry" (jdbc/query (get-ds)
                             [(str "SELECT id, type, meta_key_id from meta_data"
                                   "  WHERE media_entry_id = ? ")
                              (:id media-resource)])))


(defn get-index [request]
  (if-let [media-resource (get-media-resource request)]
    (if (authorization/authorized? request media-resource)
      (let [meta-data (get-meta-data media-resource)]
        {:body
         (conj
           {:meta-data meta-data}
           (case (:type media-resource)
             "MediaEntry" {:media_entry_id (:id media-resource)}))})
      {:status 403})
    {:status 404}))

;### Debug ####################################################################
;(logging-config/set-logger! :level :debug)
;(logging-config/set-logger! :level :info)
;(debug/debug-ns *ns*)
