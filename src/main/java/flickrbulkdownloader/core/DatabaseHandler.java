package flickrbulkdownloader.core;

import flickrbulkdownloader.tools.Util;
import flickrbulkdownloader.extensions.Photo;
import com.flickr4java.flickr.people.User;


import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


public class DatabaseHandler implements IDatabaseHandler
{
    private final static String DB_PROTOCOL = "jdbc:hsqldb:file:";
    private static String DB_URL;


    private final static String _createTablePhotos = "CREATE TABLE Photos" +
            " (photo_id bigint, title varchar(255), secret varchar(255)," +
            " user_id varchar(255),media varchar(255), isOriginal varchar(255), date_upload varchar(255),date_crawled varchar(255));";

    private final static String _createTableUsers = "CREATE TABLE Users" +
            " (user_id varchar(255), username varchar(255), realname varchar(255), photosCount int," +
            " date_crawled varchar(255));";

    private Connection _dbConnection;
    private Logger _logger;


    public DatabaseHandler(Configuration.DatabaseConfiguration databaseConfig,
                           Logger logger)
    {
//        Server server = new Server();
//        server.setDatabaseName(0, "db");
//        server.setDatabasePath(0, "file:" + DB_DIR_PATH + DB_NAME);
//        server.start();
//        server.stop();
        DB_URL = DB_PROTOCOL + databaseConfig.getDbDirPath() + databaseConfig.getDbName();

        _logger = logger;
        _dbConnection = createDbConnection();
        createAllTablesWithChecks();
    }

    private Connection createDbConnection()
    {
        Connection connection = null;
        try
        {
            Class.forName("org.hsqldb.jdbcDriver");
            connection = DriverManager.getConnection(DB_URL, "SA", ""); //UserId: SA, Password: <empty>
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return connection;
    }


    public void closeDbConnection()
    {
        if(_dbConnection == null)
            return;

        try
        {
            _dbConnection.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }


    private void createTableWithChecks(String createTableStatement, String tableName)
    {
        String checkTableNameExistsStatement = "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE table_name = '"+tableName+"'" ;
        try
        {
            ResultSet resultSet = executeStatement(checkTableNameExistsStatement,new ArrayList<Object>(), true);
            if(resultSet.next())
            {
                _logger.log(Level.INFO, "tableName = " + tableName + " already exists! Do not create.");
                return;
            }
            resultSet.close();
            executeStatement(createTableStatement,new ArrayList<Object>(), false);
            _logger.log(Level.INFO, "create tableName = " + tableName);

        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }

    }


    /*
        Setup database tables
     */
    private void createAllTablesWithChecks()
    {
        createTableWithChecks(_createTablePhotos, "PHOTOS"); //Needs to be in upper case!
        createTableWithChecks(_createTableUsers, "USERS");
    }


    private ResultSet executeStatement(String preparedQuery, List<Object> args, boolean generateResultSet) throws SQLException
    {
        PreparedStatement stmt  = _dbConnection.prepareStatement(preparedQuery);

        int i = 1;
        for(Object arg : args) //set parameters for preparedStatement
        {
            if (arg instanceof String)
                stmt.setString(i, (String)arg);
            else if (arg instanceof Integer)
                stmt.setInt(i, (Integer)arg);
            else if (arg instanceof Boolean)
                stmt.setBoolean(i, (Boolean)arg);
            else
                _logger.log(Level.SEVERE, "Unknown argument type for sql query!");
            i++;
        }

        _logger.log(Level.INFO, "Executed Query = " + Util.buildSqlQuery(preparedQuery, args));

        ResultSet resultSet = null;
        if(generateResultSet)
            resultSet = stmt.executeQuery();
        else
            stmt.executeUpdate(); //this is for inserts and deletes, since those commands do not return ResultSet
        stmt.close();

        return resultSet;
    }

    public void insertPhoto(Photo photo) throws SQLException
    {
        String insertPhotoStatement = "INSERT INTO Photos (photo_id, title, secret, user_id, media, isOriginal, date_upload, date_crawled) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?);";

        List<Object> args = new ArrayList<Object>();
        args.add(photo.getId());
        args.add(photo.getTitle().replaceAll("'",""));
        args.add(photo.getSecret());
        args.add(photo.getUserId());
        args.add(photo.getMedia());
        args.add(Boolean.toString(photo.getIsOriginalAvailable() ) );
        args.add(photo.getDateAdded().toString());
        args.add(Util.createTimestamp());
        executeStatement(insertPhotoStatement, args, false);
    }


    private void insertUser(User user) throws SQLException
    {

        String insertUserStatement = "INSERT INTO Users (user_id, username, realname, photosCount) " +
                "VALUES (?, ?, ?, ?);";

        List<Object> args = new ArrayList<Object>();
        args.add(user.getId());
        args.add(user.getUsername().replaceAll("'",""));
        args.add(user.getRealName());
        args.add(user.getPhotosCount());
        //args.add(Util.createTimestamp());
        //args.add(false);

        executeStatement(insertUserStatement, args, false);
    }


//    public void insertPhotoWithChecks(Photo photo)
//    {
//        boolean exists = existsPhoto(photo); //this method will be called in an environment, where it were already checked, thus it returns here always false
//
//        try
//        {
//            if(!exists)
//                insertPhoto(photo);
//        }
//        catch (SQLException e)
//        {
//            _logger.log(Level.SEVERE, "photo.getId() = " + photo.getId() + " | exists = " + exists + "\n"
//                    + Util.extractStackTrace(e) + "\n" + "SQL Error Code: " + e.getErrorCode() );
//        }
//    }

    //todo: user entry needs to be updated
    public void insertUserWithCheck(User user)
    {
        boolean exists = existsUser(user);

        try
        {
            if(!exists)
                insertUser(user);
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    public void updateUserDateCrawled(User user) throws SQLException
    {
        String updateUserStatement = "UPDATE USERS SET date_crawled = ? WHERE USER_ID = ?;";

        List<Object> args = new ArrayList<Object>();
        args.add(Util.createTimestamp());
        args.add(user.getId());

        executeStatement(updateUserStatement, args, false);
    }

    public String getUserDateCrawled(User user) throws SQLException
    {
        String updateUserStatement = "SELECT DATE_CRAWLED FROM USERS WHERE USER_ID = ?";

        List<Object> args = new ArrayList<Object>();
        args.add(user.getId());

        ResultSet resultSet = executeStatement(updateUserStatement, args, true);

        resultSet.next();
        String dateCrawled = resultSet.getString("DATE_CRAWLED");
        resultSet.close();

        return dateCrawled;
    }

    private boolean existsEntry(String whatExists, String fromTable, String whatValueExists)
    {
        try
        {
            List<Object> args = new ArrayList<Object>();
            args.add(whatValueExists);
            ResultSet resultSet = executeStatement("SELECT "+whatExists+" FROM "+fromTable+" WHERE "+whatExists+" = ?", args, true);

            if(resultSet.next()) //contains  result
            {
                resultSet.close();
                return true;
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }

        return false;
    }


    public boolean existsPhoto(Photo photo)
    {
        boolean exists = existsEntry("PHOTO_ID","PHOTOS", photo.getId());
        _logger.log(Level.INFO, "photo.getId() = " + photo.getId() + " | exists = " + exists);
        return exists;
    }

    private boolean existsUser(User user)
    {
        boolean exists = existsEntry("USER_ID","USERS", user.getId());
        _logger.log(Level.INFO, "user.getId() = " + user.getId() + " | exists = " + exists);
        return exists;
    }



    public void RemoveUserEntries() throws SQLException
    {
        executeStatement("DELETE FROM USERS;", new ArrayList<Object>(), false);
    }

    public void RemovePhotoEntries() throws SQLException
    {
        executeStatement("DELETE FROM PHOTOS;", new ArrayList<Object>(), false);
    }

    public void RemovePhotoEntries(String userId) throws SQLException
    {
        List<Object> args = new ArrayList<Object>();
        args.add(userId.replaceAll("'",""));
        executeStatement("DELETE FROM PHOTOS WHERE USER_ID = ? ;", args, false);
    }



    public void OutputUserEntries() throws SQLException
    {
        ResultSet res = executeStatement("SELECT * FROM USERS;", new ArrayList<Object>(), true);
        OutputUserEntries(res);
    }

    private void OutputUserEntries(ResultSet resultSet) throws SQLException
    {
        while(resultSet.next())
        {
            String msg = "";
            msg += "USER_ID: " + resultSet.getString("USER_ID") + " | ";
            msg += "USERNAME: " + resultSet.getString("USERNAME") + " | ";
            msg += "REALNAME: " + resultSet.getString("REALNAME") + " | ";
            msg += "PHOTOSCOUNT: " + resultSet.getString("PHOTOSCOUNT") + " | ";
            msg += "DATE_CRAWLED: " + resultSet.getString("DATE_CRAWLED") + "\n";

            System.out.print(msg);
        }
        resultSet.close();
    }


    public void OutputPhotoEntries() throws SQLException
    {
        ResultSet res = executeStatement("SELECT * FROM PHOTOS;", new ArrayList<Object>(), true);
        OutputPhotoEntries(res);
    }


    public void OutputPhotoEntries(String userId) throws SQLException
    {
        List<Object> args = new ArrayList<Object>();
        args.add(userId);
        ResultSet res = executeStatement("SELECT * FROM PHOTOS WHERE USER_ID = ? ;", args, true);
        OutputPhotoEntries(res);
    }

    private void OutputPhotoEntries(ResultSet resultSet) throws SQLException
    {
        while(resultSet.next())
        {
            String msg = "";
            msg += "PHOTO_ID: " + resultSet.getString("PHOTO_ID") + " | ";
            msg += "TITLE: " + resultSet.getString("TITLE") + " | ";
            msg += "SECRET: " + resultSet.getString("SECRET") + " | ";
            msg += "USER_ID: " + resultSet.getString("USER_ID") + " | ";
            msg += "MEDIA: " + resultSet.getString("MEDIA") + " | ";
            msg += "ISORIGINAL: " + resultSet.getString("ISORIGINAL") + " | ";
            msg += "DATE_UPLOAD: " + resultSet.getString("DATE_UPLOAD") + " | ";
            msg += "DATE_CRAWLED: " + resultSet.getString("DATE_CRAWLED") + "\n";

            System.out.print(msg);
        }
        resultSet.close();
    }

}