(ns madek.api.json-roa.links

  (:require
    [clj-http.client :as http-client]
    [logbug.debug :as debug]
    [clj-logging-config.log4j :as logging-config]
    [clojure.tools.logging :as logging]
    [ring.util.codec :refer [form-encode]]
    [madek.api.pagination :as pagination]
    ))

(defn root
  ([prefix]
   {:name "Root"
    :href (str prefix "/")
    :relations {:api-docs {:name "API-Doc Root"
                           :href "/api/docs/resources/root.html#root"
                           }}}))


;### auth-info ####################################################################

(defn auth-info [prefix]
  {:name "Authentication-Info"
   :href (str prefix "/auth-info")
   })


;### meta data ####################################################################

(defn collection-meta-data
  ([prefix]
   (collection-meta-data prefix "{id}"))
  ([prefix id]
   {:name "MetaData"
    :href (str prefix "/collections/" id "/meta-data/"
               "{?meta_keys}")
    :relations {:api-docs {:name "API-Doc Meta-Data"
                           :href "/api/docs/resources/meta-data.html#meta-data"
                           }}}))


;### meta data ####################################################################

(defn meta-datum
  ([prefix]
   (meta-datum prefix {:id "{id}"}))
  ([prefix meta-datum]
   {:href (str prefix "/meta-data/" (:id meta-datum))
    :name (str "Meta-Datum"
               (when-let [meta-key-id (:meta_key_id meta-datum)]
                 (str " of " meta-key-id)))
    :relations {:api-docs {:name "API-Doc Meta-Datum"
                           :href "/api/docs/resources/meta-datum.html#meta-datum"
                           }}}))


;### media-entries ################################################################

(defn media-entries-path-base
  ([prefix] (str prefix "/media-entries/")))

(defn media-entries-path
  ([prefix]
   (media-entries-path prefix {}))
  ([prefix query-params]
   (str (media-entries-path-base prefix)
        (let [template-params (str "order,"
                                   "public_get_metadata_and_previews,"
                                   "public_get_full_size,"
                                   "me_get_metadata_and_previews,"
                                   "me_get_full_size,"
                                   "filter_by,"
                                   "collection_id}")]
          (if (empty? query-params)
            (str "{?" template-params)
            (str "?"
                 (http-client/generate-query-string query-params)
                 "{&"
                 template-params))))))

(defn media-entries
  ([prefix ]
   (media-entries prefix {}))
  ([prefix query-params]
   {:name "Media-Entries"
    :href (media-entries-path prefix query-params)
    :relations {:api-docs {:name "API-Doc Media-Entries"
                           :href "/api/docs/resources/media-entries.html#media-entries"
                           }}}))

(defn media-entry
  ([prefix]
   (media-entry prefix "{id}"))
  ([prefix id]
   {:name "Media-Entry"
    :href (str prefix "/media-entries/" id)
    :relations {:api-docs {:name "API-Doc Media-Entry"
                           :href "/api/docs/resources/media-entry.html#media-entry"
                           }}}))

;### collections ##################################################################

(defn collections-path-base
  ([prefix] (str prefix "/collections/")))

(defn collections-path
  ([prefix]
   (collections-path prefix {}))
  ([prefix query-params]
   (str (collections-path-base prefix)
        (let [template-params (str "order,"
                                   "public_get_metadata_and_previews,"
                                   "me_get_metadata_and_previews,"
                                   "collection_id}")]
          (if (empty? query-params)
            (str "{?" template-params)
            (str "?"
                 (http-client/generate-query-string query-params)
                 "{&"
                 template-params))))))

(defn collections
  ([prefix]
   (collections prefix {}))
  ([prefix query-params]
   {:name "Collections"
    :href (collections-path prefix query-params)
    :relations {:api-docs {:name "API-Doc Collections"
                           :href "/api/docs/resources/collections.html#collections"}}}))

(defn collection
  ([prefix]
   (collection prefix "{id}"))
  ([prefix id]
   {:name "Collection"
    :href (str prefix "/collections/" id)
    :relations {:api-docs {:name "API-Doc Collection"
                           :href "/api/docs/resources/collection.html#collection"
                           }}}))

;### filter-sets ##################################################################

(defn filter-sets-path-base
  ([prefix] (str prefix "/filter-sets/")))

(defn filter-sets-path
  ([prefix]
   (filter-sets-path prefix {}))
  ([prefix query-params]
   (str (filter-sets-path-base prefix)
        (let [template-params (str "order,"
                                   "public_get_metadata_and_previews,"
                                   "me_get_metadata_and_previews,"
                                   "collection_id}")]
          (if (empty? query-params)
            (str "{?" template-params)
            (str "?"
                 (http-client/generate-query-string query-params)
                 "{&"
                 template-params))))))

(defn filter-sets
  ([prefix]
   (filter-sets prefix {}))
  ([prefix query-params]
   {:name "FilterSets"
    :href (filter-sets-path prefix query-params)
    :relations {:api-docs {:name "API-Doc FilterSets"
                           :href "/api/docs/resources/filter-sets.html#filter-sets"}}}))

(defn filter-set
  ([prefix]
   (filter-set prefix "{id}"))
  ([prefix id]
   {:name "FilterSet"
    :href (str prefix "/filter-sets/" id)
    :relations {:api-docs {:name "API-Doc FilterSet"
                           :href "/api/docs/resources/filter-set.html#filter-set"
                           }}}))

;###############################################################################

(defn media-entry-meta-data
  ([prefix]
   (media-entry-meta-data prefix "{id}"))
  ([prefix id]
   {:name "MetaData"
    :href (str prefix "/media-entries/" id "/meta-data/"
               "{?meta_keys}")
    :relations {:api-docs {:name "API-Doc Meta-Data"
                           :href "/api/docs/resources/meta-data.html#meta-data"
                           }}
    }))


;### media-file(s) #############################################################

(defn media-file
  ([prefix]
   (media-file prefix "{id}"))
  ([prefix id]
   {:name "Media-File"
    :href (str prefix "/media-files/" id)
    :relations {} }))

(defn media-file-data-stream
  ([prefix]
   (media-file-data-stream prefix "{id}"))
  ([prefix id]
   {:name "Media-File"
    :href (str prefix "/media-files/" id "/data-stream")
    :relations {} }))


;### preview(s) ################################################################

(defn preview
  ([prefix]
   (preview prefix "{id}"))
  ([prefix id]
   {:name "Preview"
    :href (str prefix "/previews/" id)
    :relations {} }))

(defn preview-file-data-stream
  ([prefix]
   (preview-file-data-stream prefix "{id}"))
  ([prefix id]
   {:name "Preview-File"
    :href (str prefix "/previews/" id "/data-stream")
    :relations {} }))


;### meta-key(s) ###############################################################

(defn meta-key
  ([prefix]
   (meta-key prefix "{id}"))
  ([prefix id]
   {:name "Meta-Key"
    :href (str prefix "/meta-keys/" id)
    :relations {:api-docs {:name "API-Doc Root"
                           :href "/api/docs/resources/meta-key.html#meta-key"
                           }}}))

(defn meta-keys-path
  ([prefix]
   (meta-keys-path prefix {}))
  ([prefix query-params]
   (str prefix "/meta-keys/"
        (if (empty? query-params)
          (str "{?vocabulary}")
          (str "?" (http-client/generate-query-string query-params))))))

(defn meta-keys
  ([prefix]
   (meta-keys prefix {}))
  ([prefix query-params]
   {:name "Meta-Keys"
    :href (meta-keys-path prefix query-params)
    :relations {:api-docs {:name "API-Doc Root"
                           :href "/api/docs/resources/meta-keys.html#meta-keys"
                           }}}))


;### person(s) #################################################################

(defn person
  ([prefix]
   (person prefix "{id}"))
  ([prefix id]
   {:name "Person"
    :href (str prefix "/people/" id)
    :relations {} }))


;### keyword(s) ################################################################

(defn keyword-term
  ([prefix]
   (keyword-term prefix "{id}"))
  ([prefix id]
   {:name "Keyword"
    :href (str prefix "/keywords/" id)
    :relations {} }))


;### license(s) ################################################################

(defn license
  ([prefix]
   (license prefix "{id}"))
  ([prefix id]
   {:name "License"
    :href (str prefix "/licenses/" id)
    :relations {} }))


;### vocabulary(s) ###############################################################

(defn vocabulary
  ([prefix]
   (vocabulary prefix "{id}"))
  ([prefix id]
   {:name "Vocabulary"
    :href (str prefix "/vocabularies/" id)
    :relations {:api-docs {:name "API-Doc Root"
                           :href "/api/docs/resources/vocabulary.html#vocabulary"
                           }}}))

(defn vocabularies-path
  ([prefix]
   (vocabularies-path prefix {}))
  ([prefix query-params]
   (str prefix "/vocabularies/"
        (if (empty? query-params)
          (str "")
          (str "?" (http-client/generate-query-string query-params))))))

(defn vocabularies
  ([prefix]
   (vocabularies prefix {}))
  ([prefix query-params]
   {:name "Vocabularies"
    :href (vocabularies-path prefix query-params)
    :relations {:api-docs {:name "API-Doc Root"
                           :href "/api/docs/resources/vocabularies.html#vocabularies"
                           }}}))



;### next link #################################################################

(defn next-link [url-path query-params]
  {:next {:href (str url-path "?"
                     (http-client/generate-query-string
                       (pagination/next-page-query-query-params
                         query-params)))}})

;### Debug ####################################################################
;(logging-config/set-logger! :level :debug)
;(logging-config/set-logger! :level :info)
;(debug/debug-ns *ns*)
