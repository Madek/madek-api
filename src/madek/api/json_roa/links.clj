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

(defn media-entries-path
  ([prefix]
   (media-entries-path prefix {}))
  ([prefix query-params]
   (str prefix "/media-entries/"
        (if (empty? query-params)
          (str "{?"
               "order,"
               "public_get_metadata_and_previews,"
               "public_get_full_size,"
               "me_get_metadata_and_previews,"
               "me_get_full_size,"
               "filter_by,"
               "collection_id}")
          (str "?" (http-client/generate-query-string query-params))))))

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

(defn collection
  ([prefix]
   (collection prefix "{id}"))
  ([prefix id]
   {:name "Collection"
    :href (str prefix "/collections/" id)
    :relations {} }))

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
    :relations {} }))

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

;### next link #################################################################

(defn next-link [url-path query-params]
  {:next {:href (str url-path "?"
                     (http-client/generate-query-string
                       (pagination/next-page-query-query-params
                         query-params)))}})

(defn next-rel [link-builder query-params]
  {:next {:href
          (link-builder (pagination/next-page-query-query-params
                          query-params))}})


;### Debug ####################################################################
;(logging-config/set-logger! :level :debug)
;(logging-config/set-logger! :level :info)
;(debug/debug-ns *ns*)

