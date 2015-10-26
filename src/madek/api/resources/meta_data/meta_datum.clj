(ns madek.api.resources.meta-data.meta-datum
  (:require
    [madek.api.authorization :as authorization]
    [cider-ci.utils.rdbms :as rdbms :refer [get-ds]]
    [clj-logging-config.log4j :as logging-config]
    [clojure.java.jdbc :as jdbc]
    [clojure.tools.logging :as logging]
    [compojure.core :as cpj]
    [logbug.debug :as debug]
    [honeysql.sql :refer :all]
    [madek.api.pagination :as pagination]
    [madek.api.resources.shared :as shared]
    [madek.api.resources.people.index :as people]
    [logbug.catcher :as catcher]
    ))

(defn- prepare-meta-datum [meta-datum]
  (debug/identity-with-logging
    'madek.api.resources.meta-data.meta-datum
    (merge (select-keys meta-datum [:id :meta_key_id :type])
           {:value (case (:type meta-datum)
                     "MetaDatum::Text" (:string meta-datum)
                     "MetaDatum::People" (map #(select-keys % [:id])
                                              (people/get-index meta-datum)))}
           (->> (select-keys meta-datum [:media_entry_id :collection_id :filter_set_id])
                (filter (fn [[k v]] v))
                (into {})))))

(defn get-meta-datum [request]
  (let [meta-datum (:meta-datum request)]
    {:body (prepare-meta-datum meta-datum)}))

;### Debug ####################################################################
(logging-config/set-logger! :level :debug)
;(logging-config/set-logger! :level :info)
;(debug/debug-ns *ns*)
