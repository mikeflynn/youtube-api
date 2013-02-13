(ns youtube-api.core
  "YouTube API Clojure wrapper"
  (:use [clj-time.coerce :only [from-string]]
  	    [clj-time.core :only [now]])
  (:require [clj-http.client :as http]
  	        [clojure.data.json :as json]
  	        [clj-time.format :as time-format]))

(defn set-api-key [key]
  (def api-key key))

(defn set-client-id [cid]
  (def client-id cid))

(defn set-client-secret [secret]
  (def client-secret secret))

(defn set-user [refresh_token ip_address & [access_token]]
  (def user (merge
    {:refresh_token false :access_token false :timeout 0 :ip_address false}
    {:refresh_token refresh_token
     :access_token access_token
     :ip_address ip_address
     :timeout 0})))

(defn date_format_iso8601 [date]
	(let [ISO8601 (time-format/formatter "yyyy-MM-dd'T'HH:mm:ss.SSSZ")]
		(time-format/unparse ISO8601 (if (nil? date) (now) (from-string date)))))

(defn date_format_yyyymmdd [date]
  (let [YYYYMMDD (time-format/formatter "yyyy-MM-dd")]
    (time-format/unparse YYYYMMDD (if (nil? date) (now) (from-string date)))))

(defn query-string [params]
	(clojure.string/join "&" (for [[k v] params] (str (name k) "=" v))))

(defn get-access-token []
	(if (or (false? (:access_token user)) (< (:timeout user) (System/currentTimeMillis)))
		(if-let [response (http/post "https://accounts.google.com/o/oauth2/token"
			                {:form-params {
	                         :client_id client-id
			                 :client_secret client-secret
			                 :refresh_token (:refresh_token user)
			                 :grant_type "refresh_token"}})]
		  (let [authresp (json/read-str (:body response))]
		  	(def user (assoc user :access_token (get authresp "access_token") :timeout (+ (System/currentTimeMillis) (* (int (get authresp "expires_in")) 1000)))))
		  (throw (Exception. "Error: No authorization response!"))))
	user)

(defn inject-headers [headers private]
  (into headers
    (into {"User-Agent" "Ni3ls3n"}
      (if private {"Authorization" (str "Bearer " (:access_token (get-access-token)))}))))

(defn api-request [endpoint params headers private]
  "Requests feed, handles recursing for pagination, throws exception for no authorization."
  (if-let [response (http/get endpoint
  	                 {:headers (inject-headers headers private)
                      :throw-exceptions false
                      :query-params (assoc (into {} (filter (comp not nil? val) params)) "key" api-key)})]
    (try
      (let [body (json/read-str (:body response))
    	      error (get body "error")]
    	  (if (nil? error)
          body
          (do (println "ERROR: " error) false)))
      (catch Exception e (do
                           (println (:body response))
                           {:items nil})))
    (throw (Exception. "Error: No API response!"))))
