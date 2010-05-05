(ns mcms.core
  (:require [fleetdb.client :as fleetdb]
	    [net.cgrand.enlive-html :as enlive])
  (:use [mcms covers media collection nav users entrez camera login]
	[compojure]))

(defonce *db* (atom nil))
(defonce *app* (atom nil))

(def pages 
     {:root "/",
      :media "/media",
      :collection "/collection",
      :users "/users"})

(defn page [id] 
  (pages id))

(defroutes mcms-routes
  (GET "/"
        (login session @*db*))
  (POST "/login-face"
        (face-login @*db*))
  (POST "/login-passwd"
        (passwd-login @*db* (:username params) (:password params)))
  (GET "/logout"
        (logout-user session)
	(redirect-to (page :root)))
  (GET "/covers/:isbn"
       (serve-file "covers" (:isbn params)))
  (POST "/covers/:isbn"
	(add-cover (:isbn params) (get-in params [:cover :tempfile])))
  (GET "/media"
       (show-media @*db* (:current-user session)))
  (POST "/media"
	(add-item @*db* params)
	(redirect-to (page :media)))
  (POST "/search"
	(search-media @*db* params session))
  (GET "/users"
       (show-users @*db*))
  (POST "/users"
	(add-user-passwd @*db* (:username params) (:password params))
	[(flash-assoc :message (str "Added user " (:username params))) 
	 (redirect-to (page :users))])
  (POST "/:username"
	(mod-collection @*db* session params))
  (POST "/:username/:isbn"
	(mod-collection @*db* session params))
  (GET "/:username"
       (show-user-collection @*db* (:current-user session) (:username params)))
  (GET "*"
       (or (serve-file "public" (:* params)) :next))
  (ANY "*"
       [404 "Page Not Found"]))

(defn add-to-body [html item]
  (apply str 
	 (enlive/emit* 
	  (enlive/at (enlive/html-snippet html) 
		     [:body] (enlive/prepend item)))))


; (assoc response :body (str request (:body (apply str response))))

(defn with-flash-message [handler]
  (fn [request]
    (let [response (handler request)]
      (or
       (when-let [error (get-in request [:flash :error])]
	 (assoc response :body (add-to-body (:body response) error)))
       (when-let [message (get-in request [:flash :message])]
	 (assoc response :body (add-to-body (:body response) message)))
       response))))

(defn with-logging [handler]
  (fn [request]
    (let [response (handler request)]
      (assoc response :body (add-to-body (:body response) request)))))

(decorate mcms-routes (with-multipart))
;(decorate mcms-routs (with-logging))
(decorate mcms-routes (with-flash-message))
(decorate mcms-routes (with-session :memory))


;; ========================================
;; The App
;; ========================================


(defn start-app []
  (if @*app* (stop @*app*))
  (compute-cover-histograms)
  (reset! *db* (fleetdb/connect {:host "127.0.0.1", :port 3400}))
  (reset! *app* (run-server {:port 8080}
                            "/*" (servlet mcms-routes))))
(defn stop-app []
  (when @*app* (stop @*app*)))


