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
    :relations
    {}}))


;### auth-info ####################################################################

(defn auth-info [prefix]
  {:name "Authentication-Info"
   :href (str prefix "/auth-info")
   })

;### meta data ####################################################################

(defn meta-datum
  ([prefix]
   (meta-datum prefix {:id "{id}"}))
  ([prefix meta-datum]
   {:href (str prefix "/meta-data/" (:id meta-datum))
    :name (str "Meta-Datum"
               (when-let [meta-key-id (:meta_key_id meta-datum)]
                 (str " of " meta-key-id)))}))


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
               "collection_id}")
          (str "?" (http-client/generate-query-string query-params))))))

(defn media-entries
  ([prefix ]
   (media-entries prefix {}))
  ([prefix query-params]
   {:name "Media-Entries"
    :href (media-entries-path prefix query-params)
    :relations {} }))

(defn media-entry
  ([prefix]
   (media-entry prefix "{id}"))
  ([prefix id]
   {:name "Media-Entry"
    :href (str prefix "/media-entries/" id)
    :relations {} }))

(defn collection
  ([prefix]
   (collection prefix "{id}"))
  ([prefix id]
   {:name "Collection"
    :href (str prefix "/collections/" id)
    :relations {} }))

(defn media-entry-meta-data
  ([prefix]
   (media-entry prefix "{id}"))
  ([prefix id]
   {:name "MetaData"
    :href (str prefix "/media-entries/" id "/meta-data/")
    :relations {} }))


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

