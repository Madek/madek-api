(ns madek.api.web
  (:require
    [cider-ci.open-session.cors :as cors]
    [cider-ci.utils.config :refer [get-config]]
    [cider-ci.utils.http-server :as http-server]
    [clj-logging-config.log4j :as logging-config]
    [clojure.java.io :as io]
    [clojure.tools.logging :as logging]
    [clojure.walk :refer [keywordize-keys]]
    [compojure.core :as cpj :refer [defroutes GET PUT POST DELETE ANY]]
    [compojure.handler :refer [site]]
    [compojure.route :as route]
    [drtom.logbug.catcher :as catcher]
    [drtom.logbug.debug :as debug]
    [drtom.logbug.ring :refer [wrap-handler-with-logging]]
    [drtom.logbug.thrown :as thrown]
    [environ.core :refer [env]]
    [json-roa.ring-middleware.request :as json-roa_request]
    [json-roa.ring-middleware.response :as json-roa_response]
    [madek.api.authentication :as authentication]
    [madek.api.management :as management]
    [madek.api.json-protocol]
    [madek.api.json-roa]
    [madek.api.resources]
    [madek.api.semver :as semver]
    [ring.adapter.jetty :as jetty]
    [ring.middleware.json]
    [ring.middleware.resource :as resource]
    ))


;### helper ###################################################################

(defn wrap-keywordize-request [handler]
  (fn [request]
    (-> request keywordize-keys handler)))


;### api-context ##############################################################

(defn wrap-context
  "Check for context match. Pass on and add :context, or return 404 if it doesn't match."
  [default-handler context]
  (cpj/routes
    (cpj/context context []
                 (cpj/ANY "*" _ default-handler))
    (cpj/ANY "*" [] {:status 404 :body
                     {:message (str "404 NOT FOUND, only resources under " context " are known")}})))

(defn get-context []
  (or (env :api-context) "/api"))


;### static resources #########################################################

(defn static-resources-handler [request]
  (let [context (:context request)
        context-lenth (count context)
        wo-prefix-request (assoc request
                                 :uri (subs (:uri request) context-lenth))]
    (logging/debug wo-prefix-request)
    (resource/resource-request wo-prefix-request "")))

(defn wrap-static-resources-dispatch [default-handler]
  (cpj/routes
    (cpj/GET "/browser*" request static-resources-handler)
    (cpj/ANY "*" request default-handler)))


;### routes ###################################################################

(defn- wrap-exception
  ([handler]
   (fn [request]
     (wrap-exception request handler)
     ))
  ([request handler]
   (try
     (handler request)
     (catch Exception ex
       (logging/error "An exception was thrown in the webstack: "  (thrown/stringify ex))
       {:status 500
        :body (.getMessage ex)}))))

;### routes ###################################################################

(def ^:private dead-end-handler
  (cpj/routes
    (cpj/ANY "*" _ {:status 404 :body {:message "404 NOT FOUND"}})
    ))

(def root
  {:status 200

   :body {:api-version (semver/get-semver)
          :message "Hello Madek User!"}})

(defn wrap-public-routes [handler]
  (cpj/routes
    (cpj/GET "/" _ root )
    (cpj/ANY "*" _ handler)
    ))


(defn build-site [context]
  (-> dead-end-handler
      (wrap-handler-with-logging 'madek.api.web)
      madek.api.resources/wrap-api-routes
      (wrap-handler-with-logging 'madek.api.web)
      authentication/wrap
      (wrap-handler-with-logging 'madek.api.web)
      management/wrap
      (wrap-handler-with-logging 'madek.api.web)
      wrap-static-resources-dispatch
      (wrap-handler-with-logging 'madek.api.web)
      wrap-public-routes
      (wrap-handler-with-logging 'madek.api.web)
      wrap-keywordize-request
      (wrap-handler-with-logging 'madek.api.web)
      (json-roa_request/wrap madek.api.json-roa/handler)
      (wrap-handler-with-logging 'madek.api.web)
      ring.middleware.json/wrap-json-params
      (wrap-handler-with-logging 'madek.api.web)
      cors/wrap
      (wrap-handler-with-logging 'madek.api.web)
      site
      (wrap-handler-with-logging 'madek.api.web)
      (wrap-context context)
      (wrap-handler-with-logging 'madek.api.web)
      json-roa_response/wrap
      (wrap-handler-with-logging 'madek.api.web)
      ring.middleware.json/wrap-json-response
      (wrap-handler-with-logging 'madek.api.web)
      wrap-exception
      (wrap-handler-with-logging 'madek.api.web)))


;### server ###################################################################

(defonce server (atom nil))
(defn start-server [& [port]]
  (catcher/wrap-with-log-error
    (when @server
      (.stop @server)
      (reset! server nil))
    (let [port (Integer. (or port
                             (env :http-port)
                             (-> (get-config) :api_service :port)
                             3100))]
      (reset! server
              (jetty/run-jetty (build-site)
                               {:port port :join? false})))))


(defn initialize []
  (let [http-conf (-> (get-config) :services :api :http)
        context (str (:context http-conf) (:sub_context http-conf))]
    (http-server/start http-conf (build-site context))))




;### Debug ####################################################################
;(logging-config/set-logger! :level :debug)
;(logging-config/set-logger! :level :info)
;(debug/debug-ns *ns*)
