(ns mcms.nav
  (:use [net.cgrand.enlive-html]))


(defsnippet nav "mcms/nav.html" [:#nav] [current add search]
  [:.loggedin] (when current identity)
  [:#current]  (if current
		 (do->
		  (set-attr :href (str "/" current))
		  (content (str current)))
		 (do->
		  (set-attr :href "/")
		  (content "HOME")))
  [:.add-media] (when add identity)
  [:.search-media] (when search identity))

(defn die [& errors]
  (throw (Exception. (apply str errors))))

(defmulti url (fn [key &_] key))

(defmethod url :username [_ user] (str "/" user))

(defmethod url :home [_ _] "/")

(defmethod url :item [_ item] (str "/media/" item))
