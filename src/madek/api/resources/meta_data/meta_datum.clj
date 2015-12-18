(ns madek.api.resources.meta-data.meta-datum
  (:require
    [madek.api.pagination :as pagination]
    [madek.api.resources.shared :as shared]
    [madek.api.resources.keywords.index :as keywords]
    [madek.api.resources.people.index :as people]
    [madek.api.resources.licenses.index :as licenses]

    [madek.api.authorization :as authorization]
    [cider-ci.utils.rdbms :as rdbms :refer [get-ds]]
    [clojure.java.jdbc :as jdbc]
    [compojure.core :as cpj]
    [honeysql.sql :refer :all]

    [clj-logging-config.log4j :as logging-config]
    [clojure.tools.logging :as logging]
    [logbug.debug :as debug]
    [logbug.catcher :as catcher]
    ))

;### meta-datum ###############################################################

; TODO meta-datum groups will be moved to people, no point in implementing this
; here and now, the following is a Hack so the server so it won't fail when
; groups are requested
(defn groups-with-ids [meta-datum]
  []
  )

;### meta-datum ###############################################################

; TODO people/get-index, keywords/get-index, licenses/get-index is very
; un-intuitive;  it has nothing to do with HTTP get-index which it suggest; =>
; delete all those namespaces and move the stuff over here, somthing like (def
; people-with-ids [meta-datum] ...  and so on

(defn- prepare-meta-datum [meta-datum]
  (merge (select-keys meta-datum [:id :meta_key_id :type])
         {:value (let [meta-datum-type (:type meta-datum)]
                   (if (or (= meta-datum-type "MetaDatum::Text")
                           (= meta-datum-type "MetaDatum::TextDate"))
                     (:string meta-datum)
                     (map #(select-keys % [:id])
                          ((case meta-datum-type
                             "MetaDatum::People" people/get-index
                             "MetaDatum::Keywords" keywords/get-index
                             "MetaDatum::Licenses" licenses/get-index
                             "MetaDatum::Groups" groups-with-ids)
                           meta-datum))))}
         (->> (select-keys meta-datum [:media_entry_id :collection_id :filter_set_id])
              (filter (fn [[k v]] v))
              (into {}))))

(defn get-meta-datum [request]
  (let [meta-datum (:meta-datum request)]
    {:body (prepare-meta-datum meta-datum)}))

;### Debug ####################################################################
;(logging-config/set-logger! :level :debug)
;(logging-config/set-logger! :level :info)
;(debug/debug-ns *ns*)
