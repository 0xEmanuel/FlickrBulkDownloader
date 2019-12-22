package flickrbulkdownloader.core;

import flickrbulkdownloader.extensions.ApiCallInvalidException;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;


public interface ICrawler
{
    void crawlAllPhotos(String userIdentification) throws IOException, SQLException, ParseException, ApiCallInvalidException;
    boolean crawlPhoto(String photoId) throws IOException, SQLException, ApiCallInvalidException;
    IDatabaseHandler getDatabaseHandler();
    void close();
}
