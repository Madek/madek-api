(ns madek.api.resources.locales
  (:require
   [clojure.java.jdbc :as jdbc]
   [clojure.tools.logging :as logging]
   [logbug.debug :as debug]
   [madek.api.utils.config :refer [get-config]]
   [madek.api.utils.rdbms :as rdbms]
   [madek.api.utils.sql :as sql]))

(defn- find-app-setting
  []
  (let [query (-> (sql/select :*)
                  (sql/from :app_settings)
                  (sql/format))]
    (first (jdbc/query (rdbms/get-ds) query))))

(defn- default-locale
  []
  (let [app-setting (find-app-setting)]
    (if-not (nil? app-setting)
      (:default_locale app-setting)
      (let [config (get-config)]
        (:madek_default_locale config)))))

(defn add-field-for-default-locale
  [field-name result]
  (let [field-plural (keyword (str field-name "s"))
        field-name (keyword field-name)]
    (assoc result field-name (get-in result [field-plural (default-locale)]))))
