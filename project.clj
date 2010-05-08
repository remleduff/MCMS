(defproject MCMS "0.1.0-SNAPSHOT"
 :description "A media collection manager."
 :dependencies [[enlive "1.0.0-SNAPSHOT"]
                [compojure "0.3.2"]
                [fleetdb-client "0.1.1-SNAPSHOT"]
		[org.clojars.remleduff/javacv "20100416"]
		[net.java.dev.jna/jna "3.2.4"]]
 :dev-dependencies [[leiningen/lein-swank "1.2.0-SNAPSHOT"]]
 :namespaces [mcms.core]
 :main-class mcms.core
 :repositories {"dev.java.net" "http://download.java.net/maven/2/"})

(ns leiningen.run-mcms
  (:use [leiningen.compile :only [eval-in-project]]
	[clojure.contrib.java-utils :only [file]]
	[clojure.contrib.duck-streams :only [reader]])
  (:import [java.io File]))

(defn run-process [working-dir & args]
  (let [process-builder (doto (ProcessBuilder. (into-array String args)) 
			  (.directory (File. working-dir)))
	process (.start process-builder)
	runtime (Runtime/getRuntime)]
    (.addShutdownHook runtime (Thread. #(.destroy process)))
    (println (.readLine (reader (.getInputStream process))))))

(def java (.getCanonicalPath (file (System/getProperty "java.home") "bin" "java")))

(defn run-mcms [project & args]
  #_(run-process "db" java "-cp" "lib/*" "fleetdb.server" "-f" "db.fdb")
  (System/setProperty "jna.library.path" "OpenCV2.1/bin")
  (eval-in-project project
    `(do
       (require 'mcms.core)
       (mcms.core/start-app)
       (println "Ready!"))))
