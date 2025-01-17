(ns madek.api.resources.delegations.main
  (:require
   [clojure.java.jdbc :as jdbc]
   [compojure.core :as cpj]
   [honey.sql :refer [format] :rename {format sql-format}]
   [honey.sql.helpers :as sql]
   [madek.api.utils.rdbms :as rdbms]
   [taoensso.timbre :refer [debug info spy]]))

(defn delegation
  [{{delegation-id :id} :route-params :as request}]
  (info "delegation" delegation-id request)
  (if-let [delegation (-> (sql/select :*)
                          (sql/from :delegations)
                          (sql/where [:= :delegations.id delegation-id])
                          (sql/select true)
                          (sql-format)
                          (->> (jdbc/query (rdbms/get-ds)) first))]
    {:status 200
     :body (select-keys delegation [:id :name :description])}
    {:status 404
     :body {}}))

(def routes
  (-> (cpj/routes
       (cpj/GET "/delegations/:id" [] delegation))))

