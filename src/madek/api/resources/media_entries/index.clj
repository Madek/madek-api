(ns madek.api.resources.media-entries.index
  (:refer-clojure :exclude [str keyword])
  (:require [madek.api.utils.core :refer [str keyword]])
  (:require
    [madek.api.pagination :as pagination]
    [madek.api.resources.media-entries.advanced-filter :as advanced-filter]
    [madek.api.resources.media-entries.advanced-filter.permissions :as permissions :refer [filter-by-query-params]]
    [madek.api.resources.shared :as shared]
    [madek.api.utils.rdbms :as rdbms]
    [madek.api.utils.sql :as sql]

    [clojure.core.match :refer [match]]
    [clojure.set :refer [rename-keys]]
    [clj-logging-config.log4j :as logging-config]
    [clojure.java.jdbc :as jdbc]
    [clojure.tools.logging :as logging]
    [compojure.core :as cpj]
    [logbug.catcher :as catcher]
    [logbug.debug :as debug :refer [I> I>> identity-with-logging]]
    )


  (:import
    [madek.api WebstackException]
    )
  )

;### collection_id ############################################################

(defn- filter-by-collection-id [sqlmap {:keys [collection_id] :as query-params}]
  (if-not collection_id
    sqlmap
    (-> sqlmap
        (sql/merge-join [:collection_media_entry_arcs :arcs]
                        [:= :arcs.media_entry_id :media_entries.id])
        (sql/merge-where [:= :arcs.collection_id collection_id])
        (sql/merge-select 
          [:arcs.created_at :arc_created_at]
          [:arcs.order :arc_order]
          [:arcs.created_at :arc_created_at]
          [:arcs.updated_at :arc_updated_at]
          [:arcs.id :arc_id]))))


;### query ####################################################################

(def ^:private base-query
  (-> (sql/select [:media_entries.id :media_entry_id] 
                  [:media_entries.created_at :media_entry_created_at])
      (sql/from :media_entries)))

(defn- order-by-media-entry-attribute [query [attribute order]]
  (let [order-by-arg (match [(keyword attribute) (keyword order)]
                            [:created_at :desc] [:media-entries.created_at :desc :nulls-last]
                            [:created_at _] [:media-entries.created_at])]
    (sql/merge-order-by query order-by-arg)))

(defn- order-by-arc-attribute [query [attribute order]]
  (let [order-by-arg (match [(keyword attribute) (keyword order)]
                            [:order :desc] [:arcs.order :desc :nulls-last]
                            [:order _] [:arcs.order]
                            [:created_at :desc] [:arcs.created_at :desc :nulls-last]
                            [:created_at _] [:arcs.created_at])]
    (sql/merge-order-by query order-by-arg)))


(defn- order-by-meta-datum-text [query [meta-key-id order]]
  (let [from-name (-> meta-key-id
                      (clojure.string/replace #"\W+" "_")
                      clojure.string/lower-case 
                      (#(str "meta-data-" %)))]
    (-> query
        (sql/merge-left-join [:meta_data from-name] 
                             [:= (keyword (str from-name".meta_key_id")) meta-key-id])
        (sql/merge-order-by [(-> from-name (str ".string") keyword) 
                             (case (keyword order) 
                               :asc :asc
                               :desc :desc
                               :asc) 
                             :nulls-last])
        (sql/merge-where [:= (keyword (str from-name".media_entry_id")) :media_entries.id]))))

(defn- order-reducer [query [scope & more]]
  (case scope
    "media_entry" (order-by-media-entry-attribute query more)
    "arc" (order-by-arc-attribute query more)
    "MetaDatum::Text" (order-by-meta-datum-text query more)))

(defn- set-order [query query-params]
  (-> (let [order (-> query-params :order)]
        (cond 
          (nil? order) (sql/order-by query [:media_entries.created-at :asc])
          (string? order) (if (#{"desc" "asc"} order)
                            (sql/order-by query [:media_entries.created-at (keyword order)]) 
                            (throw (ex-info "only desc or asc is allowed for simple sting values of order" 
                                            {:status 422})))
          (seq? order)(reduce order-reducer query order)
          :else (sql/order-by query [:media_entries.created-at :asc])))
      (sql/merge-order-by :media_entries.id)))

(defn- build-query [request]
  (let [query-params (:query-params request)
        authenticated-entity (:authenticated-entity request)]
    (I> identity-with-logging
        base-query
        (set-order query-params)
        (filter-by-collection-id query-params)
        (permissions/filter-by-query-params query-params
                                            authenticated-entity)
        (advanced-filter/filter-by (:filter_by query-params))
        (pagination/add-offset-for-honeysql query-params)
        sql/format)))

(defn- query-index-resources [request]
  (jdbc/query (rdbms/get-ds) (build-query request)))


;### index ####################################################################

(defn get-index [{{collection-id :collection_id} :query-params :as request}]
  (catcher/with-logging {}
    (let [data (query-index-resources request)]
      {:body
       (merge 
         {:media-entries (->> data 
                              (map #(select-keys % [:media_entry_id :media_entry_created_at]))
                              (map #(rename-keys % {:media_entry_id :id 
                                                    :media_entry_created_at :created_at})))}
         (when collection-id
           {:arcs (->> data
                       (map #(select-keys % [:arc_id :media_entry_id :arc_order :arc_created_at :arc_updated_at]))
                       (map #(rename-keys % {:arc_id :id
                                             :arc_order :order 
                                             :arc_created_at :created_at
                                             :arc_updated_at :updated_at})))}))})))

;### Debug ####################################################################
;(logging-config/set-logger! :level :debug)
;(logging-config/set-logger! :level :info)
;(debug/debug-ns *ns*)
;(debug/wrap-with-log-debug #'filter-by-permissions)
;(debug/wrap-with-log-debug #'set-order)
