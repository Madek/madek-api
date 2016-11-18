(ns madek.api.json-roa.collection-media-entry-arcs.links
  (:require
    [clj-http.client :as http-client]
    )
  (:require
    [clojure.tools.logging :as logging]
    [logbug.debug :as debug]
    [clj-logging-config.log4j :as logging-config]
    ))

(defn collection-media-entry-arcs-path-base
  ([prefix] (str prefix "/collection-media-entry-arcs/")))

(def arcs-query-template-params #{"collection_id" "media_entry_id"})

(defn unbound-template-params [template-params query-params]
  (clojure.set/difference
    template-params
    (->> query-params keys (map name) set)))

(defn collection-media-entry-arcs-path
  ([prefix]
   (collection-media-entry-arcs-path-base prefix {}))
  ([prefix query-params]
   (str (collection-media-entry-arcs-path-base prefix)
        "?" (http-client/generate-query-string query-params)
        (when-let [utp (seq (unbound-template-params
                              arcs-query-template-params
                              query-params))]
          (str "{&" (clojure.string/join "," utp) "}")))))

(defn collection-media-entry-arcs
  ([prefix]
   (collection-media-entry-arcs prefix {}))
  ([prefix query-params]
   {:name "Collection-Media-Entry-Arcs"
    :href (collection-media-entry-arcs-path prefix query-params)
    :relations
    {:api-docs {:name "API-Doc Collection-Media-Entry-Arcs"
                :href "/api/docs/resources/collection-media-entry-arcs.html"
                }}}))

(defn collection-media-entry-arc
  ([prefix id]
   {:name "Collection-Media-Entry-Arc"
    :href (str (collection-media-entry-arcs-path-base prefix) id)
    :relations
    {:api-docs {:name "API-Doc Collection-Media-Entry-Arc"
                :href "/api/docs/resources/collection-media-entry-arc.html"
                }}}))

;### Debug ####################################################################
;(logging-config/set-logger! :level :debug)
;(logging-config/set-logger! :level :info)
;(debug/debug-ns *ns*)
