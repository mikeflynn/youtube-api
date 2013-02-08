# youtube-api

A Clojure library wrapper for the [v3 YouTube API](https://developers.google.com/youtube/v3/).

## Usage

Included in this library is a youtube-api namespace for each major part of the YouTube API (currently: data, analytics). The core namespace is just for shared methods and shouldn't be called directly.

If you want to call the channels list API, you would use the youtube-api.data/channels-list function. Pretty simple.

*Note: So far, only the list methods have been implemented.*

## Questions

### Why make this? Aren't there other YouTube API wrappers?

There are a few, but I didn't find any that used the new v3 API and most of them focused on uploading, while I started with the data first.

### Hey, this isn't idiomatic Clojure because you did this [...] and you should have done [...]!

That's not really a question, but you're probably right. I'd love to get better at Clojure, so comment, set a pull request, whatever, just help me out!

## License

Copyright © 2013 Mike Flynn

Distributed under the Eclipse Public License, the same as Clojure.
