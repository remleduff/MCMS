(ns mcms.nav
  (:use [net.cgrand.enlive-html]))

(def pages 
     {:root "/",
      :media "/media",
      :collection "/collection",
      :users "/users"})

(defn page [id] 
  (pages id))

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

(defn valid [pred msg & args]
  (when-not (every? pred args)
    (die (str msg ":" args))))