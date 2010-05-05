(ns mcms.db)

(defn next-id 
  ([table]
     ["count" table])
  ([db table]
     (inc (db (next-id table)))))

(defn owned
  ([uid]
     ["select" "collection" {"where" ["=" :owner uid]}])
  ([db uid]
     (db (owned uid))))

(defn get-user-id 
  ([username]
     ["select" "users" {"where" ["=" :name username]}])
  ([db username]
     (first (db ["select" "users" {"where" ["=" :name username] "only" "id"}]))))

(defn users 
  ([]
     ["select" "users"])
  ([db]
     (db (users))))

(defn get-owned [db username]
  (let [uid (get-user-id db username)
	isbns (map #(get % "isbn") (owned db uid))]
    isbns))