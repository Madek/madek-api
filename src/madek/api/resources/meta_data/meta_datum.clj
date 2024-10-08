(ns madek.api.resources.meta-data.meta-datum
  (:require
   [clojure.java.jdbc :as jdbc]
   [clojure.tools.logging :as logging]
   [compojure.core :as cpj]
   [logbug.catcher :as catcher]
   [logbug.debug :as debug]
   [madek.api.authorization :as authorization]
   [madek.api.pagination :as pagination]
   [madek.api.resources.keywords.index :as keywords]
   [madek.api.resources.shared :as shared]
   [madek.api.utils.rdbms :as rdbms :refer [get-ds]]
   [madek.api.utils.sql :as sql]
   [ring.util.response :as ring-response]))

;### people ###################################################################

; TODO meta-datum groups will be moved to people, no point in implementing this
; here and now, the following is a Hack so the server so it won't fail when
; groups are requested
(defn groups-with-ids [meta-datum]
  [])

(defn get-people-index [meta-datum]
  (let [query (-> (sql/select :people.*)
                  (sql/from :people)
                  (sql/merge-join
                   :meta_data_people
                   [:= :meta_data_people.person_id :people.id])
                  (sql/merge-where
                   [:= :meta_data_people.meta_datum_id (:id meta-datum)])
                  (sql/order-by [:people.last_name :asc]
                                [:people.first_name :asc]
                                [:people.id :asc])
                  (sql/format))]
    (jdbc/query (rdbms/get-ds) query)))

;### meta-datum ###############################################################

; TODO people/get-index, keywords/get-index is very un-intuitive,
; it has nothing to do with HTTP get-index which it suggest; =>
; delete all those namespaces and move the stuff over here, somthing like (def
; people-with-ids [meta-datum] ...  and so on

(defn find-meta-data-roles
  [meta-datum]
  (let [query (-> (sql/select :meta_data_roles.*)
                  (sql/from :meta_data_roles)
                  (sql/merge-where
                   [:= :meta_data_roles.meta_datum_id (:id meta-datum)])
                  (sql/order-by [:meta_data_roles.position :asc]
                                [:meta_data_roles.id :asc])
                  (sql/format))]
    (jdbc/query (rdbms/get-ds) query)))

(defn- find-meta-datum-role
  [id]
  (let [query (-> (sql/select :meta_data_roles.*)
                  (sql/from :meta_data_roles)
                  (sql/merge-where
                   [:= :meta_data_roles.id id])
                  (sql/format))]
    (first (jdbc/query (rdbms/get-ds) query))))

(defn- prepare-meta-datum [meta-datum]
  (merge (select-keys meta-datum [:id :meta_key_id :type])
         {:value (let [meta-datum-type (:type meta-datum)]
                   (case meta-datum-type
                     "MetaDatum::JSON" (:json meta-datum)
                     "MetaDatum::Text" (:string meta-datum)
                     "MetaDatum::TextDate" (:string meta-datum)
                     (map #(select-keys % [:id])
                          ((case meta-datum-type
                             "MetaDatum::Groups" groups-with-ids
                             "MetaDatum::Keywords" keywords/get-index
                             "MetaDatum::People" get-people-index
                             "MetaDatum::Roles" find-meta-data-roles)
                           meta-datum))))}
         (->> (select-keys meta-datum [:media_entry_id :collection_id])
              (filter (fn [[k v]] v))
              (into {}))))

(defn- prepare-meta-datum-role
  [id]
  (let [meta-datum (find-meta-datum-role id)]
    (select-keys meta-datum [:id :meta_datum_id :person_id :role_id :position])))

(defn get-meta-datum [request]
  (let [meta-datum (:meta-datum request)]
    {:body (prepare-meta-datum meta-datum)}))

(defn get-meta-datum-data-stream [request]
  (let [meta-datum (:meta-datum request)
        content-type (case (-> request :meta-datum :type)
                       "MetaDatum::JSON" "application/json; charset=utf-8"
                       "text/plain; charset=utf-8")
        value (-> meta-datum prepare-meta-datum :value)]
    (cond
      (nil? value) {:status 422}
      (str value) (-> {:body value}
                      (ring-response/header "Content-Type" content-type))
      :else {:body value})))

(defn get-meta-datum-role
  [request]
  (let [meta-datum-role-id (-> request :params :meta_datum_id)]
    {:body (prepare-meta-datum-role meta-datum-role-id)}))

;### Debug ####################################################################
;(debug/debug-ns *ns*)
