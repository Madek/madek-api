(ns madek.api.resources.meta-data.meta-datum
  (:require
   [clojure.java.jdbc :as jdbc]
   [clojure.tools.logging :as logging]
   [compojure.core :as cpj]
   [logbug.catcher :as catcher]
   [logbug.debug :as debug]
   [madek.api.authorization :as authorization]
   [madek.api.pagination :as pagination]
   [madek.api.resources.shared :as shared]
   [madek.api.utils.rdbms :as rdbms :refer [get-ds]]
   [madek.api.utils.sql :as sql]
   [ring.util.response :as ring-response]
   [taoensso.timbre :refer [debug info warn error spy]]))

;### people ###################################################################

(defn groups-with-ids [meta-datum]
  [])

(defn get-people-index [meta-datum]
  (let [query (-> (sql/select :people.*)
                  (sql/from :people)
                  (sql/merge-join
                   :meta_data_people
                   [:= :meta_data_people.person_id :people.id])
                  (sql/merge-select [:meta_data_people.position :position])
                  (sql/merge-where
                   [:= :meta_data_people.meta_datum_id (:id meta-datum)])
                  (sql/order-by [:meta_data_people.position :asc]
                                [:people.last_name :asc]
                                [:people.first_name :asc]
                                [:people.id :asc])
                  (sql/format))]
    (jdbc/query (rdbms/get-ds) query)))

;### keywords #################################################################

(defn keywords [{meta_datum_id :id :as meta-datum}]
  (let [meta_key (-> (sql/select :meta_keys.keywords_alphabetical_order)
                     (sql/from :meta_keys)
                     (sql/merge-join :meta_data
                                     [:= :meta_data.meta_key_id :meta_keys.id])
                     (sql/merge-where [:= :meta_data.id meta_datum_id])
                     (sql/format)
                     (->> (jdbc/query (rdbms/get-ds)) first))

        base-query (-> (sql/select :keywords.id :keywords.term)
                       (sql/from :keywords)
                       (sql/merge-join :meta_data_keywords
                                       [:= :meta_data_keywords.keyword_id :keywords.id])
                       (sql/merge-select [:meta_data_keywords.position :position])
                       (sql/merge-where [:= :meta_data_keywords.meta_datum_id meta_datum_id]))
        query (if (:keywords_alphabetical_order meta_key)
                (-> base-query (sql/order-by [:keywords.term :asc]
                                             [:meta_data_keywords.position :asc]))
                (-> base-query (sql/order-by
                                [:meta_data_keywords.position :asc]
                                [:keywords.term :asc])))]
    (-> query
        (sql/format)
        (->> (jdbc/query (rdbms/get-ds))))))

;### meta-datum ###############################################################

(defn find-meta-data-people
  [meta-datum]
  (let [query (-> (sql/select :meta_data_people.*)
                  (sql/from :meta_data_people)
                  (sql/merge-where
                   [:= :meta_data_people.meta_datum_id (:id meta-datum)])
                  (sql/order-by [:meta_data_people.position :asc]
                                [:meta_data_people.id :asc])
                  (sql/format))]
    (jdbc/query (rdbms/get-ds) query)))

(defn- find-meta-datum-person
  [id]
  (let [query (-> (sql/select :meta_data_people.*)
                  (sql/from :meta_data_people)
                  (sql/merge-where
                   [:= :meta_data_people.id id])
                  (sql/format))]
    (first (jdbc/query (rdbms/get-ds) query))))

(defn- prepare-meta-datum [meta-datum]
  (merge (select-keys meta-datum [:id :meta_key_id :type])
         {:value (let [meta-datum-type (:type meta-datum)]
                   (case meta-datum-type
                     "MetaDatum::JSON" (:json meta-datum)
                     "MetaDatum::Text" (:string meta-datum)
                     "MetaDatum::TextDate" (:string meta-datum)
                     (map #(select-keys % [:id :position])
                          ((case meta-datum-type
                             "MetaDatum::Groups" groups-with-ids
                             "MetaDatum::Keywords" keywords
                             "MetaDatum::People" find-meta-data-people)
                           meta-datum))))}
         (->> (select-keys meta-datum [:media_entry_id :collection_id])
              (filter (fn [[k v]] v))
              (into {}))))

(defn- prepare-meta-datum-person
  [id]
  (let [meta-datum (find-meta-datum-person id)]
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

(defn get-meta-datum-person
  [request]
  (let [meta-datum-person-id (-> request :params :meta_datum_id)]
    {:body (prepare-meta-datum-person meta-datum-person-id)}))

;### Debug ####################################################################
;(debug/debug-ns *ns*)
