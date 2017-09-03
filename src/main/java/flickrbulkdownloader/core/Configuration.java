package flickrbulkdownloader.core;


import flickrbulkdownloader.tools.Util;

import java.io.*;
import java.util.Properties;


public class Configuration
{

    public class DatabaseConfiguration
    {
        private String _dbDirPath;
        private String _dbName;

        public String getDbDirPath()
        {
            return _dbDirPath;
        }
        public String getDbName()
        {
            return _dbName;
        }
        public void setDbDirPath(String dbDirPath)
        {
            _dbDirPath = dbDirPath;
        }
        public void setDbName(String dbName)
        {
            _dbName = dbName;
        }
    }

    public class AuthConfiguration
    {
        private String _apiKey;
        private String _apiSecret;
        private String _authToken;

        public String getApiKey()
        {
            return _apiKey;
        }
        public String getApiSecret()
        {
            return _apiSecret;
        }
        public String getAuthToken()
        {
            return _authToken;
        }
        public void setApiKey(String apiKey)
        {
            _apiKey = apiKey;
        }
        public void setApiSecret(String apiSecret)
        {
            _apiSecret = apiSecret;
        }
        public void setAuthToken(String authToken)
        {
            _authToken = authToken;
        }

        public void updateAuthToken(String authToken) throws IOException
        {
            _properties.setProperty("AUTH_TOKEN", authToken);
            _properties.store(new FileOutputStream(configPath), null);

            _authToken = authToken;
        }
    }

    public class DownloadHandlerConfiguration
    {
        private boolean _enableTimeoutCheck;
        private int _timeoutSeconds;
        private int _timeoutSecondsPollCheck;
        private String _savePath;

        public boolean getEnableTimeoutCheck()
        {
            return _enableTimeoutCheck;
        }
        public int getTimeoutSeconds()
        {
            return _timeoutSeconds;
        }
        public int getTimeoutSecondsPollCheck()
        {
            return _timeoutSecondsPollCheck;
        }
        public String getSavePath()
        {
            return _savePath;
        }
        public void setEnableTimeoutCheck(boolean enableTimeoutCheck)
        {
            _enableTimeoutCheck = enableTimeoutCheck;
        }
        public void setTimeoutSeconds(int timeoutSeconds)
        {
            _timeoutSeconds = timeoutSeconds;
        }
        public void setTimeoutSecondsPollCheck(int timeoutSecondsPollCheck)
        {
            _timeoutSecondsPollCheck = timeoutSecondsPollCheck;
        }
        public void setSavePath(String savePath)
        {
            _savePath = savePath;
        }
    }

    public class CrawlerConfiguration
    {
        private boolean _crawlVideos;
        private boolean _crawlPictures;

        private boolean _enableDownloadHandler;
        private boolean _enableDbInserts;
        private boolean _enableDbLookups;

        public boolean getCrawlVideos()
        {
            return _crawlVideos;
        }
        public boolean getCrawlPictures()
        {
            return _crawlPictures;
        }
        public boolean getEnableDownloadHandler()
        {
            return _enableDownloadHandler;
        }
        public boolean getEnableDbInserts()
        {
            return _enableDbInserts;
        }
        public boolean getEnableDbLookups()
        {
            return _enableDbLookups;
        }

        public void setCrawlVideos(boolean crawlVideos)
        {
            _crawlVideos = crawlVideos;
        }
        public void setCrawlPictures(boolean crawlPictures)
        {
            _crawlPictures = crawlPictures;
        }
        public void setEnableDownloadHandler(boolean enableDownloadHandler)
        {
            _enableDownloadHandler = enableDownloadHandler;
        }
        public void setEnableDbInserts(boolean enableDbInserts)
        {
            _enableDbInserts = enableDbInserts;
        }
        public void setEnableDbLookups(boolean enableDbLookups)
        {
            _enableDbLookups = enableDbLookups;
        }
    }


    private AuthConfiguration _authConfiguration;
    private DatabaseConfiguration _databaseConfiguration;
    private DownloadHandlerConfiguration _downloadHandlerConfiguration;
    private CrawlerConfiguration _crawlerConfiguration;

    private static final String configPath = "config.properties";
    private Properties _properties;

    public Configuration()
    {
        File f = new File(configPath);
        if( !(f.exists() && !f.isDirectory()) )
            createDefaultProperties();

        _properties = new Properties();
        try
        {
            _properties.load(new FileInputStream(configPath));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        _authConfiguration = createAuthConfiguration();
        _databaseConfiguration = createDatabaseConfiguration();
        _downloadHandlerConfiguration = createDownloadHandlerConfiguration();
        _crawlerConfiguration = createCrawlerConfiguration();

        Util.ENABLE_HTTP_OUTPUT = Boolean.parseBoolean(_properties.getProperty("ENABLE_HTTP_OUTPUT"));
    }

    public void createDefaultProperties()
    {
        Properties properties = new Properties();

        // set the properties value
        properties.setProperty("CRAWL_PICTURES", "true");
        properties.setProperty("TIMEOUT_SECONDS_POLL_CHECK", "5");
        properties.setProperty("CRAWL_VIDEOS", "true");
        properties.setProperty("DB_DIR_PATH", new File("").getAbsoluteFile() + File.separator + "database" + File.separator);
        properties.setProperty("SAVE_PATH", System.getProperty("user.home") + File.separator + "Downloads" + File.separator + "FlickrBulkDownloads");
        properties.setProperty("TIMEOUT_SECONDS", "10");
        properties.setProperty("DB_NAME", "db");
        properties.setProperty("API_KEY", "");
        properties.setProperty("API_SECRET", "");
        properties.setProperty("AUTH_TOKEN", "");
        properties.setProperty("ENABLE_TIMEOUT_CHECK", "false");
        properties.setProperty("ENABLE_HTTP_OUTPUT", "true");
        properties.setProperty("ENABLE_DOWNLOAD_HANDLER", "true");
        properties.setProperty("ENABLE_DB_INSERTS", "true");
        properties.setProperty("ENABLE_DB_LOOKUPS", "true");

        // save properties to project root folder
        try
        {
            properties.store(new FileOutputStream(configPath), null);
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }

    }


    private DatabaseConfiguration createDatabaseConfiguration()
    {
        DatabaseConfiguration dbConfig = new DatabaseConfiguration();
        dbConfig.setDbDirPath(_properties.getProperty("DB_DIR_PATH"));
        dbConfig.setDbName(_properties.getProperty("DB_NAME"));

        return dbConfig;
    }

    private AuthConfiguration createAuthConfiguration()
    {
        AuthConfiguration authConfig = new AuthConfiguration();
        authConfig.setApiKey(_properties.getProperty("API_KEY"));
        authConfig.setApiSecret(_properties.getProperty("API_SECRET"));
        authConfig.setAuthToken(_properties.getProperty("AUTH_TOKEN"));

        return authConfig;
    }

    private DownloadHandlerConfiguration createDownloadHandlerConfiguration()
    {
        DownloadHandlerConfiguration downloadHandlerConfig = new DownloadHandlerConfiguration();
        downloadHandlerConfig.setEnableTimeoutCheck(Boolean.parseBoolean(_properties.getProperty("ENABLE_TIMEOUT_CHECK")));
        downloadHandlerConfig.setTimeoutSeconds(Integer.parseInt(_properties.getProperty("TIMEOUT_SECONDS")));
        downloadHandlerConfig.setTimeoutSecondsPollCheck(Integer.parseInt(_properties.getProperty("TIMEOUT_SECONDS_POLL_CHECK")));
        downloadHandlerConfig.setSavePath(_properties.getProperty("SAVE_PATH"));

        return downloadHandlerConfig;
    }

    private CrawlerConfiguration createCrawlerConfiguration()
    {
        CrawlerConfiguration crawlerConfig = new CrawlerConfiguration();
        crawlerConfig.setCrawlVideos(Boolean.parseBoolean(_properties.getProperty("CRAWL_VIDEOS")));
        crawlerConfig.setCrawlPictures(Boolean.parseBoolean(_properties.getProperty("CRAWL_PICTURES")));
        crawlerConfig.setEnableDownloadHandler(Boolean.parseBoolean(_properties.getProperty("ENABLE_DOWNLOAD_HANDLER")));
        crawlerConfig.setEnableDbInserts(Boolean.parseBoolean(_properties.getProperty("ENABLE_DB_INSERTS")));
        crawlerConfig.setEnableDbLookups(Boolean.parseBoolean(_properties.getProperty("ENABLE_DB_LOOKUPS")));

        return crawlerConfig;
    }

    //getter

    public AuthConfiguration getAuthConfiguration()
    {
        return _authConfiguration;
    }

    public DatabaseConfiguration getDatabaseConfiguration()
    {
        return _databaseConfiguration;
    }

    public DownloadHandlerConfiguration getDownloadHandlerConfiguration()
    {
        return _downloadHandlerConfiguration;
    }

    public CrawlerConfiguration getCrawlerConfiguration()
    {
        return _crawlerConfiguration;
    }

}
