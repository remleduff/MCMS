(ns mcms.media
  (:require [fleetdb.client :as fleetdb] [clojure.xml :as xml])
  (:use [mcms covers db entrez nav] 
	[compojure]
	[clojure.contrib.duck-streams :only [copy]]
	[net.cgrand.enlive-html])
  (:import [java.io File]
	   [java.util.regex Pattern]))

(defsnippet add-media-form "mcms/addMedia.html" [:form]
  [destination]
  [:form] (set-attr :action destination))

(defsnippet search-media-form "mcms/searchMedia.html" [:form]
  [destination]
  [:form] (set-attr :action destination))

(defsnippet add-collection-option "mcms/media-template.html" [:#add] 
  [destination]
  [:form] (set-attr :action destination))

(defsnippet delete-collection-option "mcms/media-template.html" [:#delete]
  [destination]
  [:form] (set-attr :action destination))

(defsnippet item "mcms/media-template.html" [:#item] [current username {:strs [id title author rank] :keys [owned]}]
  [:.isbn] (do->
	    (content (str id))
	    (set-attr :href (str "http://www.librarything.com/isbn/" id)))
  [:.title] (content title)
  [:.author] (content author)
  [:.cover] (set-attr :href (str "/covers/" id))
  [:.cover :img] (set-attr :src (str "/covers/" id))
  [:.action] (when current 
	       (content 
		(cond
		 (and owned (= username current))
		   (delete-collection-option (str "/" current "/" id))
                 owned
		   nil
		 :else
		   (add-collection-option (str "/" current "/" id)))))
  [:.rank] (when rank (content (str (- 1 rank)))))

(defn has-rank? [collection] 
  (get (first collection) "rank"))

(deftemplate media-template "mcms/media-template.html" 
                                  [current collection {:keys [add search username]}]
  [:#nav] (substitute (nav current add search))
  [:.current] (do->
	       (content (str current))
	       (set-attr :href (str current)))
  [:.collection] (when username (content (str username "'s Collection")))
  [:#add-media] (when add (do-> (append (add-media-form "/media"))))
  [:#search-media] (when search (do-> (append (search-media-form "/search"))))
  [:.rank]  (when (has-rank? collection) identity)
  [:.class]  (when (has-rank? collection) identity)
  [:#item] (substitute (map (partial item current username) collection)))

(defn count-item 
  ([isbn]
     ["count" "media" {"where" ["=" :id isbn]}])
  ([db isbn]
     (db (count-item isbn))))

(defn add-item [db {:keys [isbn author title cover] :as item}]
  (valid not-empty "Must specify ISBN" isbn)
  (let [author (get-author isbn)
        title (get-title isbn)
        cover-source (get-cover isbn cover)]
    (db ["checked-write"
	 (count-item isbn) 0 ; Don't insert the book if its ISBN already exists
	 ["insert" "media" {:id isbn :author author :title title}]])
    (add-cover isbn cover-source)
    [(flash-assoc :message (str "Added item: " isbn)) 
     (redirect-to (page :media))]))

(defn- get-items
  ([isbns]
     ["select" "media" {"where" ["in" :id (vec isbns)]}])
  ([db isbns]
     (db (get-items isbns))))

(defn get-media 
  ([]
     ["select" "media"])
  ([db]
     (db (get-media)))
  ([db isbns]
     (db (get-items isbns))))

(defn show-media 
  ([db current options]
     (show-media db current (get-media db) options))
  ([db current media options]
     (let [isbns (get-owned db current)
	   owned-items (for [{:strs [id] :as item} media]  
			 (assoc item :owned (some #(= id %) isbns)))]
       (media-template current owned-items options))))


(defn search-covers [coverfile]
  (when (.exists coverfile)
    (for [[k v] (search-cover coverfile)]
      {"id" k, "rank" v})))

(defn search-text [db query]
  (let [pattern (Pattern/compile query Pattern/CASE_INSENSITIVE)]
    (set (for [item (get-media db), 
	   val (vals item)
	   :when (and val 
		      (re-find pattern val))] 
       item))))

;; (let [search-results 
;; 	  user (or (:user params) (:current-user session))
;; 	  media (get-media db (keys search-results))
;; 	  ranks (vals search-results)]
;;       )


(defn search-media [db params session]
  (let [cover-results (search-covers (get-in params [:cover :tempfile]))
	text-results (search-text db (:query params))
	media (if cover-results 
		(sort-by #(get % "rank") (clojure.set/join cover-results text-results))
		text-results)
	current-user (:current-user session)]
    (show-media db current-user media {})))