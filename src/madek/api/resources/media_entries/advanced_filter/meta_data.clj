(ns madek.api.resources.media-entries.advanced-filter.meta-data
  (:require
   [clojure.java.jdbc :as jdbc]
   [clojure.tools.logging :as logging]
   [logbug.catcher :as catcher]
   [logbug.debug :as debug]
   [madek.api.resources.meta-keys.meta-key :as meta-key]
   [madek.api.utils.rdbms :as rdbms]
   [madek.api.utils.sql :as sql]))

(def ^:private match-columns {"meta_data_people" {:table "people",
                                                  :resource "person",
                                                  :match_column "searchable"}
                              "meta_data_keywords" {:table "keywords",
                                                    :resource "keyword",
                                                    :match_column "term"}})

(defn- get-meta-datum-object-type [meta-datum-spec]
  (or (:type meta-datum-spec)
      (:meta_datum_object_type
       (first
        (jdbc/query
         (rdbms/get-ds)
         (meta-key/build-meta-key-query (:key meta-datum-spec)))))))

(defn- sql-merge-join-related-meta-data
  ([initial-sqlmap counter related-meta-data-table]
   (sql-merge-join-related-meta-data initial-sqlmap
                                     counter
                                     related-meta-data-table
                                     nil))
  ([initial-sqlmap counter related-meta-data-table match-value]
   (let [meta-data-alias (str "md" counter)
         related-meta-data-alias (str "rmd" counter)]
     (cond-> initial-sqlmap
       related-meta-data-table
       (sql/merge-join [(keyword related-meta-data-table)
                        (keyword related-meta-data-alias)]
                       [:=
                        (keyword (str related-meta-data-alias ".meta_datum_id"))
                        (keyword (str meta-data-alias ".id"))])

       (and related-meta-data-table match-value)
       (->
        (sql/merge-join
         (keyword (get-in match-columns [related-meta-data-table :table]))
         [:=
          (keyword
           (str related-meta-data-alias "."
                (get-in match-columns [related-meta-data-table :resource]) "_id"))
          (keyword
           (str (get-in match-columns [related-meta-data-table :table]) ".id"))]))))))

(defn- sql-merge-where-with-value
  [sqlmap counter related-meta-data-table value]
  (let [related-meta-data-alias (str "rmd" counter)]
    (-> sqlmap
        (sql/merge-where
         [:=
          (keyword (str related-meta-data-alias
                        "."
                        (get-in match-columns
                                [related-meta-data-table :resource])
                        "_id"))
          value]))))

(defn- sql-raw-text-search [column search-string]
  (sql/raw
    ; we need to pass 'english' because it was also used
    ; when creating indexes
   (str "to_tsvector('english', " column ")"
        " @@ plainto_tsquery('english', '" search-string "')")))

(defn- sql-merge-where-with-match
  [sqlmap related-meta-data-table match]
  (cond-> sqlmap
    related-meta-data-table
    (sql/merge-where
     (sql-raw-text-search
      (str (get-in match-columns
                   [related-meta-data-table :table])
           "."
           (get-in match-columns
                   [related-meta-data-table :match_column]))
      match))))

(defn- primitive-type? [md-object-type]
  (or (= md-object-type "MetaDatum::Text")
      (= md-object-type "MetaDatum::TextDate")))

(defn- sql-meta-data-from-public-vocabularies [sqlmap meta-data-alias counter]
  (let [meta-keys-alias (str "mk" counter)
        vocabularies-alias (str "v" counter)]
    (-> sqlmap
        (sql/merge-join [:meta_keys (keyword meta-keys-alias)]
                        [:= (keyword (str meta-data-alias ".meta_key_id"))
                         (keyword (str meta-keys-alias ".id"))])
        (sql/merge-join [:vocabularies (keyword vocabularies-alias)]
                        [:and
                         [:= (keyword (str meta-keys-alias ".vocabulary_id"))
                          (keyword (str vocabularies-alias ".id"))]
                         [:= (keyword (str vocabularies-alias ".enabled_for_public_view")) true]]))))

(defn- sql-merge-join-meta-data
  [sqlmap counter md-object-type {meta-key :key
                                  not-meta-key :not_key
                                  match :match}]
  (let [meta-data-alias (str "md" counter)
        join-conditions (cond-> [:and [:=
                                       (keyword (str meta-data-alias
                                                     ".media_entry_id"))
                                       :media_entries.id]]

                          (and (primitive-type? md-object-type) match)
                          (conj (sql-raw-text-search (str meta-data-alias ".string")
                                                     match))

                          (not= meta-key "any")
                          (conj [(cond meta-key := not-meta-key :!=)
                                 (keyword (str meta-data-alias ".meta_key_id"))
                                 (or meta-key not-meta-key)]))]

    (-> (sql/merge-join sqlmap
                        [:meta_data (keyword meta-data-alias)]
                        join-conditions)
        (sql-meta-data-from-public-vocabularies meta-data-alias counter))))

(defn sql-search-through-all [sqlmap search-string]
  (cond-> sqlmap
    search-string
    (sql/merge-where
     (cons :or
           (into [[:exists
                   (-> (sql/select true)
                       (sql/from :meta_data)
                       (sql/merge-where [:= :meta_data.media_entry_id :media_entries.id]
                                        (sql-raw-text-search "meta_data.string"
                                                             search-string)))]]
                 (map #(let [resource_table (get-in match-columns [% :table])]
                         [:exists
                          (-> (sql/select true)
                              (sql/from (keyword resource_table))
                              (sql/merge-join (keyword %)
                                              [:=
                                               (keyword (str % "." (get-in match-columns [% :resource]) "_id"))
                                               (keyword (str resource_table ".id"))])
                              (sql/merge-join :meta_data
                                              [:=
                                               (keyword (str % ".meta_datum_id"))
                                               :meta_data.id])
                              (sql/merge-where
                               (sql-raw-text-search
                                (str resource_table "."
                                     (get-in match-columns [% :match_column]))
                                search-string))
                              (sql/merge-where [:= :meta_data.media_entry_id :media_entries.id]))])
                      (keys match-columns)))))))

(defn- extend-sqlmap-according-to-meta-datum-spec [sqlmap [meta-datum-spec counter]]
  (let [meta-datum-object-type (get-meta-datum-object-type meta-datum-spec)
        related-meta-data-table (case meta-datum-object-type
                                  "MetaDatum::People" "meta_data_people"
                                  "MetaDatum::Keywords" "meta_data_keywords"
                                  nil)
        sqlmap-with-joined-meta-data (sql-merge-join-meta-data sqlmap
                                                               counter
                                                               meta-datum-object-type
                                                               meta-datum-spec)]

    (cond
      ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

      (and (:key meta-datum-spec)
           (not= (:key meta-datum-spec) "any")
           (:value meta-datum-spec))

      (-> sqlmap-with-joined-meta-data
          (sql-merge-join-related-meta-data counter
                                            related-meta-data-table)
          (sql-merge-where-with-value counter
                                      related-meta-data-table
                                      (:value meta-datum-spec)))

      ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

      (and (:key meta-datum-spec)
           (not= (:key meta-datum-spec) "any")
           (:match meta-datum-spec))

      (-> sqlmap-with-joined-meta-data
          (sql-merge-join-related-meta-data counter
                                            related-meta-data-table
                                            (:match meta-datum-spec))
          (sql-merge-where-with-match related-meta-data-table
                                      (:match meta-datum-spec)))

      ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

      (and (= (:key meta-datum-spec) "any")
           (:value meta-datum-spec)
           (:type meta-datum-spec))

      (-> sqlmap-with-joined-meta-data
          (sql-merge-join-related-meta-data counter
                                            related-meta-data-table)
          (sql-merge-where-with-value counter
                                      related-meta-data-table
                                      (:value meta-datum-spec)))

      ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

      (and (= (:key meta-datum-spec) "any")
           (:match meta-datum-spec)
           (:type meta-datum-spec))

      (-> sqlmap-with-joined-meta-data
          (sql-merge-join-related-meta-data counter
                                            related-meta-data-table
                                            (:match meta-datum-spec))
          (sql-merge-where-with-match related-meta-data-table
                                      (:match meta-datum-spec)))

      ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

      (and (= (:key meta-datum-spec) "any")
           (:match meta-datum-spec)
           (not (:type meta-datum-spec)))

      (sql-search-through-all sqlmap (:match meta-datum-spec))

      ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

      (and (:key meta-datum-spec)
           (not (:value meta-datum-spec))
           (not (:match meta-datum-spec)))

      sqlmap-with-joined-meta-data

      ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

      (and (:not_key meta-datum-spec)
           (not (:value meta-datum-spec))
           (not (:match meta-datum-spec)))

      sqlmap-with-joined-meta-data

      :else (throw
             (ex-info
              (str "Invalid meta data filter: " meta-datum-spec)
              {:status 422})))))

(defn sql-filter-by [sqlmap meta-data-specs]
  (if-not (empty? meta-data-specs)
    (-> (reduce extend-sqlmap-according-to-meta-datum-spec
                sqlmap
                (partition 2
                           (interleave meta-data-specs
                                       (iterate inc 1))))
        (sql/modifiers :distinct))
    sqlmap))

;### Debug ####################################################################
;(debug/debug-ns *ns*)
;(debug/wrap-with-log-debug #'filter-by-permissions)
;(debug/wrap-with-log-debug #'build-query)
