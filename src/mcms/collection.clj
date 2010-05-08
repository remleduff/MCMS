(ns mcms.collection
  (:use [net.cgrand.enlive-html]
	[mcms db media nav users]
	[compojure]))

(defn user-collection [db username]   
  (get-media db (get-owned db username)))

(defn add-to-collection
  ([db {username :username, isbn :isbn :as item}]
     (let [uid (get-user-id db username)]
       (when (zero? (count-item db isbn)) (add-item db item))
       (db ["insert" "collection" {:id (str isbn uid) :isbn isbn, :owner uid}])
       [(flash-assoc :message (str "Added item: " isbn))
	(redirect-to (str "/" username))])))

(defn delete-from-collection
  ([uid isbn]
     ["delete" "collection" {"where" ["and" ["=" :owner uid] ["=" :isbn isbn]]}])
  ([db username isbn]
     (db (delete-from-collection (get-user-id db username) isbn))
     [(flash-assoc :message (str "Deleted item: " isbn))
      (redirect-to (str "/" username))]))

(defn show-item [media] (apply str (emit* (item nil media))))

(defn show-user-collection [db current username]
  (show-media db current (user-collection db username) {:add true :username username}))

(defn delete? [params]
  (= (:_method params) "delete"))

(defn mod-collection [db session params]
  (let [user (:current-user session)
	isbn (:isbn params)]
    (valid not-empty "Must be logged in and provide isbn" user isbn)
    (cond 
     (delete? params) 
       (delete-from-collection db user isbn)
     :else 
       (add-to-collection db (assoc params :username user)))))