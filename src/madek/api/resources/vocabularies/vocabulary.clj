(ns madek.api.resources.vocabularies.vocabulary
  (:require
    [madek.api.utils.config :as config :refer [get-config]]
    [madek.api.utils.rdbms :as rdbms :refer [get-ds]]
    [madek.api.utils.sql :as sql]
    [madek.api.resources.shared :refer [remove-internal-keys]]
    [madek.api.resources.vocabularies.permissions :as permissions]

    [clj-logging-config.log4j :as logging-config]
    [clojure.java.jdbc :as jdbc]
    [clojure.tools.logging :as logging]
    [logbug.debug :as debug]
    ))

(defn- find-app-setting
  []
  (let [query (-> (sql/select :*)
                  (sql/from :app_settings)
                  (sql/format))]
    (first (jdbc/query (rdbms/get-ds) query))))

(defn- available-locales []
  (let [settings (find-app-setting)]
    (if-not (nil? settings)
      (set (:available_locales settings))
      (let [locales (set (:madek_available_locales (get-config)))]
        (into #{}
          (for [locale locales]
            locale))))))

(defn- default-locale []
  (let [app-setting (find-app-setting)]
    (if-not (nil? app-setting)
      (:default_locale app-setting)
      (let [config (get-config)]
        (:madek_default_locale config)))))

(defn- determine-locale [request]
  (let [locale (get-in request [:query-params :lang] (default-locale))]
    (if (and (some? locale) (contains? (available-locales) locale))
      locale
      (default-locale))))

(defn- localize-field [field-name result locale]
  (let [field-plural (keyword (str field-name "s"))
        field-name   (keyword field-name)]
    (if-let [localized-field (get-in result [field-plural locale])]
      (assoc result field-name localized-field)
      (assoc result field-name (get-in result [field-plural (default-locale)])))))

(defn- localize-result [result locale]
  (localize-field "label" (localize-field "description" result locale) locale))

(defn- where-clause
  [id user-id]
  (let [public [:= :vocabularies.enabled_for_public_view true]
        id-match [:= :vocabularies.id id]]
    (if user-id
      (let [vocabulary-ids (permissions/accessible-vocabulary-ids user-id)]
        [:and
         [:or public [:in :vocabularies.id vocabulary-ids]]
         id-match])
      [:and public id-match])))

(defn build-vocabulary-query [id user-id]
  (-> (sql/select :*)
      (sql/from :vocabularies)
      (sql/where (where-clause id user-id))
      (sql/format)))

(defn get-vocabulary [request]
  (let [id (-> request :params :id)
        user-id (-> request :authenticated-entity :id)
        query (build-vocabulary-query id user-id)]
    (if-let [vocabulary (first (jdbc/query (rdbms/get-ds) query))]
      {:body (localize-result (remove-internal-keys vocabulary) (determine-locale request))}
      {:status 404 :body {:message "Vocabulary could not be found!"}})))

;### Debug ####################################################################
;(logging-config/set-logger! :level :debug)
;(logging-config/set-logger! :level :info)
;(debug/debug-ns *ns*)
