package flickrbulkdownloader.core;

import java.io.IOException;
import java.sql.SQLException;


public interface ICrawler
{
    void crawlAllPhotosByUserId(String userId) throws IOException, SQLException;
    void crawlAllPhotosByUsername(String username) throws IOException, SQLException;
    boolean crawlPhoto(String photoId) throws IOException, SQLException;
    IDatabaseHandler getDatabaseHandler();
    void close();
}
