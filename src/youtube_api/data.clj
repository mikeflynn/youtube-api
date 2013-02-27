(ns youtube-api.data
  "YouTube Data API functions"
  (:require [youtube-api.core :as apicore]))

(def base-url "https://www.googleapis.com/youtube/v3")

(defn fetch [endpoint params headers private]
"Fetch function that just returns the response body as a parsed map."
  (if-let [body (apicore/api-request (str base-url (if (.startsWith endpoint "/") endpoint (str "/" endpoint)))
                                     (into {} (for [[k v] params] {(name k) v}))
                                     headers
                                     private)]
    body
    (throw (Exception. "Error: Failed API request."))))

(defn fetch-list [endpoint params headers private limit & [data]]
"Recursive fetch function based on the 'items' field in the API response"
  (try
    (let [per (if (nil? limit) 25 (if (< limit 50) limit 50))
          body (fetch endpoint (assoc params "maxResults" per) headers private)]
      (if-let [items (get body "items")]
        (let [return (apply conj (if (nil? data) [] data) (if (nil? items) [] items))
              nextPageToken (get body "nextPageToken")
              nextLimit (- limit (count items))]
          (println (str (count items) "/" nextPageToken))
          (if (or (nil? nextPageToken) (>= 0 nextLimit))
              return
              (fetch-list endpoint
                          (assoc params "pageToken" nextPageToken)
                          headers
                          private
                          nextLimit
                          return)))
      (do
        (println "ERROR: Failed to get valid list API response.")
        (println body)
        data)))
    (catch Exception e (do (Thread/sleep 1000)
                           (fetch-list endpoint params headers private limit data)))))

(defn override [params overrides]
"Overrides values in the params map, if the override value isn't nil."
  (conj params (into {} (filter (comp not nil? val) overrides))))

(defn date [datetime] (apicore/date_format_iso8601 datetime))

(defmulti api (fn [& {:keys [endpoint action]
                      :or {endpoint nil action nil}}]
  action))

(defmethod api "list" [& {:keys [endpoint action params limit private headers]
                          :or {endpoint nil action "list" params {} headers {} limit 25 private false}}]
  (let [defaults {:part "id,snippet"}
        params (conj defaults params)]
    (fetch-list endpoint
                params
                headers
                (if private true false)
                limit
                [])))

(defmethod api "insert" [& {:keys [endpoint action params headers limit private]
                            :or {endpoint nil action "insert" params {} headers {}}}]
  false)

(defmethod api "update" [& {:keys [endpoint action params headers limit private]
                            :or {endpoint nil action "update" params {} headers {}}}]
  false)

(defmethod api "delete" [& {:keys [endpoint action params headers limit private]
                            :or {endpoint nil action "delete" params {} headers {}}}]
  false)

(defmethod api :default [endpoint action params & options]
  (throw (Exception. "Unsupported api action")))
