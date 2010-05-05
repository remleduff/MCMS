(ns mcms.users
  (:require [fleetdb.client :as fleetdb])
  (:use [mcms collection db nav]
	[compojure]
	[clojure.contrib.duck-streams :only [copy]]
	[net.cgrand.enlive-html])
  (:import [java.io File]))

(defsnippet user-template "mcms/users-template.html" [:#item]
  [{:strs [name]}]
  [:.user]   (do-> 
	      (set-attr :href (str "/" name)) 
	      (content name)))

(defsnippet user-form "mcms/addUser.html" [:form]
  [destination]
  [:form] (set-attr :action destination))

(deftemplate users-template "mcms/users-template.html" [users]
  [:#nav] (substitute (nav nil nil nil))
  [:#item] (content (map user-template users))
  [:#add-user] (append (user-form "/users")))

;(defn add-user
;  ([db username]
;     (db ["insert" "users" {:id (next-id db "users"), :name username}])))

(defn add-user-passwd [db username password]
  (db ["insert" "users" {:id (next-id db "users"), :name username :passwd password}]))
     
(defn show-users [db]
  (apply str (users-template (users db))))