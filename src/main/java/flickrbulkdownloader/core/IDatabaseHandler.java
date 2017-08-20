package flickrbulkdownloader.core;


import com.flickr4java.flickr.people.User;
import flickrbulkdownloader.extensions.Photo;

import java.sql.SQLException;

public interface IDatabaseHandler
{
    void closeDbConnection();
    void insertPhoto(Photo photo) throws SQLException;
    void insertUserWithCheck(User user);
    boolean existsPhoto(Photo photo);
    void RemoveUserEntries() throws SQLException;
    void RemovePhotoEntries() throws SQLException;
    void RemovePhotoEntries(String userId) throws SQLException;
    void OutputUserEntries() throws SQLException;
    void OutputPhotoEntries() throws SQLException;
    void OutputPhotoEntries(String userId) throws SQLException;

}
