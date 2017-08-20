package flickrbulkdownloader;

import com.flickr4java.flickr.FlickrException;


import flickrbulkdownloader.cli.CommandLineInterface;
import flickrbulkdownloader.core.*;

import java.io.*;
import java.sql.SQLException;

import java.util.logging.Logger;


/*
DB Layout:

Processed:

1) photo_id | title | secret | nsid | date_upload | date_crawled | media

2) nsid | username | fullname | date_update | update_photo_id | date_last_time_crawled
*/

public class Program
{
    public static void main(String[] args) throws Exception
    {
        //turn off logger log4j of flickr4java
        org.apache.log4j.Logger.getRootLogger().setLevel(org.apache.log4j.Level.OFF);

        System.out.println("START");

        Logger logger = Logger.getLogger("bla");
        ICrawler crawler = new CrawlerFactory().createCrawler(logger);

        //testMain(crawler);

        new CommandLineInterface(args, crawler).parse();

        crawler.close(); //TODO NEVER FORGET THIS TO AVOID DATA LOSS
    }

    public static void testMain(ICrawler crawler) throws IOException, FlickrException, SQLException
    {
        String photoSetId = "72157685090055265";
        String userId = "39267545@N06";
        String photoId = "5107978412";
        //124841879@N08 //todo some do not allow original download?
        //56762560@N03
        //39267545@N06

        //getPhotoSetList(userId,apiKey,tokenValue);
        //getPhotoSets(userId,apiKey);
        //getGalleryList(userId,apiKey,"1");
        //getCollectionList(userId,apiKey);

        crawler.crawlAllPhotos(userId);

        //List<PhotoSet> photoSetList = crawler.getAllPhotosOrganizedInPhotoSets(userId);
        //Output.OutputPhotoSetList(photoSetList);

    }
}