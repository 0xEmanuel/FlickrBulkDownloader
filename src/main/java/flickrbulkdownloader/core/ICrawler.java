package flickrbulkdownloader.core;

import java.io.IOException;
import java.sql.SQLException;


public interface ICrawler
{
    void crawlAllPhotos(String userId) throws IOException, SQLException;
    boolean crawlPhoto(String photoId) throws IOException, SQLException;
    IDatabaseHandler getDatabaseHandler();
    void close();
}