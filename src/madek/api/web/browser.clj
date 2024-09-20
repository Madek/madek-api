(ns madek.api.web.browser
  (:require
   [clojure.tools.logging :as logging]
   [compojure.core :as cpj :refer [defroutes GET PUT POST DELETE ANY]]
   [logbug.catcher :as catcher]
   [logbug.debug :as debug :refer [I> I>>]]
   [logbug.ring :as logbug-ring :refer [wrap-handler-with-logging]]
   [logbug.thrown :as thrown]
   [ring.middleware.resource :as resource]))

(defn- remove-context [request]
  (let [context (:context request)
        context-lenth (count context)]
    (assoc request
           :uri (subs (:uri request) context-lenth))))

(defn- expand-with-index-html [request]
  "Adds index.html if the path-info ends with /"
  (let [path-info (:path-info request)]
    (if (re-matches #".+/$" path-info)
      (assoc request :path-info (str path-info "index.html"))
      request)))

(defn static-resources-handler [request]
  (resource/resource-request (-> request
                                 remove-context
                                 expand-with-index-html) ""))

(defn wrap [default-handler]
  (cpj/routes
   (cpj/GET "/browser*" request static-resources-handler)
   (cpj/ANY "*" request default-handler)))

;### Debug ####################################################################
;(debug/debug-ns *ns*)
