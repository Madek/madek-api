(ns madek.api.sql
  (:require
    [honeysql.format :as hsql-format]
    [honeysql.types :as hsql-types]
    [honeysql.helpers :as hsql-helpers]
    [honeysql.util :refer [defalias]]
    ))

(defalias sql-delete-from hsql-helpers/delete-from)
(defalias sql-format hsql-format/format)
(defalias sql-from hsql-helpers/from)
(defalias sql-limit hsql-helpers/limit)
(defalias sql-merge-where honeysql.helpers/merge-where)
(defalias sql-returning honeysql.helpers/returning)
(defalias sql-select hsql-helpers/select)
(defalias sql-using hsql-helpers/using)
