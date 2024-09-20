; Copyright © 2013 - 2017 Dr. Thomas Schank <Thomas.Schank@AlgoCon.ch>
; Licensed under the terms of the GNU Affero General Public License v3.
; See the "LICENSE.txt" file provided with this software.

(ns madek.api.utils.fs
  (:require
   [clj-uuid]
   [clojure.string :as string]
   [clojure.tools.logging :as logging]
   [logbug.debug :as debug])
  (:import
   [java.io File]
   [org.apache.commons.lang3 SystemUtils]))

(defn directory? [path]
  (let [file (clojure.java.io/file path)]
    (and (.exists file)
         (.isDirectory file))))

(defn assert-directory [path]
  (when-not (directory? path)
    (throw (IllegalStateException. "Directory does not exist."))))

(defn path-proof
  "Returns a unique - whenever (str x) is unique - representation of x that can
  be safely used as a filename or as a part of a path."
  [x]
  (str (-> x
           str
           (string/replace #"[\W_-]+" "-")
           (string/replace #"^-" "")
           (string/replace #"-$" ""))
       "_"
       (clj-uuid/v5 clj-uuid/+null+ (str x))))

(defn system-path [& args]
  "Returns a path (segment) by joining the arguments
  with the system specific file separator."
  (clojure.string/join (File/separator) args))

(defn system-path-abs [& args]
  (if-not SystemUtils/IS_OS_WINDOWS
    (str (File/separator) (apply system-path args))
    (throw (UnsupportedOperationException.
            "`system-path-abs` is not implement for this platform."))))
