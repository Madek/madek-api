(ns madek.api.constants
  (:require
    [clj-logging-config.log4j :as logging-config]
    [clojure.tools.logging :as logging]
    [environ.core :refer [env]]
    [logbug.catcher :as catcher]
    [logbug.debug :as debug]
    [me.raynes.fs  :as clj-fs]
    [clojure.string :refer [trim blank?]]
    ))

(declare DEFAULT_STORAGE_DIR
         FILE_STORAGE_DIR
         THUMBNAIL_STORAGE_DIR )


(defn- presence [str]
  (and (not (blank? str)) str))

(defn- madek-env []
  (or (presence (env :madek-env))
      (presence (env :rails-env))
      (do (logging/warn "neither MADEK_ENV nor RAILS_ENV is not set; using test")
          "test")))

(defn initialize [config]

  (def DEFAULT_STORAGE_DIR
    (str (clj-fs/absolute
           (or (:default_storage_dir {})
               (clojure.string/join (java.io.File/separator)
                                    [(System/getProperty "user.dir") "tmp" (madek-env)])))))

  (def FILE_STORAGE_DIR
    (str (clj-fs/absolute
           (or (:file_storage_dir {})
               (clojure.string/join (java.io.File/separator)
                                    [DEFAULT_STORAGE_DIR "originals"])))))
  (def THUMBNAILS_STORAGE_DIR
    (str (clj-fs/absolute
           (or (:thumbnail_storage_dir {})
               (clojure.string/join (java.io.File/separator)
                                    [DEFAULT_STORAGE_DIR "thumbnails"])))))
  )


;### Debug ####################################################################
;(logging-config/set-logger! :level :debug)
;(logging-config/set-logger! :level :info)
;(debug/debug-ns *ns*)
