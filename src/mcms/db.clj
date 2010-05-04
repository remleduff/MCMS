
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