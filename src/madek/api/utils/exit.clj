(ns madek.api.utils.exit
  (:refer-clojure :exclude [str keyword])
  (:require
   [clj-pid.core :as pid]
   [clojure.java.io :as io]
   [environ.core :refer [env]]
   [signal.handler]
   [taoensso.timbre :refer [debug info warn error spy]]))

(def cli-options
  [[nil "--pid-file PID_FILE"
    :default (some-> :pid-file env)]])

(defonce options* (atom nil))

(defn exit
  ([] (exit 0))
  ([status]
   (info 'exit [status] @options*)
   (if (:dev-mode @options*)
     (info "ignoring exit in dev-mode")
     (System/exit status))))

(defn init [options]
  (info 'init [options])
  (reset! options* options)
  (when-let [pid-file (:pid-file options)]
    (info "PID_FILE" pid-file)
    (io/make-parents pid-file) ; ensure dirs exist before creating file!
    (pid/save pid-file)
    (pid/delete-on-shutdown! pid-file))
  (signal.handler/with-handler :term
    (info "Received SIGTERM, exiting ...")
    (exit 0)))
