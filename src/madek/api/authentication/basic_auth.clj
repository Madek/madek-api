(ns madek.api.authentication.basic-auth
  (:require 
    [clojure.data.codec.base64 :as base64]
    [clojure.tools.logging :as logging]
    [drtom.logbug.thrown :as thrown]
    ))

(defn- decode-base64
  [^String string]
  (apply str (map char (base64/decode (.getBytes string)))))

(defn extract [request]
  (logging/debug 'extract request)
  (try (let [auth-header (-> request :headers :authorization)
             decoded-val (decode-base64 (last (re-find #"^Basic (.*)$" auth-header)))
             [username password] (clojure.string/split (str decoded-val) #":" 2)]
         (logging/debug 'auth-header auth-header)
         (logging/debug 'decoded-val decoded-val)
         (logging/debug 'up [username password])
         {:username username :password password})
       (catch Exception _
         (logging/warn "failed to extract basic-auth properties" ))))


