# FlickrBulkDownloader

With this tool all media can be downloaded (via Flickr API) of multiple given Flickr users (e.g. for backup purposes). The downloads are logged in an internal database (HSQLDB), so the next time the program is run, they will not be repeated. OAuth is supported.

The tool implements a non-documented API method of Flickr that allows to download any media from any user in its original format. In the meantime (2020) the API method has unfortunately been deactivated, so the actual added value of this tool is lost. This was an unique feature back then. Therefore it will not be further developed. It still works, however, for the next best quality formats.

A bulk file downloader / crawler for Flickr.com

Features:
- Bulk Downloads: Download all media files of a specified user
- Download Database: Every downloaded file will have its own database entry. So you will have your own download history, that can be used to avoid to download files that were already crawled.
- Supports OAuth authentication
- Feature: Downloads media files in best quality format.

```
Usage:
 -c,--crawlall <crawlall=userid>            Pass an userId as argument to crawl everything
                                            
 -cs,--crawlsingle <crawlsingle=mediaId>    Pass a photoId/videoId as
                                            argument to crawl this single media
                                            
 -h,--help                                  show help
 
 -lp,--listphotos <listphotos=userid>       Lists all crawled photos from
                                            database. If userId specified,
                                            it will only list entries of that user.
                                            
 -lu,--listusers                            Lists all crawled users from database
                                            
 -rp,--removephotos <removephotos=userid>   Removes all crawled photos
                                            from database. If userId
                                            specified, it will only remove
                                            entries of that user.
                                                                                    
 -ru,--removeusers                          Removes all crawled users from database

```
