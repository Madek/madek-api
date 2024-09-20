; Copyright © 2013 - 2017 Dr. Thomas Schank <Thomas.Schank@AlgoCon.ch>
; Licensed under the terms of the GNU Affero General Public License v3.
; See the "LICENSE.txt" file provided with this software.

(ns madek.api.utils.core
  (:refer-clojure :exclude [str keyword]))

;### override some very basic vars in clojure.core ############################

(defn str
  "Like clojure.core/str but maps keywords to strings without preceding colon."
  ([] "")
  ([x]
   (if (keyword? x)
     (subs (clojure.core/str x) 1)
     (clojure.core/str x)))
  ([x & yx]
   (apply clojure.core/str (concat [(str x)] (apply str yx)))))

(defn keyword
  "Like clojure.core/keyword but coerces an unknown single argument x
  with (-> x madek.api.utils.core/str madek.api.utils.core/keyword).
  In contrast clojure.core/keyword will return nil for anything
  not being a String, Symbol or a Keyword already (including
  java.util.UUID, Integer)."
  ([name] (cond (keyword? name) name
                :else (clojure.core/keyword (str name))))
  ([ns name] (clojure.core/keyword ns name)))

(defn deep-merge [& vals]
  (if (every? map? vals)
    (apply merge-with deep-merge vals)
    (last vals)))

(defn presence [v]
  "Returns nil if v is a blank string or if v is an empty collection.
   Returns v otherwise."
  (cond
    (string? v) (if (clojure.string/blank? v) nil v)
    (coll? v) (if (empty? v) nil v)
    :else v))

(defn to-cistr [v]
  "Converts v to a string without preceding colons and preserving `/` if v is a
  keyword.  Converts v to string by applying (str v) if v is not a keyword.
  Invariant: (= s (-> s keyword to-cistr)) if s is a string."
  (if (keyword? v)
    (subs (clojure.core/str v) 1)
    (clojure.core/str v)))

(defn to-ciset [value]
  "Converts a map of key/value pairs to a set of keys. A key is present in the
  set if and only if the value is truthy. Keys are also stringified by
  to-cistr. Inverse of to-cisetmap."
  (cond
    (map? value) (->> value
                      (filter (fn [[_ v]] v))
                      (map (fn [[k _]] k))
                      (map to-cistr)
                      set)
    (coll? value) (->> value
                       (map (fn [v] (to-cistr v)))
                       set)
    :else (throw (ex-info (str "I don't know how to convert a"
                               (type value) " to a ciset") {:input value}))))

(defn to-cisetmap [sq]
  "Converts a seq of keys into a map with true values.
  Keys are always converted to keywords. Inverse of to-cisetmap.
  Invariant:  (= (to-ciset v) (to-ciset (to-cisetmap (to-ciset v)))
  for to-ciset applicable types of v."
  (cond
    (map? sq) (->> sq
                   (map (fn [[k v]] [(keyword k) v]))
                   (into {}))
    (coll? sq) (->> sq
                    (map (fn [k] [(keyword k) true]))
                    (into {}))
    :else (throw (ex-info (str "I don't know how to convert a"
                               (type sq) " to a cisetmap.") {:input sq}))))


