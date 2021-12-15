(ns madek.api.json-roa.meta-data
  (:require
    [clojure.tools.logging :as logging]
    [logbug.debug :as debug]
    [madek.api.json-roa.links :as links]
    [madek.api.pagination :as pagination]
    ))

(defn build-meta-datum-collection-item [context idx meta-datum]
  [(format "%07d" idx)
   (links/meta-datum context meta-datum)])

(defn- meta-datum-role?
  [response]
  (let [params (-> response :body)]
    (and (contains? params :meta_datum_id)
         (contains? params :person_id)
         (contains? params :role_id))))

(defn- meta-datum-relations
  [response context]
  {:meta-key (links/meta-key
               context (-> response :body :meta_key_id))
   :media-entry (links/media-entry
                  context (-> response :body :media_entry_id))
   :data-stream (links/meta-datum-data-stream
                  context (-> response :body))})

(defn- meta-datum-role-relations
  [response context]
  (conj {}
    {:meta-datum (links/meta-datum
                 context {:id (-> response :body :meta_datum_id)})
     :person (links/person
             context (-> response :body :person_id))}
    (if (some? (-> response :body :role_id))
      {:role (links/role
             context (-> response :body :role_id))}
      {})))

(defn- relations
  [response context]
  (if (meta-datum-role? response)
    (meta-datum-role-relations response context)
    (meta-datum-relations response context)))

(defn index [request response]
  (let [context (:context request)
        query-params (:query-params request)]
    {:name "Meta-Data"
     :relations (conj {:root (links/root context) }
                      ; slight duplicative structure here; could be avoided
                      ; with dynamic resolution via resolve; but not worth yet,
                      ; maybe when we handle filter-sets too
                      (when-let [id (-> response :body :media_entry_id)]
                        {:media-entry (links/media-entry context id)})
                      (when-let [id (-> response :body :collection_id)]
                        {:collection (links/collection context id)}))
     :collection
     {:relations (merge {}
                        (when-let [meta-data (-> response :body :meta-data)]
                          (->> meta-data
                               (map-indexed (fn [idx itm] (build-meta-datum-collection-item (:context request) idx itm)))
                               (into {}))))}}))

(defn meta-datum [request response]
  (let [context (:context request)
        meta-datum-type (-> response :body :type)]
    (conj {:name "Meta-Datum"
           :relations (conj {:root (links/root context)}
                            (relations response context))}
          (when-not (#{"MetaDatum::JSON" "MetaDatum::Text" "MetaDatum::TextDate"} meta-datum-type)
            {:collection
             {:relations
              (into {}
                    (map #(hash-map % ((case meta-datum-type
                                         "MetaDatum::People" links/person
                                         "MetaDatum::Keywords" links/keyword-term
                                         "MetaDatum::Roles" links/meta-datum-role)
                                       context %))
                         (map :id (-> response :body :value))))}}))))

;### Debug ####################################################################
;(debug/debug-ns *ns*)
