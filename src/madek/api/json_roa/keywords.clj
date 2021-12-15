(ns madek.api.json-roa.keywords
  (:require
    [clojure.tools.logging :as logging]
    [logbug.debug :as debug]
    [madek.api.json-roa.links :as links]
    [madek.api.pagination :as pagination]
    ))

(defn keyword-term [request response]
  (let [context (:context request)
        params (:params request)]
    {:name "Keyword"
     :relations
     {:meta-key (links/meta-key
                  context (-> response :body :meta_key_id))
      :root (links/root context)}}))

;### Debug ####################################################################
;(debug/debug-ns *ns*)
