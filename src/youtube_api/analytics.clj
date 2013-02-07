(ns youtube-api.analytics
  "YouTube Analytics API functions"
  (:require [youtube-api.core :as apicore]))

(def base-url "https://www.googleapis.com/youtube/analytics/v1")

(defn fetch [endpoint params limit headers private data & [n]]
  (if-let [body (core/api-request (str base-url endpoint)
  	                              (assoc params "max-results" limit "alt" "json")
  	                              headers
  	                              private)
           return (apply conj data (get body "items"))
           page (if (nil? n) 1 n)]
    (if (nil? nextPageToken)
      return
      (fetch (str base-url endpoint) (assoc params "start-index" (* limit page)) limit headers private return page))))

; API endpoint functions

(defn report [metrics & {:keys [channel contentOwner start-date end-date dimensions filters sorting max-results]
                              :or {channel nil
                              	   contentOwner nil
                              	   start-date nil
                              	   end-date nil
                              	   dimensions nil
                              	   filters nil
                              	   sorting nil
                              	   max-results 5}
                              :as argmap}]
"https://developers.google.com/youtube/v3/docs/channels/list"
  (if (and (nil? channel) (nil? contentOwner))
    (throw (Exception. "Error: Filter param missing (channel, contentOwner)!"))
    (apicore/api-request "/reports" {"metrics" metrics
    	                             "start-date" (if (nil? start-date) start-date (core/date_format start-date))
    	                             "end-date" (if (nil? publishedAfter) publishedAfter (core/date_format publishedAfter))
    	                             "ids" (if (not (nil? channel)) (str "channel==" channel) (if (not (nil? contentOwner)) (str "contentOwner==" contentOwner) "ERROR"))
                                     "dimensions" dimensions
                                     "filters" filters
                                     "sort" sort} max-results {} true [])))