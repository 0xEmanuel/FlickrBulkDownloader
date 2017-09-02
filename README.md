# FlickrBulkDownloader

Status: Still in Development (no release yet)

A bulk file downloader / crawler for Flickr.com

Features:
- Bulk Downloads: Download all media files of a specified user
- Download Database: Every downloaded file will have its own database entry. So you will have your own download history, that can be used to avoid to download files that were already crawled.
- Supports OAuth authentication
- Unique Feature: Downloads the ORIGINAL files (videos and pictures). This tool uses undocumented API-method with that you can download the original media file. Thus you can backup your whole Flickr Account.

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