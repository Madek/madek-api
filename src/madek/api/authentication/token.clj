(ns madek.api.authentication.token
  (:require
   [clojure.java.jdbc :as jdbc]
   [clojure.tools.logging :as logging]
   [logbug.debug :as debug]
   [logbug.thrown :as thrown]
   [madek.api.authentication.shared :refer [sql-select-user-admin-scopes]]
   [madek.api.utils.rdbms :as rdbms]
   [madek.api.utils.sql :as sql]
   [pandect.algo.sha256 :as algo.sha256])
  (:import
   [java.util Base64]))

(defn ^String base64-encode [^bytes bts]
  (String. (.encode (Base64/getEncoder) bts)))

(defn hash-string [s]
  (->> s
       algo.sha256/sha256-bytes
       base64-encode))

(defn find-user-token-by-some-secret [secrets]
  (->> (-> (sql/select :users.*
                       [:scope_read :token_scope_read]
                       [:scope_write :token_scope_write]
                       [:revoked :token_revoked]
                       [:description :token_description])
           (sql/from :api_tokens)
           (sql/merge-where [:in :api_tokens.token_hash
                             (->> secrets
                                  (filter identity)
                                  (map hash-string))])
           (sql/merge-where [:<> :api_tokens.revoked true])
           (sql/merge-where (sql/raw "now() < api_tokens.expires_at"))
           (sql/merge-join :users [:= :users.id :api_tokens.user_id])
           sql-select-user-admin-scopes
           (sql/format))
       (jdbc/query (rdbms/get-ds))
       (map #(clojure.set/rename-keys % {:email :email_address}))
       first))

(defn violates-not-read? [user-token request]
  (and (not (:token_scope_read user-token))
       (#{:get :head :options}
        (:request-method request))))

(defn violates-not-write? [user-token request]
  (and (not (:token_scope_write user-token))
       (#{:delete :put :post :patch}
        (:request-method request))))

(defn authenticate [user-token handler request]
  (cond
    (:token_revoked user-token) {:status 401
                                 :body "The token has been revoked."}
    (violates-not-read?
     user-token request) {:status 403
                          :body (str "The token is not allowed to read"
                                     " i.e. to use safe http verbs.")}
    (violates-not-write?
     user-token request) {:status 403
                          :body (str "The token is not allowed to write"
                                     " i.e. to use unsafe http verbs.")}
    :else (handler
           (assoc request
                  :authenticated-entity (assoc user-token :type "User")))))

(defn find-token-secret-in-header [request]
  (when-let [header-value (-> request :headers :authorization)]
    (when (re-matches #"(?i)^token\s+.+$" header-value)
      (last (re-find #"(?i)^token\s+(.+)$" header-value)))))

(defn find-and-authenticate-token-secret-or-continue [handler request]
  (if-let [token-secret (find-token-secret-in-header request)]
    (if-let [user-token (find-user-token-by-some-secret [token-secret])]
      (authenticate user-token handler request)
      {:status 401
       :body "No token for this token-secret found!"})
    (handler request)))

(defn wrap [handler]
  (fn [request]
    (find-and-authenticate-token-secret-or-continue handler request)))

;### Debug ####################################################################
;(debug/wrap-with-log-debug #'authenticate-token-header)
;(debug/debug-ns *ns*)
