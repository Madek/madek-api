; Copyright © 2013 - 2017 Dr. Thomas Schank <Thomas.Schank@AlgoCon.ch>
; Licensed under the terms of the GNU Affero General Public License v3.
; See the "LICENSE.txt" file provided with this software.


(ns madek.api.utils.http-server
  (:require
    [clj-logging-config.log4j :as logging-config]
    [clojure.tools.logging :as logging]
    [aleph.http :as http-server]
    )
  (:import
    ))


(defonce _server (atom nil))

(defn stop []
  (when-let [server @_server]
    (logging/info stop)
    (.close server)
    (reset! _server nil)))

(defn start [conf main-handler ]
  "Starts (or stops and then starts) the webserver"
  (let [server-conf (conj {:ssl? false
                           :join? false}
                          (select-keys conf [:port :host]))]
    (stop)
    (logging/info "starting server " server-conf)
    (reset! _server (http-server/start-server main-handler server-conf)))
  (.addShutdownHook (Runtime/getRuntime) (Thread. (fn [] (stop)))))

