; Copyright © 2013 - 2017 Dr. Thomas Schank <Thomas.Schank@AlgoCon.ch>
; Licensed under the terms of the GNU Affero General Public License v3.
; See the "LICENSE.txt" file provided with this software.

(ns madek.api.utils.runtime
  (:require
   [clj-commons-exec :as commons-exec]
   [clojure.tools.logging :as logging]
   [logbug.debug :as debug])
  (:import
   [humanize Humanize]))

(defn check-memory-usage []
  (System/gc)
  (let [rt (Runtime/getRuntime)
        max-mem (.maxMemory rt)
        allocated-mem (.totalMemory rt)
        free (.freeMemory rt)
        used (- allocated-mem free)
        usage (double (/ used max-mem))
        ok? (and (< usage 0.95) (> free))
        stats {:OK? ok?
               :Max (Humanize/binaryPrefix max-mem)
               :Allocated (Humanize/binaryPrefix allocated-mem)
               :Used (Humanize/binaryPrefix used)
               :Usage (Double/parseDouble (String/format "%.2f" (into-array [usage])))}]
    (when-not ok? (logging/fatal stats))
    stats))

;### Debug #####################################################################
;(debug/debug-ns *ns*)
