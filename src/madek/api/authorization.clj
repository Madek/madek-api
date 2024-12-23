(ns madek.api.authorization
  (:require
   [clojure.java.jdbc :as jdbc]
   [clojure.tools.logging :as logging]
   [logbug.catcher :as catcher]
   [logbug.debug :as debug]
   [logbug.thrown :as thrown]
   [madek.api.resources.collections.permissions :as collection-perms :only [viewable-by-auth-entity?]]
   [madek.api.resources.media-entries.permissions :as media-entry-perms :only [viewable-by-auth-entity?]]
   [madek.api.utils.rdbms :as rdbms]))

(defn authorized? [auth-entity resource]
  (case (:type resource)
    "MediaEntry" (media-entry-perms/viewable-by-auth-entity?
                  resource auth-entity)
    "Collection" (collection-perms/viewable-by-auth-entity?
                  resource auth-entity)
    false))

(defn authorized?! [request resource]
  (or authorized?
      (throw (ex-info "Forbidden" {:status 403}))))

(def destructive-methods #{:post :put :delete})

(defn wrap-authorize-http-method [handler]
  (fn [request]
    (if (and (= (request :authentication-method) "Session")
             (destructive-methods (request :request-method)))
      {:status 405,
       :body {:message "Destructive methods not allowed for session authentication!"}}
      (handler request))))

;### Debug ####################################################################
;(debug/debug-ns *ns*)
