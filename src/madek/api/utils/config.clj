; Copyright © 2013 - 2017 Dr. Thomas Schank <Thomas.Schank@AlgoCon.ch>
; Licensed under the terms of the GNU Affero General Public License v3.
; See the "LICENSE.txt" file provided with this software.

(ns madek.api.utils.config
  (:require
   [clj-yaml.core :as yaml]
   [clojure.java.io :as io]
   [clojure.set :refer [difference]]
   [clojure.tools.logging :as logging]
   [logbug.catcher :refer [snatch with-logging]]
   [logbug.debug :as debug]
   [madek.api.utils.core :refer [deep-merge]]
   [madek.api.utils.daemon :as daemon :refer [defdaemon]]
   [madek.api.utils.duration :refer [parse-string-to-seconds]]
   [madek.api.utils.fs :refer :all]
   [madek.api.utils.rdbms :as rdbms]
   [me.raynes.fs :as clj-fs]))

(defonce ^:private conf (atom {}))

(defn get-config [] @conf)

(defonce default-opts {:defaults {}
                       :overrides {}
                       :resource-names ["config_default.yml"]
                       :filenames [(system-path "." "config" "config.yml")
                                   (system-path ".." "config" "config.yml")]})

(defonce opts (atom {}))

(defn get-opts [] @opts)

;##############################################################################

(defn exit! []
  (System/exit -1))

;##############################################################################

(defn merge-into-conf [params]
  (when-not (= (get-config)
               (deep-merge (get-config) params))
    (let [new-config (swap! conf
                            (fn [current-config params]
                              (deep-merge current-config params))
                            params)]
      (logging/info "config changed to " new-config))))

(defn slurp-and-merge [config slurpable]
  (->> (slurp slurpable)
       yaml/parse-string
       (deep-merge config)))

(defn read-and-merge-resource-name-configs [config]
  (reduce (fn [config resource-name]
            (if-let [io-resource (io/resource resource-name)]
              (snatch {} (slurp-and-merge config io-resource))
              config))
          config (:resource-names @opts)))

(defn read-and-merge-filename-configs [config]
  (reduce (fn [config filename]
            (if (.exists (io/as-file filename))
              (snatch {} (slurp-and-merge config filename))
              config))
          config (:filenames @opts)))

(defn read-configs-and-merge-into-conf []
  (-> (:defaults @opts)
      (deep-merge (get-config))
      read-and-merge-resource-name-configs
      read-and-merge-filename-configs
      (deep-merge (:overrides @opts))
      merge-into-conf))

(defdaemon "reload-config" 1 (read-configs-and-merge-into-conf))

;### Initialize ###############################################################

(defn initialize [options]
  (snatch {:throwable Throwable
           :level :fatal
           :return-fn (fn [_] (exit!))}
          (let [default-opt-keys (-> default-opts keys set)]
            (assert
             (empty?
              (difference (-> options keys set)
                          default-opt-keys))
             (str "Opts must only contain the following keys: " default-opt-keys))
            (stop-reload-config)
            (Thread/sleep 1000)
            (reset! conf {})
            (let [new-opts (deep-merge default-opts options)]
              (reset! opts new-opts)
              (read-configs-and-merge-into-conf)
              (start-reload-config)))))

;### DB #######################################################################

(defn get-db-spec [service]
  (let [conf (get-config)]
    (deep-merge
     (or (-> conf :database) {})
     (or (-> conf :services service :database) {}))))

;### duration #################################################################

(defn parse-config-duration-to-seconds [& ks]
  (try (if-let [duration-config-value (-> (get-config) (get-in ks))]
         (parse-string-to-seconds duration-config-value)
         (logging/warn (str "No value to parse duration for " ks " was found.")))
       (catch Exception ex
         (cond (instance? clojure.lang.IExceptionInfo ex) (throw ex)
               :else (throw (ex-info "Duration parsing error."
                                     {:config-keys ks} ex))))))

;### Debug ####################################################################
;(debug/debug-ns *ns*)
;(logbug.thrown/reset-ns-filter-regex #".*cider.ci.*")
