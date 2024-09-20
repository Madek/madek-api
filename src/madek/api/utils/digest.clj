(ns madek.api.utils.digest
  (:import
   [java.security MessageDigest]))

(def sha256-message-digest (MessageDigest/getInstance "SHA-256"))

(defn sha256 [s]
  (.digest sha256-message-digest (.getBytes s)))

(defn sha256hex [s]
  (->> s sha256
       (map (partial format "%02x"))
       (apply str)))
