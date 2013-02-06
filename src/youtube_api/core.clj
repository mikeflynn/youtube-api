(ns youtube-api.core
  "YouTube API Clojure wrapper"
  (:use [clj-time.coerce :only [from-string]]
  	    [clj-time.core :only [now]])
  (:require [clj-http.client :as http]
  	        [clojure.data.json :as json]
  	        [clj-time.format :as time-format]))

(def base-url "https://www.googleapis.com/youtube/v3")
(def api-key "AIzaSyA0Q9pv2p0MlYAWDlmyPcN19o59W31ytTI")
(def client-id "320722306436-p6qjg69jvomcda1j77a0nee1uo40omia.apps.googleusercontent.com")
(def client-secret "PTBLJmrqJL4oENO1PPOAe2To")

(def user {
	:refresh_token "1/7qU7y2w3hsJRlRUlIjwaY7enunqRRSLrjJ8j8dDvaC8"
	:access_token false
	:timeout 0})

(defn set-user [refresh_token access_token ip_address]
  (def user (merge
    {:refresh_token false :access_token false :timeout 0 :ip_address false}
    {:refresh_token refresh_token
     :access_token access_token
     :ip_address ip_address
     :timeout 0})))

(defn date_format [date]
	(let [ISO8601 (time-format/formatter "yyyy-MM-dd'T'HH:mm:ss.SSSZ")]
		(time-format/unparse ISO8601 (if (nil? date) (now) (from-string date)))))

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

(defn api-request [endpoint params limit headers private data]
  "Requests feed, handles recursing for pagination, throws exception for no authorization."
  (into headers
  	(into {"User-Agent" "Ni3ls3n"}
  		(if private {"Authorization" (str "Bearer " (:access_token (get-access-token)))})))

  (if-let [response (http/get (str base-url endpoint)
  	                 {:headers headers
                      :throw-exceptions false
                      :query-params (assoc (into {} (filter (comp not nil? val) params)) "maxResults" limit "key" api-key)})]
    (let [body (json/read-str (:body response))
    	  nextPageToken (get body "nextPageToken")
    	  error (get body "error")]
    	(if (nil? error)
    		(let [return (apply conj data (get body "items"))]
    		  (if (nil? nextPageToken)
    		  	  return
    			  (api-request endpoint (assoc params "pageToken" nextPageToken) limit return)))
    	    error))
    (throw (Exception. "Error: No API response!"))))
