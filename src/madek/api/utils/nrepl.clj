; Copyright © 2013 - 2017 Dr. Thomas Schank <Thomas.Schank@AlgoCon.ch>
; Licensed under the terms of the GNU Affero General Public License v3.
; See the "LICENSE.txt" file provided with this software.

(ns madek.api.utils.nrepl
  (:refer-clojure :exclude [str keyword])
  (:require
    [camel-snake-kebab.core :refer [->snake_case]]
    [clj-yaml.core :as yaml]
    [clojure.java.io :as io]
    [clojure.string :refer [upper-case]]
    [clojure.tools.logging :as logging :refer [debug info]]
    [environ.core :refer [env]]
    [logbug.catcher :as catcher]
    [madek.api.utils.core :refer [presence keyword str]]
    [nrepl.server :as nrepl-server :refer [start-server stop-server]]
    ))


;;; cli-options ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn long-opt-for-key [k]
  (str "--" k " " (-> k str ->snake_case upper-case)))

(defonce options* (atom nil))

(def repl-enable-key :repl)
(def repl-port-key :repl-port)
(def repl-bind-key :repl-bind)
(def repl-port-file-key :repl-port-file)
(def options-keys [repl-enable-key repl-bind-key repl-port-key repl-port-file-key])

(def cli-options
  [["-r" (long-opt-for-key repl-enable-key) "start the nREPL server"
    :default (or (some-> repl-enable-key env yaml/parse-string) false)
    :parse-fn #(yaml/parse-string %)
    :validate [boolean? "Must parse to a boolean"]]
   [nil (long-opt-for-key repl-port-key) "nREPL port"
    :default  (some-> repl-port-key env Integer/parseInt)
    :parse-fn #(Integer/parseInt %)
    :validate [#(< 0 % 0x10000) "Must be a number between 0 and 65536"]]
   [nil (long-opt-for-key repl-bind-key) "nREPL bind interface"
    :default (some-> repl-bind-key env)]
   [nil (long-opt-for-key repl-port-file-key ) "write port to this file; NO (or any YAML falsy) disables this"
    :default (or (some-> repl-port-file-key env yaml/parse-string) ".nrepl-port")
    :validate [#(or (false? %) (presence %)) "Must be false or present"]]])



;;; server ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defonce server* (atom nil))

(defn stop []
  (when @server*
    (info "stopping nREPL server " @server*)
    (stop-server @server*)
    (when-let [port-file (repl-port-file-key @options*)]
      (io/delete-file port-file true))
    (reset! server* nil)
    (reset! options* nil)))

(defn init [config options]
  (info 'init options)
  (if @server*
    (info "repl server ist already running, ignoring init")
    (do (reset! options* (select-keys options options-keys))
        (stop)
        (when (or (:enabled config)
                  (repl-enable-key @options*))
          (let [bind (or (:bind config) (repl-bind-key @options*))
                port (or (:port config) (repl-port-key @options*))]
            (info "starting nREPL server " port bind)
            (reset! server* (start-server :bind bind :port port))
            (when (:dev-mode options)
              (when-let [port-file (repl-port-file-key @options*)]
                (spit port-file (str port))))
            (info "started nREPL server "))))))


