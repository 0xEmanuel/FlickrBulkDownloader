package flickrbulkdownloader.core;

import com.flickr4java.flickr.FlickrException;

import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Logger;


public class CrawlerFactory
{
    public ICrawler createCrawler(Logger logger) throws IOException, FlickrException, SQLException
    {
        //logger.setLevel(Level.OFF);

        //logger.setLevel(Level.FINEST);
        //logger.log(Level.INFO, "ughh infoo");
        //logger.log(Level.SEVERE, "severrrrree");

        Configuration config = new Configuration();
        FlickrApi flickrApi = new FlickrApi(
                config.getAuthConfiguration(),
                logger
        );
        IDownloadHandler downloadHandler = new DownloadHandler(
                flickrApi,
                config.getDownloadHandlerConfiguration(),
                logger
        );
        IDatabaseHandler databaseHandler = new DatabaseHandler(
                config.getDatabaseConfiguration(),
                logger
        );
        ICrawler crawler = new Crawler(
                flickrApi,
                config.getCrawlerConfiguration(),
                downloadHandler,
                databaseHandler,
                logger
        );

        //databaseHandler.RemoveUserEntries();
        //databaseHandler.RemovePhotoEntries("");
        databaseHandler.RemovePhotoEntries();

        //databaseHandler.OutputUserEntries();
        //databaseHandler.OutputPhotoEntries("");
        databaseHandler.OutputPhotoEntries();

        return crawler;
    }
}
