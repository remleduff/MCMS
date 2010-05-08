(ns mcms.core
  (:require [fleetdb.client :as fleetdb]
	    [net.cgrand.enlive-html :as enlive]
	    [compojure.http.response :as response])
  (:use [mcms covers media collection nav users entrez camera login]
	[compojure]
	[clojure.contrib pprint seq-utils]))

(defonce *db* (atom nil))
(defonce *app* (atom nil))

(defroutes mcms-routes
  (GET "/"
        (login session @*db*))
  (POST "/login-face"
        (face-login @*db*))
  (POST "/login-passwd"
	(passwd-login @*db* (:username params) (:password params)))
  (GET "/logout"
        (logout-user session))
  (GET "/covers/:isbn"
       (serve-file "covers" (:isbn params)))
  (POST "/covers/:isbn"
	(add-cover (:isbn params) (get-in params [:cover :tempfile])))
  (GET "/media"
       (show-media @*db* (:current-user session) {:add true :search true}))
  (POST "/media"
	(add-item @*db* params))
  (POST "/search"
	(search-media @*db* params session))
  (GET "/users"
       (show-users @*db*))
  (POST "/users"
	(add-user-passwd @*db* (:username params) (:password params)))
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
  (let [snippet (apply enlive/html-snippet html)
	transformed (enlive/at snippet
			       [:body] (enlive/prepend item))
	str (apply str (enlive/emit* transformed))]
    str))

(defn with-flash-message [handler]
  (fn [request]
    (let [response (handler request)]
      (or
       (when-let [error (get-in request [:flash :error])]
	 (assoc response :body (add-to-body (:body response) error)))
       (when-let [message (get-in request [:flash :message])]
	 (assoc response :body (add-to-body (:body response) message)))
       response))))

(defn with-caught-exception [handler]
  (fn [request]
    (try (handler request)
	 (catch Throwable e
	     (.printStackTrace e)
	     (response/create-response request 
				       [(flash-assoc :error (.getMessage e)) 
					(redirect-to (or (get-in request [:headers "referer"]) "/"))])))))

(defn with-logging [handler]
  (fn [request]
    (let [response (handler request)]
      (println "->" request "<-" response)
      response)))

;(decorate mcms-routes (with-logging))
(decorate mcms-routes (with-caught-exception))
(decorate mcms-routes (with-flash-message))
(decorate mcms-routes (with-multipart))
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


