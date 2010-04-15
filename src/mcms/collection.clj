(ns mcms.collection
  (:use [net.cgrand.enlive-html]))

(def *item-selector* (selector [:tr#item]))

(defsnippet item "mcms/main-template.html" *item-selector*
  [media] 
  [:.isbn] (content (str (get media "id")))
  [:.title] (content (get media "title"))
  [:.author] (content (get media "author"))
  [:.cover] (set-attr :src (str "/covers/" (get media "id"))))

(defsnippet book-form "mcms/addBook.html" (selector [:form])
  [destination]
  [:form] (set-attr :action destination))

(deftemplate collection-template "mcms/main-template.html" [username collection]
  [:.collection] (content (str username "'s Collection"))
  [:#add-book] (do-> (after (book-form "/media")))
  [:#item] (content (map item collection)))

(defn show-collection [username media] (apply str (collection-template username media)))

(defn show-item [media] (apply str (emit* (item media))))