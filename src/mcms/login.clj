(ns mcms.login
  (:require [fleetdb.client :as fleetdb] [clojure.xml :as xml])
  (:use [mcms media nav camera collection] 
	[compojure]
	[clojure.contrib.duck-streams :only [copy]]
	[net.cgrand.enlive-html])
  (:import [java.io File]
           [javax.swing SwingUtilities]))
  
(defsnippet passwd-login-form "mcms/passwd-login.html" [:form]
  [destination]
  [:form] (set-attr :action destination))

(defsnippet face-detect-form "mcms/face-detect.html" [:form]
  [destination]
  [:form] (set-attr :action destination))

(deftemplate login-template "mcms/login-template.html" []
  [:#nav] (substitute (nav nil nil nil))
  [:#passwd-logging] (after (passwd-login-form "/login-passwd"))
  [:#face-detect] (after (face-detect-form "/login-face")))

(defn query-password 
  ([username]
        ["select" "users" {"where" ["=" :name username] "only" "passwd"}])
  ([db username]
     (first (db (query-password username)))))

(defn check-passwd [db username password]
  (= password (query-password db username)))
  
(defn show-loggedin [db {:keys [username selected]}]
  ;(println "Logged in" username) interferes!
  ;(reset! *current-user* username)
  ; Return a vector containing new session and the html
  [(session-assoc :current-user username)
  (redirect-to (str "/" username))])

;(defn show-tolog [db]
;  (if #_(= @*current-user* nil) (= current-user nil)
;  (login-template)
;  #_(show-loggedin db @*current-user*) (show-loggedin db current-user))) 

(defn login [session db]
  (if-let [current (:current-user session)]
    (show-loggedin db current)
    (login-template)))

(defn logout-user [session]
  #_(reset! *current-user* nil)
  (if (contains? session :current-user)
    [(session-dissoc :current-user)
    (redirect-to "/")]))
  
(defn get-detect-result [db]
  (let [login-promise (promise)]
    (SwingUtilities/invokeAndWait #(face-detect db login-promise))
    (show-loggedin db @login-promise)))
  
(defn face-login [db]
  (get-detect-result db)
  #_(if (= (get-detect-result db) 1)))
  
(defn passwd-login [db username password]
  (if (check-passwd db username password)
    (show-loggedin db {:username username})
    [(flash-assoc :error "Bad login info")
     (redirect-to "/")]))