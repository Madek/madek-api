; Copyright © 2013 - 2017 Dr. Thomas Schank <Thomas.Schank@AlgoCon.ch>
; Licensed under the terms of the GNU Affero General Public License v3.
; See the "LICENSE.txt" file provided with this software.

(ns madek.api.utils.nrepl
  (:require
    [clojure.tools.logging :as logging]
    [clojure.tools.nrepl.server :as nrepl-server]
    [logbug.catcher :as catcher]
    ))

(declare start-server)

(defonce conf (atom nil))

(defn initialize [new-conf]
  (logging/info [initialize new-conf])
  (reset! conf new-conf)
  (when-not @conf (throw (IllegalStateException. "not configured")))
  (start-server))


(defonce ^:private server nil)

(defn stop-server []
  (catcher/with-logging {}
    (logging/info "stopping server")
    (nrepl-server/stop-server server)
    (def ^:private server nil)))

(defn start-server []
  (logging/debug start-server [])
  (catcher/with-logging {}
    (when server (stop-server))
    (if (:enabled @conf)
      (let [args (flatten (seq (select-keys @conf [:port :bind])))]
        (do
          (logging/info "starting server " (with-out-str (clojure.pprint/pprint args)))
          (def server (apply nrepl-server/start-server args )))
        (.addShutdownHook (Runtime/getRuntime)
                          (Thread. (fn [] (stop-server))))))))

