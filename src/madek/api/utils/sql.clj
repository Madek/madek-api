(ns madek.api.utils.sql
  (:refer-clojure :exclude [format])
  (:require
    [honeysql.format :as format]
    [honeysql.helpers :as helpers :refer [build-clause defhelper]]
    [honeysql.types :as types]
    [honeysql.util :as util :refer [defalias]]


    [logbug.debug :as debug]
    [clj-logging-config.log4j :as logging-config]
    [clojure.tools.logging :as logging]
    ))

(defmethod format/fn-handler "~*" [_ field value]
  (str (format/to-sql field) " ~* " (format/to-sql value)))

(defn dedup-join [honeymap]
  (assoc honeymap :join
         (reduce #(let [[k v] %2] (conj %1 k v)) []
                 (clojure.core/distinct (partition 2 (:join honeymap))))))

(defn format
  "Calls honeysql.format/format with removed join duplications in sql-map."
  [sql-map & params-or-opts]
  (apply format/format [(dedup-join sql-map) params-or-opts]))


(defalias call types/call)
(defalias param types/param)
(defalias raw types/raw)

(defalias format-predicate format/format-predicate)
(defalias quote-identifier format/quote-identifier)

(defalias columns helpers/columns)
(defalias delete-from helpers/delete-from)
(defalias from helpers/from)
(defalias group helpers/group)
(defalias insert-into helpers/insert-into)
(defalias join helpers/join)
(defalias limit helpers/limit)
(defalias merge-join helpers/merge-join)
(defalias merge-where helpers/merge-where)
(defalias modifiers helpers/modifiers)
(defalias offset helpers/offset)
(defalias order-by helpers/order-by)
(defhelper returning [m fields]
  (assoc m :returning (helpers/collify fields)))
(defalias select helpers/select)
(defhelper using [m tables]
  (assoc m :using (helpers/collify tables)))
(defalias values helpers/values)
(defalias where helpers/where)



;#### debug ###################################################################
;(logging-config/set-logger! :level :debug)
;(logging-config/set-logger! :level :info)
;(debug/debug-ns *ns*)
