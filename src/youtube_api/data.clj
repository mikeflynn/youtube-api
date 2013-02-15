(ns youtube-api.data
  "YouTube Data API functions"
  (:require [youtube-api.core :as apicore]))

(def base-url "https://www.googleapis.com/youtube/v3")

(defn fetch [endpoint params headers private]
  (if-let [body (apicore/api-request (str base-url endpoint)
                                     params
                                     headers
                                     private)]
    body
    (throw (Exception. "Error: Failed API request."))))

(defn fetch-list [endpoint params headers private limit & [data]]
"Recursive fetch function based on the 'items' field in the API response"
  ;(println (str "Limit: " limit "\n"))
  (let [per (if (nil? limit) 25 (if (< limit 50) limit 50))
        body (fetch endpoint (assoc params "maxResults" per) headers private)]
    (if-let [items (get body "items")]
      (let [return (apply conj (if (nil? data) [] data) (if (nil? items) [] items))
            nextPageToken (get body "nextPageToken")]
        ;(println (str "Result Count: " (count items) "\n"))
        ;(println (str "Next Page: " nextPageToken "\n"))
        (if (nil? nextPageToken)
            return
            (fetch-list endpoint
                        (assoc params "pageToken" nextPageToken)
                        headers
                        private
                        (- limit (count items))
                        return)))
    (do
      (println "ERROR: Failed to get valid list API response.")
      (println body)
      data))))

; API endpoint functions

(defn channels-list [parts & {:keys [categoryId id mine mySubscribers maxResults]
                              :or {categoryId nil id nil mine nil mySubscribers nil maxResults 5}
                              :as argmap}]
"https://developers.google.com/youtube/v3/docs/channels/list"
  (if (and (nil? categoryId) (nil? id) (nil? mine) (nil? mySubscribers))
    (throw (Exception. "Error: Filter param missing (id, categoryId, mine mySubscribers)!"))
    (fetch-list "/channels" {"part" parts "id" id "mine" mine "mySubscribers" mySubscribers} {} true maxResults [])))

(defn activities-list [parts & {:keys [channelId home mine publishedAfter publishedBefore maxResults]
                              :or {channelId nil home nil mine nil publishedAfter nil publishedBefore nil maxResults 5}
                              :as argmap}]
"https://developers.google.com/youtube/v3/docs/activities/list"
  (if (and (nil? channelId) (nil? home) (nil? mine))
    (throw (Exception. "Error: Filter param missing (channelId, home, mine)!"))
    (fetch-list "/activities" {"part" parts
                                     "channelId" channelId
                                     "home" home
                                     "mine" mine
                                     "publishedAfter" (if (nil? publishedAfter) publishedAfter (apicore/date_format_iso8601 publishedAfter))
                                     "publishedBefore" (if (nil? publishedBefore) publishedBefore (apicore/date_format_iso8601 publishedBefore))} {} true maxResults [])))

(defn guideCatagories-list [parts & {:keys [id regionCode hl maxResults]
                              :or {id nil regionCode nil hl nil maxResults 5}
                              :as argmap}]
"https://developers.google.com/youtube/v3/docs/guideCategories/list"
  (if (and (nil? id) (nil? regionCode))
    (throw (Exception. "Error: Filter param missing (id, regionCode)!"))
    (fetch-list "/guideCategories" {"part" parts
                               "id" id
                               "regionCode" regionCode} {} true maxResults [])))

(defn playlistItems-list [parts & {:keys [id playlistId maxResults]
                              :or {id nil playlistId nil maxResults 5}
                              :as argmap}]
"https://developers.google.com/youtube/v3/docs/playlistItems/list"
  (if (and (nil? id) (nil? playlistId))
    (throw (Exception. "Error: Filter param missing (id, playlistId)!"))
    (fetch-list "/playlistItems" {"part" parts
                                             "id" id
                                             "playlistId" playlistId} {} true maxResults [])))

(defn playlists-list [parts & {:keys [id channelId mine maxResults]
                              :or {id nil playlistId nil mine nil maxResults 5}
                              :as argmap}]
"https://developers.google.com/youtube/v3/docs/playlists/list"
  (if (and (nil? id) (nil? channelId) (nil? mine))
    (throw (Exception. "Error: Filter param missing (channelId, id, playlistId)!"))
    (fetch-list "/playlists" {"part" parts
                                             "channelId" channelId
                                             "id" id
                                             "mine" mine} {} true maxResults [])))

(defn subscriptions-list [parts & {:keys [id channelId mine forChannelId order maxResults]
                              :or {id nil playlistId nil mine nil forChannelId nil order "alphabetical" maxResults 5}
                              :as argmap}]
"https://developers.google.com/youtube/v3/docs/subscriptions/list"
  (if (and (nil? id) (nil? channelId) (nil? mine))
    (throw (Exception. "Error: Filter param missing (channelId, id, mine)!"))
    (fetch-list "/subscriptions" {"part" parts
                                           "channelId" channelId
                                           "id" id
                                           "mine" mine
                                           "order" (if (= -1 (.indexOf '("alphabetical" "relevance" "unread") order)) "alphabetical" order)
                                           "forChannelId" forChannelId} {} true maxResults [])))

(defn videoCategories-list [parts & {:keys [id regionCode hl maxResults]
                              :or {id nil regionCode nil hl nil maxResults 5}
                              :as argmap}]
"https://developers.google.com/youtube/v3/docs/videoCategories/list"
  (if (and (nil? id) (nil? regionCode))
    (throw (Exception. "Error: Filter param missing (id, regionCode)!"))
    (fetch-list "/videoCategories" {"part" parts
                                             "id" id
                                             "regionCode" regionCode}  {} true maxResults [])))

(defn videos-list [parts id]
"https://developers.google.com/youtube/v3/docs/videos/list"
    (fetch-list "/videos" {"part" parts "id" id} {} true nil []))

(defn search-list [parts & {:keys [relatedToVideoId
                                   channelId
                                   order
                                   publishedAfter
                                   publishedBefore
                                   q
                                   regionCode
                                   topicId
                                   type
                                   videoCaption
                                   videoCategoryId
                                   videoDefinition
                                   videoDimension
                                   videoDuration
                                   videoEmbeddable
                                   videoLicense
                                   videoSyndicated
                                   maxResults]
                              :or {relatedToVideoId nil
                                   channelId nil
                                   order "date"
                                   publishedAfter nil
                                   publishedBefore nil
                                   q nil
                                   regionCode nil
                                   topicId nil
                                   type "video,channel,playlist"
                                   videoCaption "any"
                                   videoCategoryId nil
                                   videoDefinition "any"
                                   videoDimension "any"
                                   videoDuration "any"
                                   videoEmbeddable "any"
                                   videoLicense "any"
                                   videoSyndicated "any"
                                   maxResults 5}
                              :as argmap}]
"https://developers.google.com/youtube/v3/docs/search/list"
    (fetch-list "/search" {"part" parts
                           "relatedToVideoId" relatedToVideoId
                           "channelId" channelId
                           "order" order
                           "publishedAfter" (if (nil? publishedAfter) publishedAfter (apicore/date_format_iso8601 publishedAfter))
                           "publishedBefore" (if (nil? publishedBefore) publishedBefore (apicore/date_format_iso8601 publishedBefore))
                           "q" q
                           "regionCode" regionCode
                           "topicId" topicId
                           "type" type
                           "videoCaption" (if (= -1 (.indexOf '("any" "closedCaption" "none") videoCaption)) "any" videoCaption)
                           "videoCategoryId" nil
                           "videoDefinition" (if (= -1 (.indexOf '("any" "high" "standard") videoCaption)) "any" videoCaption)
                           "videoDimension" (if (= -1 (.indexOf '("any" "2d" "3d") videoCaption)) "any" videoCaption)
                           "videoDuration" (if (= -1 (.indexOf '("any" "long" "medium" "short") videoCaption)) "any" videoCaption)
                           "videoEmbeddable" (if (= -1 (.indexOf '("any" "true") videoCaption)) "any" videoCaption)
                           "videoLicense" (if (= -1 (.indexOf '("any" "creativeCommon" "youtube") videoCaption)) "any" videoCaption)
                           "videoSyndicated" (if (= -1 (.indexOf '("any" "true") videoCaption)) "any" videoCaption)
                           } {} true maxResults []))
