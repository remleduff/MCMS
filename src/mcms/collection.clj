(ns mcms.collection
  (:use [net.cgrand.enlive-html]
	[mcms db media nav users]
	[compojure]))

(deftemplate collection-template "mcms/media-template.html" [current username collection]
  [:#nav] (substitute (nav current true false)) 
  [:.current] (do->
	    (content (str current))
	    (set-attr :href (str current)))
  [:.collection] (content (str username "'s Collection"))
  [:#add-media] (do-> (append (add-media-form (str "/" username))))
  [:#search-media] nil
  [:#tableheader :.rank]  (when (get collection "rank") identity)
  [:#item] (substitute (map (partial item current) (map #(assoc % :owned true) collection))))


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

(defn show-collection [current username media] (apply str (collection-template current username media)))

(defn show-item [media] (apply str (emit* (item nil media))))

(defn show-user-collection [db current username]
  (show-collection current username (user-collection db username)))

(defn delete? [params]
  (= (:_method params) "delete"))

(defn valid [pred msg & args]
  (when (every? pred args)
    (die (str msg ":" args))))

(defn mod-collection [db session params]
  (let [user (:current-user session)
	isbn (:isbn params)]
    (valid nil? "Must be logged in and provide isbn" user isbn)
    (cond 
     (delete? params) 
       (delete-from-collection db user isbn)
     :else 
       (add-to-collection db (assoc params :username user)))))