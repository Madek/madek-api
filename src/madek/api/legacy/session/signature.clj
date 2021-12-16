; Copyright (C) 2015 Dr. Thomas Schank  (DrTom@schank.ch, Thomas.Schank@algocon.ch)
; Licensed under the terms of the GNU Affero General Public License v3.
; See the "LICENSE.txt" file provided with this software.

(ns madek.api.legacy.session.signature
  (:require
    [clojure.string :refer [split]]
    [clojure.data.json :as json]
    )
   (:import
     [javax.crypto Mac]
     [javax.crypto.spec SecretKeySpec]
     ))

(def ^:const ^:private signing-algorithm "HmacSHA1")

(defn- get-signing-key_ [secret]
  (SecretKeySpec. (.getBytes secret) signing-algorithm))
(def ^:private get-signing-key (memoize get-signing-key_))

(defn- get-mac_ [signing-key]
  (doto (Mac/getInstance signing-algorithm)
    (.init signing-key)))
(def ^:private get-mac (memoize get-mac_))

(defn sha1-hmac [^String message secret]
  (let [mac (get-mac (get-signing-key secret))]
    (->>
      (.doFinal mac (.getBytes message))
      (map (partial format "%02x"))
      (apply str))))

(defn valid? [signature secret message]
  (= signature (sha1-hmac message secret)))

(defn validate! [signature secret message]
  (when-not (valid? signature secret message)
    (throw (IllegalStateException. "Signature validation failed!"))))


