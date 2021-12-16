; Copyright (C) 2015 Dr. Thomas Schank  (DrTom@schank.ch, Thomas.Schank@algocon.ch)
; Licensed under the terms of the GNU Affero General Public License v3.
; See the "LICENSE.txt" file provided with this software.

(ns madek.api.legacy.session.encryptor
  (:import
    [javax.crypto KeyGenerator SecretKey Cipher]
    [javax.crypto.spec IvParameterSpec SecretKeySpec]
    [java.security SecureRandom]
    )
  (:require
    [madek.api.utils.digest :refer [sha256]]
    [clojure.string :refer [split]]
    [clojure.data.json :as json]
    [madek.api.legacy.session.encoder :refer [decode encode]]
    ))

(defn decrypt [secret encrypted-message]
  (let [cipher (Cipher/getInstance "AES/CBC/PKCS5Padding")
        skey-spec (SecretKeySpec. (sha256 secret) "AES")
        [iv, msg]  (->> (split encrypted-message #"~")
                        (map decode ))
        iv_spec (IvParameterSpec. iv)]
    (.init cipher (Cipher/DECRYPT_MODE) skey-spec iv_spec)
    (->> (.doFinal cipher msg)
         (map char)
         (apply str)
         json/read-str
         clojure.walk/keywordize-keys)))

(def random (SecureRandom.))

(defn encrypt [secret data]
  (let [cipher (Cipher/getInstance "AES/CBC/PKCS5Padding")
        skey-spec (SecretKeySpec. (sha256 secret) "AES")
        seed (.generateSeed random 16)
        iv_spec (IvParameterSpec. seed)
        _ (.init cipher (Cipher/ENCRYPT_MODE) skey-spec iv_spec)
        crypt (->> data
                   json/write-str
                   (map (comp byte int))
                   byte-array
                   (.doFinal cipher))]
    (->> [seed, crypt]
         (map encode)
         (clojure.string/join "~"))))
