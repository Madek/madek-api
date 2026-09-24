(ns madek.api.json-roa.query-params
  (:require
   [cheshire.core :as json]
   [clj-http.client :as http-client]))

(defn- encode-query-param-value
  "Re-encode maps/vectors as JSON so they round-trip with
  wrap-parse-json-query-parameters. Scalars are left unchanged."
  [value]
  (if (or (map? value) (sequential? value))
    (json/generate-string value)
    value))

(defn- encode-query-params [query-params]
  (update-vals query-params encode-query-param-value))

(defn generate-query-string [query-params]
  (http-client/generate-query-string (encode-query-params query-params)))
