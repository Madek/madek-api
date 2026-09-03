(ns madek.api.resources.media-resources.advanced-filter.permissions
  (:require
   [madek.api.utils.sql :as sql]))

(defn sql-merge-where-permission-spec
  [table-name
   {:keys [user-permission-exists
           group-permission-for-user-exists
           group-permission-exists]}
   sqlmap
   permission-spec]
  (case (:key permission-spec)
    "public"
    (-> sqlmap
        (sql/merge-where
         [:=
          (keyword (str table-name ".get_metadata_and_previews"))
          (case (:value permission-spec)
            "true" true
            "false" false
            (throw
             (ex-info
              (str "Invalid filter for \"public\" permission!")
              {:status 422})))]))

    "responsible_user"
    (-> sqlmap
        (sql/merge-where [:=
                          (keyword (str table-name ".responsible_user_id"))
                          (:value permission-spec)]))

    "responsible_delegation"
    (-> sqlmap
        (sql/merge-where [:=
                          (keyword (str table-name ".responsible_delegation_id"))
                          (:value permission-spec)]))

    "entrusted_to_user"
    (-> sqlmap
        (sql/merge-where
         [:or
          (user-permission-exists "get_metadata_and_previews"
                                  (:value permission-spec))
          (group-permission-for-user-exists "get_metadata_and_previews"
                                            (:value permission-spec))]))

    "entrusted_to_group"
    (-> sqlmap
        (sql/merge-where
         (group-permission-exists "get_metadata_and_previews"
                                  (:value permission-spec))))

    (throw
     (ex-info
      (str "Invalid permission filter key: " (:key permission-spec))
      {:status 422}))))

(defn sql-filter-by [table-name conditions sqlmap permission-specs]
  (if-not (empty? permission-specs)
    (reduce (partial sql-merge-where-permission-spec table-name conditions)
            sqlmap
            permission-specs)
    sqlmap))
