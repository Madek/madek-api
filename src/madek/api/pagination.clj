(ns madek.api.pagination
  (:require
   [clojure.tools.logging :as logging]
   [clojure.walk :refer [keywordize-keys]]
   [compojure.core :as cpj]
   [logbug.debug :as debug]
   [madek.api.utils.rdbms :as rdbms]
   [madek.api.utils.sql :as sql]))

(def LIMIT 100)

(defn page-number [params]
  (or (-> params keywordize-keys :page)
      0))

(defn compute-offset [params]
  (let [page (page-number params)]
    (* LIMIT page)))

(defn add-offset-for-honeysql [query params]
  (let [off (compute-offset params)]
    (-> query
        (sql/offset off)
        (sql/limit LIMIT))))

(defn next-page-query-query-params [query-params]
  (let [query-params (keywordize-keys query-params)
        i-page (page-number query-params)]
    (assoc query-params
           :page (+ i-page 1))))

;### Debug ####################################################################
;(debug/debug-ns *ns*)
