(ns madek.api.json-roa.query-params-test
  (:require
   [cheshire.core :as json]
   [clojure.string :as string]
   [clojure.test :refer [deftest is testing]]
   [madek.api.json-roa.links :as links]
   [madek.api.json-roa.query-params :as query-params]
   [ring.util.codec :refer [form-decode]]))

(def ^:private filter-by
  {:meta_data [{:key "any" :match "brot"}]})

(def ^:private order
  [["media_entry" "created_at" "desc"]])

(def ^:private params
  {:filter_by filter-by
   :order order
   :page 1})

(defn- decode-query-string [query-string]
  (form-decode query-string))

(defn- query-string-from-path [path]
  (-> path
      (string/split #"\?" 2)
      second
      (string/split #"\{" 2)
      first))

(defn- assert-structured-query-params [decoded]
  (is (= filter-by
         (json/parse-string (get decoded "filter_by") true)))
  (is (= order
         (json/parse-string (get decoded "order"))))
  (is (= "1" (get decoded "page"))))

(deftest generate-query-string-encodes-structured-values-as-json
  (assert-structured-query-params
   (decode-query-string
    (query-params/generate-query-string params))))

(deftest structured-values-round-trip-through-collection-paths
  (doseq [[name path-fn] [["media entries" links/media-entries-path]
                          ["collections" links/collections-path]]]
    (testing name
      (assert-structured-query-params
       (decode-query-string
        (query-string-from-path
         (path-fn "/api" params)))))))
