(ns madek.api.authentication
  (:require
    [madek.api.authentication.basic :as basic-auth]
    [madek.api.authentication.session :as session-auth]
    [madek.api.authentication.token :as token-auth]

    [madek.api.utils.rdbms :as rdbms]

    [clj-logging-config.log4j :as logging-config]
    [clojure.tools.logging :as logging]
    [logbug.debug :as debug]
    [logbug.thrown :as thrown]
    ))

(defn- add-www-auth-header-if-401 [response]
  (case (:status response)
    401 (assoc-in response [:headers "WWW-Authenticate"]
                  (str "Basic realm=\"Madek ApiClient with password"
                       " or User with token.\""))
    response))

(defn wrap [handler]
  (fn [request]
    (let [response ((-> handler
                        session-auth/wrap
                        basic-auth/wrap
                        token-auth/wrap
                        ) request)]
      (add-www-auth-header-if-401 response))))


;### Debug ####################################################################
;(logging-config/set-logger! :level :debug)
;(logging-config/set-logger! :level :info)
;(debug/debug-ns *ns*)
