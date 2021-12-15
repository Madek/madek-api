(ns madek.api.json-roa
  (:require
    [clojure.tools.logging :as logging]
    [compojure.core :as cpj]
    [logbug.debug :as debug]
    [madek.api.json-roa.auth-info :as auth-info]
    [madek.api.json-roa.collection-media-entry-arcs.core :as collection-media-entry-arcs]
    [madek.api.json-roa.collections :as collections]
    [madek.api.json-roa.filter-sets :as filter-sets]
    [madek.api.json-roa.group-users :as group-users]
    [madek.api.json-roa.groups :as groups]
    [madek.api.json-roa.keywords :as keywords]
    [madek.api.json-roa.links :as links :refer [root]]
    [madek.api.json-roa.media-entries :as media-entries]
    [madek.api.json-roa.media-files :as media-files]
    [madek.api.json-roa.meta-data :as meta-data]
    [madek.api.json-roa.meta-keys :as meta-keys]
    [madek.api.json-roa.people :as people]
    [madek.api.json-roa.previews :as previews]
    [madek.api.json-roa.roles :as roles]
    [madek.api.json-roa.root :as root]
    [madek.api.json-roa.users :as users]
    [madek.api.json-roa.vocabularies :as vocabularies]
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
      (assoc-in [:_json-roa :version] "1.0.0")
      ))

(defn build-routes-handler [json-response]
  (cpj/routes
    (cpj/GET "/" _ root/build)
    (cpj/GET "/auth-info" request (auth-info/auth-info request))

    (cpj/GET "/collection-media-entry-arcs/" request
             (collection-media-entry-arcs/index request json-response))
    (cpj/GET "/collection-media-entry-arcs/:id" request
             (collection-media-entry-arcs/item request json-response))
    (cpj/GET "/collections/" request (collections/index request json-response))
    (cpj/GET "/collections/:id" request (collections/collection request json-response))

    (cpj/GET "/filter-sets/" request (filter-sets/index request json-response))

    (cpj/GET "/groups/" request (groups/groups request json-response))
    (cpj/GET "/groups/:id" request (groups/group request json-response))
    (cpj/PATCH "/groups/:id" request (groups/group request json-response))
    (cpj/POST "/groups/" request (groups/group request json-response))

    (cpj/GET "/keywords/:id" request (keywords/keyword-term request json-response))

    (cpj/GET "/media-entries/" request (media-entries/index request json-response))
    (cpj/GET "/media-entries/:id" request (media-entries/media-entry request json-response))
    (cpj/GET "/:media_resource_type/:id/meta-data/" request (meta-data/index request json-response))

    (cpj/GET "/media-files/:id" request (media-files/media-file request json-response))
    (cpj/GET "/meta-data/:id" request (meta-data/meta-datum request json-response))
    (cpj/GET "/meta-data-roles/:id" request (meta-data/meta-datum request json-response))
    (cpj/GET "/meta-keys/:id" request (meta-keys/meta-key request json-response))
    (cpj/GET "/meta-keys/" request (meta-keys/index request json-response))

    (cpj/GET "/people/" request (people/people request json-response))
    (cpj/GET "/people/:id" request (people/person request json-response))
    (cpj/PATCH "/people/:id" request (people/person request json-response))
    (cpj/POST "/people/" request (people/person request json-response))

    (cpj/GET "/roles/" request (roles/roles request json-response))
    (cpj/GET "/roles/:id" request (roles/role request json-response))

    (cpj/GET "/users/" request (users/users request json-response))
    (cpj/GET "/groups/:group-id/users/" request (group-users/users request json-response))
    (cpj/GET "/users/:id" request (users/user request json-response))
    (cpj/GET "/groups/:group-id/users/:user-id" request (users/user request json-response))
    (cpj/PUT "/groups/:group-id/users/:user-id" request (users/user request json-response))
    (cpj/PATCH "/users/:id" request (users/user request json-response))
    (cpj/POST "/users/" request (users/user request json-response))

    (cpj/GET "/previews/:id" request (previews/preview request json-response))

    (cpj/GET "/vocabularies/" request (vocabularies/index request json-response))
    (cpj/GET "/vocabularies/:id" request (vocabularies/vocabulary request json-response))))

(defn handler [request response]
  (let [body (:body response)]
    (if-not (and body (map? body))
      response
      (let [json-roa-handler (when (< (:status response) 300) (build-routes-handler response))
            json-roa-data (select-keys (if json-roa-handler
                                         (json-roa-handler request)
                                         {:relations {:root (links/root (:context request))}})
                                       [:self-relation :relations :collection :name])
            amended-json-roa-data (amend-json-roa request json-roa-data)]
        (update-in response
                   [:body]
                   (fn [original-body json-road-data]
                     (into {} (sort (conj {} original-body json-road-data))))
                   amended-json-roa-data)))))


;### Debug ####################################################################
;(debug/debug-ns *ns*)
