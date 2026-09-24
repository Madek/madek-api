(ns madek.api.pagination
  (:require
   [clojure.tools.logging :as logging]
   [clojure.walk :refer [keywordize-keys]]
   [compojure.core :as cpj]
   [logbug.debug :as debug]
   [madek.api.utils.rdbms :as rdbms]
   [madek.api.utils.sql :as sql]))

(def LIMIT 100)

(defn paginated-response
  "Pair with `add-offset-with-lookahead-for-honeysql`: the query must fetch
  LIMIT+1 rows. Trim the collection at `primary-key` (and any other
  sequential body values) to LIMIT, and set `::has-next-page?` from that
  key so JSON-ROA `next` is emitted only when a following page exists."
  [primary-key body]
  (let [items (get body primary-key)]
    {:body (into {}
                 (map (fn [[k v]]
                        [k (if (sequential? v)
                             (vec (take LIMIT v))
                             v)])
                      body))
     ::has-next-page? (> (count items) LIMIT)}))

(defn has-next-page?
  "True when the index handler built the response with `paginated-response`
  after a lookahead query and more than LIMIT rows were available."
  [response]
  (::has-next-page? response))

(defn page-number [params]
  (or (-> params keywordize-keys :page)
      0))

(defn compute-offset [params]
  (let [page (page-number params)]
    (* LIMIT page)))

(defn add-offset-with-lookahead-for-honeysql [query params]
  (let [off (compute-offset params)]
    (-> query
        (sql/offset off)
        (sql/limit (inc LIMIT)))))

(defn next-page-query-query-params [query-params]
  (let [query-params (keywordize-keys query-params)
        i-page (page-number query-params)]
    (assoc query-params
           :page (+ i-page 1))))

;### Debug ####################################################################
;(debug/debug-ns *ns*)
