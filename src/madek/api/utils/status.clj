; Copyright © 2013 - 2017 Dr. Thomas Schank <Thomas.Schank@AlgoCon.ch>
; Licensed under the terms of the GNU Affero General Public License v3.
; See the "LICENSE.txt" file provided with this software.

(ns madek.api.utils.status
  (:require
    [clj-commons-exec :as commons-exec]
    [clojure.data.json :as json]
    [clojure.tools.logging :as logging]
    [compojure.core :as cpj]
    [compojure.handler :as cpj.handler]
    [logbug.debug :as debug]
    [madek.api.utils.rdbms :as rdbms]
    [madek.api.utils.runtime :as runtime]
    ))

(defn status-handler [request]
  (let [rdbms-status (rdbms/check-connection)
        memory-status (runtime/check-memory-usage)
        body (json/write-str {:memory memory-status
                              :rdbms rdbms-status
                              })]
    {:status (if (every? identity (map :OK? [rdbms-status rdbms-status]))
               200 499)
     :body body
     :headers {"content-type" "application/json; charset=utf-8"}}))

(defn wrap [default-handler]
  (cpj/routes
    (cpj/GET "/status" request #'status-handler)
    (cpj/ANY "*" request default-handler)))
