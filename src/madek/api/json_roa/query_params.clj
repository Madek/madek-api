(ns madek.api.json-roa.query-params
  (:require
   [cheshire.core :as json]
   [clj-http.client :as http-client]))

(defn- encode-query-param-value [value]
  "Re-encode maps/vectors as JSON so they round-trip with
  wrap-parse-json-query-parameters. Scalars are left unchanged."
  (if (or (map? value) (sequential? value))
    (json/generate-string value)
    value))

(defn encode-query-params [query-params]
  (->> query-params
       (map (fn [[k v]] [k (encode-query-param-value v)]))
       (into {})))

(defn generate-query-string [query-params]
  (http-client/generate-query-string (encode-query-params query-params)))
