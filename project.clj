(defproject MCMS "0.1.0-SNAPSHOT"
 :description "A media collection manager."
 :dependencies [[enlive "1.0.0-SNAPSHOT"]
                [compojure "0.3.2"]
		[fleetdb "0.1.1-SNAPSHOT"]
                [fleetdb-client "0.1.1-SNAPSHOT"]
		[org.clojars.remleduff/javacv "20100416"]
		[net.java.dev.jna/jna "3.2.4"]]
 :dev-dependencies [[leiningen/lein-swank "1.2.0-SNAPSHOT"]]
 :namespaces [mcms.core]
 :main-class mcms.core
 :repositories {"dev.java.net" "http://download.java.net/maven/2/"})

(ns leiningen.run-db
  (:use [leiningen.compile :only [eval-in-project]]))

(defn run-db [project & args]
  (eval-in-project project
    `(do
       (require 'fleetdb.server)
       (fleetdb.server/-main "-f" "db.fdb"))))

(ns leiningen.run-mcms
  (:use [leiningen.compile :only [eval-in-project]]))

(defn run-mcms [project & args]
  (System/setProperty "jna.library.path" "OpenCV2.1")
  (eval-in-project project
    `(do
       (require 'mcms.core)
       (mcms.core/start-app)
       (println "Ready!"))))
