(ns madek.api.main
  (:gen-class)
  (:require
    [clojure.pprint :refer [pprint]]
    [clojure.tools.cli :as cli]
    [clojure.java.jdbc :as jdbc]
    [clojure.tools.logging :as logging]
    [logbug.catcher :as catcher]
    [logbug.debug :as debug]
    [logbug.thrown]
    [madek.api.constants :as constants]
    [madek.api.utils.config :as config :refer [get-config]]
    [madek.api.utils.exit :as exit]
    [madek.api.utils.nrepl :as nrepl]
    [madek.api.utils.rdbms :as rdbms]
    [madek.api.web]
    [pg-types.all]
    [taoensso.timbre :as timbre :refer []]
    [taoensso.timbre.tools.logging]
    ))

;; cli ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def cli-options
  (concat
    [["-h" "--help"]
     ["-d" "--dev-mode"]]
    exit/cli-options
    nrepl/cli-options))

(defn main-usage [options-summary & more]
  (->> ["Madek API"
        ""
        "usage: madek-api [<opts>] "
        ""
        "Options:"
        options-summary
        ""
        ""
        (when more
          ["-------------------------------------------------------------------"
           (with-out-str (pprint more))
           "-------------------------------------------------------------------"])]
       flatten (clojure.string/join \newline)))


(defn helpnexit [summary args options]
  (println (main-usage summary {:args args :options options})))


;; run ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn run [options]
  (catcher/snatch
    {:level :fatal
     :throwable Throwable
     :return-fn (fn [e] (System/exit -1))}
    (logging/info 'madek.api.main "initializing ...")
    (madek.api.utils.config/initialize
      {:filenames ["./config/settings.yml"
                   "../config/settings.yml",
                   "./datalayer/config/settings.yml",
                   "../webapp/datalayer/config/settings.yml",
                   "./config/settings.local.yml"
                   "../config/settings.local.yml"]})
    (logging/info "Effective startup options " options)
    (logging/info "Effective startup config " (get-config))
    (rdbms/initialize (config/get-db-spec :api))
    (nrepl/init (-> (get-config) :services :api :nrepl) options)
    (madek.api.web/initialize)
    (madek.api.constants/initialize (get-config))
    (logging/info 'madek.api.main "... initialized")))


;; main ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defonce args* (atom nil))

(defn main []
  (logging/info "main")
  (let [args @args*]
    (let [args @args*
          {:keys [options arguments errors summary]}
          (cli/parse-opts args cli-options :in-order true)
          options (merge (sorted-map) options)]
      (logging/info "options" options)
      (exit/init options)
      (cond
        (:help options) (helpnexit summary args options)
        :else (run options)))))

(defn -main [& args]
  (timbre/merge-config! constants/DEFAULT_LOGGING_CONFIG)
  ;(logbug.thrown/reset-ns-filter-regex #".*madek.*")
  (reset! args* args)
  (main))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

; hot reload on require
(when @args* (main))



;### Debug ####################################################################
;(debug/debug-ns 'madek.api.utils.rdbms)
