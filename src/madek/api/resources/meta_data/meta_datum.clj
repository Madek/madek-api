(ns madek.api.resources.meta-data.meta-datum
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
    [drtom.logbug.catcher :as catcher]
    ))

(defn- prepare-meta-datum [meta-datum]
  (merge (select-keys meta-datum [:id :meta_key_id :type])
         (case (:type meta-datum)
           "MetaDatum::Text" {:value (:string meta-datum)})
          (->> (select-keys meta-datum [:media_entry_id :collection_id :filter_set_id])
               (filter (fn [[k v]] v))
               (into {}))))

(defn get-meta-datum [request]
  (let [meta-datum (:meta-datum request)]
    {:body (prepare-meta-datum meta-datum)}))

;### Debug ####################################################################
;(logging-config/set-logger! :level :debug)
;(logging-config/set-logger! :level :info)
(debug/debug-ns *ns*)
