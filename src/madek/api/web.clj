(ns madek.api.web
  (:require
    [madek.api.authentication :as authentication]
    [madek.api.authorization :as authorization]
    [madek.api.json-protocol]
    [madek.api.json-roa]
    [madek.api.management :as management]
    [madek.api.resources]
    [madek.api.semver :as semver]
    [madek.api.web.browser :as web.browser]

    [madek.api.utils.config :refer [get-config]]
    [madek.api.utils.http-server :as http-server]
    [madek.api.utils.status :as status]
    [clojure.data.json :as json]
    [clojure.java.io :as io]
    [clojure.walk :refer [keywordize-keys]]
    [compojure.core :as cpj :refer [defroutes GET PUT POST DELETE ANY]]
    [compojure.handler :refer [site]]
    [compojure.route :as route]
    [environ.core :refer [env]]
    [json-roa.ring-middleware.request :as json-roa_request]
    [json-roa.ring-middleware.response :as json-roa_response]
    [ring.adapter.jetty :as jetty]
    [ring.middleware.json]
    [ring.middleware.cors :as cors-middleware]

    [clj-logging-config.log4j :as logging-config]
    [clojure.tools.logging :as logging]
    [logbug.catcher :as catcher]
    [logbug.debug :as debug :refer [I> I>>]]
    [logbug.ring :as logbug-ring :refer [wrap-handler-with-logging]]
    [logbug.thrown :as thrown]
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



;### routes ###################################################################

(defn- wrap-exception
  ([handler]
   (fn [request]
     (wrap-exception request handler)
     ))
  ([request handler]
   (try
     (handler request)
     (catch madek.api.WebstackException ex
       (if-let [status (-> ex ex-data :status)]
         {:status status
          :body {:message (.getMessage ex)}}
         {:status 500
          :body {:message (.getMessage ex)}}))
     (catch Exception ex
       (logging/error "An exception was thrown in the webstack: "  (thrown/stringify ex))
       {:status 500
        :body {:message (.getMessage ex)}}))))


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


;### wrap json encoded query params ###########################################

(defn try-as-json [value]
  (try (cheshire.core/parse-string value)
       (catch Exception _
         value)))

(defn- *wrap-parse-json-query-parameters [request handler]
  (handler (assoc request :query-params
                  (->> request :query-params
                       (map (fn [[k v]] [k (try-as-json v)] ))
                       (into {})))))

(defn- wrap-parse-json-query-parameters [handler]
  (fn [request]
    (*wrap-parse-json-query-parameters request handler)))

;### wrap CORS ###############################################################

(defn add-access-control-allow-credentials [response]
  (assoc-in response [:headers "Access-Control-Allow-Credentials"] true))

(defn wrap-with-access-control-allow-credentials [handler]
  (fn [request]
    (add-access-control-allow-credentials (handler request))))

(defn wrap-cors-if-configured [handler doit]
  (if doit
    (-> handler
        (cors-middleware/wrap-cors
          :access-control-allow-origin [#".*"]
          :access-control-allow-methods [:get :put :post :delete]
          :access-control-allow-headers ["Origin" "X-Requested-With" "Content-Type" "Accept" "Authorization"])
        wrap-with-access-control-allow-credentials)
    handler))


;##############################################################################

(defn build-site [context]
  (I> wrap-handler-with-logging
      dead-end-handler
      madek.api.resources/wrap-api-routes
      authorization/wrap-authorize-http-method
      authentication/wrap
      management/wrap
      web.browser/wrap
      wrap-public-routes
      wrap-keywordize-request
      (json-roa_request/wrap madek.api.json-roa/handler)
      wrap-parse-json-query-parameters
      (wrap-cors-if-configured (-> (get-config) :services :api :cors_enabled))
      status/wrap
      site
      (wrap-context context)
      wrap-exception
      json-roa_response/wrap
      (ring.middleware.json/wrap-json-body {:keywords? true})
      ring.middleware.json/wrap-json-response))


;### server ###################################################################

(defonce server (atom nil))

(defn start-server [& [port]]
  (catcher/with-logging {}
    (when @server
      (.stop @server)
      (reset! server nil))
    (let [port (Integer. (or port
                             (env :http-port)
                             (-> (get-config) :api_service :port)
                             3100))]
      (reset! server
              (jetty/run-jetty (build-site)
                               {:port port
                                :host "localhost"
                                :join? false})))))

(defn initialize []
  (let [http-conf (-> (get-config) :services :api :http)
        context (str (:context http-conf) (:sub_context http-conf))]
    (http-server/start http-conf (build-site context))))



;### Debug ####################################################################
;(logging-config/set-logger! :level :debug)
;(logging-config/set-logger! :level :info)
;(debug/debug-ns *ns*)
