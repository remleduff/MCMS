(ns mcms.collection
  (:use [net.cgrand.enlive-html]
	[mcms db media users]))

(deftemplate collection-template "mcms/collection-template.html" [username collection]
  [:.collection] (content (str username "'s Collection"))
  [:#add-media] (do-> (after (add-media-form (str "/" username))))
  [:#item] (content (map item collection)))

(defn show-collection [username media] (apply str (collection-template username media)))

(defn show-item [media] (apply str (emit* (item media))))

(defn user-collection [db username]   
  (let [uid (get-user-id db username)
	isbns (map #(get % "isbn") (owned db uid))]
    (get-items db isbns)))

(defn list-collection [db username]
  (show-collection username (user-collection db username)))

(defn add-to-collection 
  ([db {username :username, isbn :isbn :as item}]
     (let [uid (get-user-id db username)
	   isbn (if (integer? isbn) isbn (Integer/parseInt isbn))]
       (when (zero? (count-item db isbn)) (add-item db item))
       (db ["insert" "collection" {:id (next-id db "collection") :isbn isbn, :owner uid}])
       (list-collection db username))))