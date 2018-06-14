(ns madek.api.resources.meta-keys.meta-key
  (:require
    [madek.api.utils.config :as config :refer [get-config]]
    [madek.api.utils.rdbms :as rdbms :refer [get-ds]]
    [madek.api.utils.sql :as sql]
    [madek.api.resources.shared :refer [remove-internal-keys]]

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
      (let [settings (set (:madek_available_locales (get-config)))]
        (into #{}
          (for [locale settings]
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

(defn- localize-result [result locale]
  (if-let [label (get-in result [:labels locale])]
    (assoc result :label label)
    (assoc result :label (get-in result [:labels (default-locale)]))))

(defn- get-io-mappings
  [id]
  (let [query (-> (sql/select :key_map, :io_interface_id)
                  (sql/from :io_mappings)
                  (sql/where
                    [:= :io_mappings.meta_key_id id])
                  (sql/format))]
    (jdbc/query (rdbms/get-ds) query)))

(defn- prepare-io-mappings-from
  [io-mappings]
  (let [groupped (group-by :io_interface_id io-mappings)]
    (let [io-interfaces (keys groupped)]
      (map (fn [io-interface-id] {
        :id io-interface-id
        :keys (reduce (fn [m key-map]
                        (conj m {:key (:key_map key-map)}))
                      []
                      (get groupped io-interface-id))
      }) io-interfaces))))
  

(defn- include-io-mappings
  [result id]
  (let [io-mappings (prepare-io-mappings-from(get-io-mappings id))]
    (assoc result :io_mappings io-mappings)))

(defn build-meta-key-query [id]
  (-> (sql/select :*)
      (sql/from :meta-keys)
      (sql/merge-where
        [:= :meta-keys.id id])
      (sql/format)))

(defn get-meta-key [request]
  (let [id (-> request :params :id)
        query (build-meta-key-query id)]
    (if (re-find #"^[a-z0-9\-\_\:]+:[a-z0-9\-\_\:]+$" id)
      (if-let [meta-key (first
                          (jdbc/query (rdbms/get-ds) query))]
        {:body (include-io-mappings
          (remove-internal-keys
            (localize-result meta-key
              (determine-locale request))) id)}
        {:status 404 :body {:message "Meta-Key could not be found!"}})
      {:status 422
       :body {:message "Wrong meta_key_id format! See documentation."}})))

;### Debug ####################################################################
;(logging-config/set-logger! :level :debug)
;(logging-config/set-logger! :level :info)
;(debug/debug-ns *ns*)
