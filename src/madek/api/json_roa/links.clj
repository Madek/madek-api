(ns madek.api.json-roa.links
  (:require
   [clj-http.client :as http-client]
   [clojure.tools.logging :as logging]
   [logbug.debug :as debug]
   [madek.api.json-roa.collection-media-entry-arcs.links :as collection-media-entry-arcs.links]
   [madek.api.pagination :as pagination]
   [ring.util.codec :refer [form-encode]]
   [uritemplate-clj.core :refer [uritemplate]]))

;### helpers ######################################################################

(defn template-params-str [template-keys query-params]
  "Computes the string of available template-params by removing those
  already used in the query-params. The Madek-API does not use duplicate
  keys in the query params. "
  (->> (clojure.set/difference
        template-keys
        (->> query-params
             keys
             (map keyword)))
       (map name)
       (clojure.string/join ",")))

;### root #########################################################################

(defn root
  ([prefix]
   {:name "Root"
    :href (str prefix "/")
    :relations {:api-docs {:name "API-Doc Root"
                           :href "/api/docs/resources/root.html#root"}}}))

;### auth-info ####################################################################

(defn auth-info [prefix]
  {:name "Authentication-Info"
   :href (str prefix "/auth-info")})

;### groups #######################################################################

(defn groups-path [prefix query-params]
  (str prefix "/groups/?" (http-client/generate-query-string query-params)))

(defn group
  ([prefix]
   (group prefix "{id}"))
  ([prefix id]
   {:href (str prefix "/groups/" id)
    :name "Group"
    :methods {:delete {}
              :get {}
              :patch {}}
    :relations {:api-docs {:name "API-Doc Group"
                           :href "/api/docs/resources/group.html#group"}}}))

(defn groups
  ([prefix]
   (groups prefix {}))
  ([prefix query-params]
   {:href (groups-path prefix query-params)
    :name "Groups"
    :methods {:get {}
              :post {}}
    :relations {:api-docs {:name "API-Doc Group"
                           :href "/api/docs/resources/group.html#group"}}}))

;### groups-users #################################################################

(defn group-user
  ([prefix & {:keys [group-id user-id]
              :or {group-id "{group_id}"
                   user-id "{user_id}"}}]
   {:href (str prefix "/groups/" group-id "/users/" user-id)
    :name "Group-User"
    :methods {:delete {}
              :get {}
              :put {}}
    :relations {:api-docs {:name "API-Doc Group"
                           :href "/api/docs/resources/group-users.html"}}}))

(defn group-users-path [prefix query-params]
  (str prefix "/groups/{group_id}/users/?"
       (http-client/generate-query-string query-params)))

(defn group-users
  ([prefix & {:keys [group-id]
              :or {group-id "{group_id}"}}]
   {:href (str prefix "/groups/" group-id "/users/")
    :name "Group-Users"
    :methods {:get {}
              :put {}}
    :relations {:api-docs {:name "API-Doc Group-Users"
                           :href "/api/docs/resources/group-users.html"}}}))

;### meta data ####################################################################

(defn collection-meta-data
  ([prefix]
   (collection-meta-data prefix "{id}"))
  ([prefix id]
   {:name "MetaData"
    :href (str prefix "/collections/" id "/meta-data/"
               "{?meta_keys}")
    :relations {:api-docs {:name "API-Doc Meta-Data"
                           :href "/api/docs/resources/meta-data.html#meta-data"}}}))

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
                           :href "/api/docs/resources/meta-datum.html#meta-datum"}}}))

(defn meta-datum-data-stream
  ([prefix]
   (meta-datum-data-stream prefix {:id "{id}"}))
  ([prefix meta-datum]
   {:href (str prefix "/meta-data/" (:id meta-datum) "/data-stream")
    :name (str "Meta-Datum-Data-Stream"
               (when-let [meta-key-id (:meta_key_id meta-datum)]
                 (str " of " meta-key-id)))
    :relations {:api-docs {:name "API-Doc Meta-Datum-Data-Stream"
                           :href "/api/docs/resources/meta-datum.html#meta-datum-data-stream"}}}))

(defn meta-datum-role
  ([prefix]
   (meta-datum-role prefix "{id}"))
  ([prefix id]
   {:name "MetaDatum::Role"
    :href (str prefix "/meta-data-roles/" id)
    :relations {:api-docs {:name "API-Doc MetaDatum::Role"
                           :href "/api/docs/resources/meta-datum.html#meta-datum"}}}))

;### media-entries ################################################################

(defn media-entries-path-base
  ([prefix] (str prefix "/media-entries/")))

(def media-entries-path-query-template-keys
  #{:collection_id
    :filter_by
    :me_get_full_size
    :me_get_full_size_dedicated
    :me_get_metadata_and_previews
    :me_get_metadata_and_previews_dedicated
    :order
    :public_get_full_size
    :public_get_metadata_and_previews})

(defn media-entries-path
  ([prefix]
   (media-entries-path prefix {}))
  ([prefix query-params]
   (let [template-params (template-params-str
                          media-entries-path-query-template-keys
                          query-params)]
     (str (media-entries-path-base prefix)
          (if (empty? query-params)
            (str "{?" template-params "}")
            (str "?" (http-client/generate-query-string query-params)
                 "{&" template-params "}"))))))

(defn media-entries
  ([prefix]
   (media-entries prefix {}))
  ([prefix query-params]
   {:name "Media-Entries"
    :href (media-entries-path prefix query-params)
    :relations {:api-docs {:name "API-Doc Media-Entries"
                           :href "/api/docs/resources/media-entries.html#media-entries"}}}))

(defn media-entry
  ([prefix]
   (media-entry prefix "{id}"))
  ([prefix id]
   {:name "Media-Entry"
    :href (str prefix "/media-entries/" id)
    :relations {:api-docs {:name "API-Doc Media-Entry"
                           :href "/api/docs/resources/media-entry.html#media-entry"}}}))

;### collections ##################################################################

(defn collections-path-base
  ([prefix] (str prefix "/collections/")))

(def collections-path-query-template-keys
  #{:collection_id
    :me_get_metadata_and_previews
    :me_get_metadata_and_previews_dedicated
    :order
    :public_get_metadata_and_previews})

(defn collections-path
  ([prefix]
   (collections-path prefix {}))
  ([prefix query-params]
   (let [template-params (template-params-str
                          collections-path-query-template-keys
                          query-params)]
     (str (collections-path-base prefix)
          (if (empty? query-params)
            (str "{?" template-params "}")
            (str "?" (http-client/generate-query-string query-params)
                 "{&" template-params "}"))))))

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
                           :href "/api/docs/resources/collection.html#collection"}}}))

;### collection-media-entry­arcs ##################################################

(defn collection-media-entry-arcs [& args]
  (apply collection-media-entry-arcs.links/collection-media-entry-arcs args))

;###############################################################################

(defn media-entry-meta-data
  ([prefix]
   (media-entry-meta-data prefix "{id}"))
  ([prefix id]
   {:name "MetaData"
    :href (str prefix "/media-entries/" id "/meta-data/"
               "{?meta_keys}")
    :relations {:api-docs {:name "API-Doc Meta-Data"
                           :href "/api/docs/resources/meta-data.html#meta-data"}}}))

;### media-file(s) #############################################################

(defn media-file
  ([prefix]
   (media-file prefix "{id}"))
  ([prefix id]
   {:name "Media-File"
    :href (str prefix "/media-files/" id)
    :relations {}}))

(defn media-file-data-stream
  ([prefix]
   (media-file-data-stream prefix "{id}"))
  ([prefix id]
   {:name "Media-File"
    :href (str prefix "/media-files/" id "/data-stream")
    :relations {}}))

;### preview(s) ################################################################

(defn preview
  ([prefix]
   (preview prefix "{id}"))
  ([prefix id]
   {:name "Preview"
    :href (str prefix "/previews/" id)
    :relations {}}))

(defn preview-file-data-stream
  ([prefix]
   (preview-file-data-stream prefix "{id}"))
  ([prefix id]
   {:name "Preview-File"
    :href (str prefix "/previews/" id "/data-stream")
    :relations {}}))

;### meta-key(s) ###############################################################

(defn meta-key
  ([prefix]
   (meta-key prefix "{id}"))
  ([prefix id]
   {:name "Meta-Key"
    :href (str prefix "/meta-keys/" id)
    :relations {:api-docs {:name "API-Doc Meta-Key"
                           :href "/api/docs/resources/meta-key.html#meta-key"}}}))

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
    :relations {:api-docs {:name "API-Doc Meta-Keys"
                           :href "/api/docs/resources/meta-keys.html#meta-keys"}}}))

;### people and person #########################################################

(defn people-path [prefix query-params]
  (str prefix "/people/?" (http-client/generate-query-string query-params)))

(defn person
  ([prefix]
   (person prefix "{id}"))
  ([prefix id]
   {:href (str prefix "/people/" id)
    :name "Person"
    :methods {:delete {}
              :get {}
              :patch {}}
    :relations {:api-docs {:name "API-Doc Person"
                           :href "/api/docs/resources/person.html#person"}}}))

(defn people
  ([prefix]
   (people prefix {}))
  ([prefix query-params]
   {:href (people-path prefix query-params)
    :name "Groups"
    :methods {:get {}
              :post {}}
    :relations {:api-docs {:name "API-Doc Group"
                           :href "/api/docs/resources/person.html#person"}}}))

;### users #######################################################################

(defn users-path [prefix query-params]
  (str prefix "/users/?" (http-client/generate-query-string query-params)))

(defn user
  ([prefix]
   (user prefix "{id}"))
  ([prefix id]
   {:href (str prefix "/users/" id)
    :name "User"
    :methods {:delete {}
              :get {}
              :patch {}}
    :relations {:api-docs {:name "API-Doc User"
                           :href "/api/docs/resources/user.html#user"}}}))

(defn users
  ([prefix]
   (users prefix {}))
  ([prefix query-params]
   {:href (users-path prefix query-params)
    :name "Users"
    :methods {:get {}
              :post {}}
    :relations {:api-docs {:name "API-Doc Users"
                           :href "/api/docs/resources/users.html#users"}}}))

;### keyword(s) ################################################################

(defn keyword-term
  ([prefix]
   (keyword-term prefix "{id}"))
  ([prefix id]
   {:name "Keyword"
    :href (str prefix "/keywords/" id)
    :relations {}}))

;### vocabulary(s) ###############################################################

(defn vocabulary
  ([prefix]
   (vocabulary prefix "{id}"))
  ([prefix id]
   {:name "Vocabulary"
    :href (str prefix "/vocabularies/" id)
    :relations {:api-docs {:name "API-Doc Vocabulary"
                           :href "/api/docs/resources/vocabulary.html#vocabulary"}}}))

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
    :relations {:api-docs {:name "API-Doc Vocabularies"
                           :href "/api/docs/resources/vocabularies.html#vocabularies"}}}))

;### role(s) ################################################################

(defn roles-path [prefix query-params]
  (str prefix "/roles/?" (http-client/generate-query-string query-params)))

(defn role
  ([prefix]
   (role prefix "{id}"))
  ([prefix id]
   {:name "Role"
    :href (str prefix "/roles/" id)
    :relations {:api-docs {:name "API-Doc Role"
                           :href "/api/docs/resources/role.html#role"}}}))

(defn roles
  ([prefix]
   (roles prefix {}))
  ([prefix query-params]
   {:name "Roles"
    :href (roles-path prefix query-params)
    :relations {:api-docs {:name "API-Doc Roles"
                           :href "/api/docs/resources/roles.html#roles"}}}))

;### next link #################################################################

(defn next-link
  "Computes the next-link for a paginated collection by modifying the
  query-params and passing on the result. `url-fn` must be a function of two
  arguments: the context and the query-params. `url-fn` may return a templated
  URL. The result will be piped through uritemplate and the final result will be
  a non templated URL in accordance with the JSON-ROA specification."
  [url-fn context query-params]
  (let [qp (pagination/next-page-query-query-params query-params)]
    {:next {:href (uritemplate (apply url-fn [context qp]) qp)}}))

;### Debug ####################################################################
;(debug/debug-ns *ns*)
;(debug/wrap-with-log-debug #'next-link)
;(debug/wrap-with-log-debug #'media-entries-path)
