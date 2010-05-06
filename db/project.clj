(defproject MCMS "0.1.0-SNAPSHOT"
 :description "A media collection manager."
 :main fleetdb.server
 :dependencies [[fleetdb "0.1.1-SNAPSHOT"]]
 :dev-dependencies [[leiningen-run "0.2"]]
 :repositories {"clojars" "https://clojars.org/repo"})
)

(ns leiningen.run-db
  (:use [leiningen.compile :only [eval-in-project]]))


(defn run-db [project & args]
  (eval-in-project project
  `(do
     (require 'fleetdb.server)
     (fleetdb.server/-main "-f" "db.fdb"))))
