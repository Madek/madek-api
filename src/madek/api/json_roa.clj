(ns madek.api.json-roa
  (:require
    [logbug.debug :as debug]
    [clj-logging-config.log4j :as logging-config]
    [clojure.tools.logging :as logging]
    [compojure.core :as cpj]
    [madek.api.json-roa.root :as root]
    [madek.api.json-roa.media-entries :as media-entries]
    [madek.api.json-roa.media-files :as media-files]
    [madek.api.json-roa.meta-data :as meta-data]
    [madek.api.json-roa.meta-keys :as meta-keys]
    [madek.api.json-roa.people :as people]
    ))

(def about
  {:name "JSON-ROA"
   :description "A JSON extension for resource and relation oriented architectures providing explorable APIs for humans and machines."
   :relations{:json-roa_homepage
              {:ref "http://json-roa.github.io/"
               :name "JSON-ROA Homepage"}}})


(defn amend-json-roa [request json-roa-data]
  (-> {}
      (assoc-in [:_json-roa] json-roa-data)
      (assoc-in [:_json-roa :about_json-roa] about)
      (assoc-in [:_json-roa :json-roa_version] "1.0.0")
      ))

(defn build-routes-handler [json-response]
  (cpj/routes
    (cpj/GET "/" _ root/build)
    (cpj/GET "/media-entries/" request (media-entries/index request json-response))
    (cpj/GET "/media-entries/:id" request (media-entries/media-entry request json-response))
    (cpj/GET "/media-entries/:id/meta-data/" request (meta-data/index request json-response))
    (cpj/GET "/media-files/:id" request (media-files/media-file request json-response))
    (cpj/GET "/meta-data/:id" request (meta-data/meta-datum request json-response))
    (cpj/GET "/meta-keys/:id" request (meta-keys/meta-key request json-response))
    (cpj/GET "/people/:id" request (people/person request json-response))
    ))

(defn handler [request response]
  (let [body (:body response)]
    (if-not (and body (map? body))
      response
      (let [json-roa-handler (build-routes-handler response)
            json-roa-data (select-keys (json-roa-handler request) [:self-relation :relations :collection :name])
            amended-json-roa-data (amend-json-roa request json-roa-data)]
        (update-in response
                   [:body]
                   (fn [original-body json-road-data]
                     (into {} (sort (conj {} original-body json-road-data))))
                   amended-json-roa-data)))))


;### Debug ####################################################################
;(logging-config/set-logger! :level :debug)
;(logging-config/set-logger! :level :info)
;(debug/debug-ns *ns*)
