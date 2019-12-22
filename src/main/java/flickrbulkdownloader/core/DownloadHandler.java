package flickrbulkdownloader.core;

import flickrbulkdownloader.extensions.ApiCallInvalidException;
import flickrbulkdownloader.tools.Util;
import flickrbulkdownloader.extensions.Photo;
import org.apache.commons.io.FilenameUtils;

import javax.net.ssl.SSLException;
import java.io.File;
import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.*;
import java.security.Security;
import java.util.logging.Level;
import java.util.logging.Logger;


public class DownloadHandler implements IDownloadHandler
{
    public static boolean ENABLE_TIMEOUT_CHECK;// = true; //enabled: no freeze at loss of internet connection while download, but program is slower

    static int TIMEOUT_SECONDS;// = 10;
    static int TIMEOUT_SECONDS_POLL_CHECK;// = 5; //check every 5 seconds for time out.
    static int MAX_FILENAME_LENGTH = 90;

    private BufferedInputStream inputStream = null;
    private FileOutputStream outputStream = null;

    private FlickrApi _flickrApi;
    private Logger _logger;

    private static String SAVE_PATH;

    private long _lastUpdateTime;
    private boolean _stillDownloading;
    private boolean _sslExceptionThrown;

    //external setup via setter methods
    private String _currentPhotoSetFolderName = "default";
    private String _currentUserFolderName = "default";

    public DownloadHandler(FlickrApi flickrApi,
                           Configuration.DownloadHandlerConfiguration downloadHandlerConfig,
                           Logger logger)
    {
        System.setProperty("java.protocol.handler.pkgs", "com.sun.net.ssl.internal.www.protocol");
        System.setProperty("https.protocols", "TLSv1.2");
        Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());

        ENABLE_TIMEOUT_CHECK = downloadHandlerConfig.getEnableTimeoutCheck();
        TIMEOUT_SECONDS = downloadHandlerConfig.getTimeoutSeconds();
        TIMEOUT_SECONDS_POLL_CHECK = downloadHandlerConfig.getTimeoutSecondsPollCheck();
        SAVE_PATH = Util.appendSlashIfNeeded(downloadHandlerConfig.getSavePath());

        _flickrApi = flickrApi;
        _logger = logger;
    }

    /*
        return Download Link for Media (Picture or Video)
     */
    private String getDownloadLink(Photo photo) throws IOException, ApiCallInvalidException
    {
        if(photo.getMedia().equalsIgnoreCase(FlickrApi.VIDEO))
            return _flickrApi.queryApiGetVideoDownloadLink(photo);
        return _flickrApi.queryApiGetPictureDownloadLink(photo);
    }

    /*
        Create filename from media attributes (Photo: id and title) and downloadLink (file extension)
     */
    private static String createFilename(Photo photo, String downloadLink)
    {
        String filename = "";

        //get file extension
        if(photo.getMedia().equalsIgnoreCase(FlickrApi.VIDEO))
        {
            final int MIN_FILE_EXTENSION_LENGTH = 2;
            final int MAX_FILE_EXTENSION_LENGTH = 5;

            int indexOfQuestionMark = downloadLink.indexOf("?"); //occurs right after the file extension
            int indexOfFileExtensionDot = 0;

            String fileExtension = "_no_fe.mp4"; //default

            while(indexOfFileExtensionDot >= 0)
            {
                indexOfFileExtensionDot = downloadLink.indexOf(".", indexOfFileExtensionDot+1);
                int distance = Math.abs(indexOfQuestionMark - indexOfFileExtensionDot);

                if( (distance >= MIN_FILE_EXTENSION_LENGTH) && (distance <= MAX_FILE_EXTENSION_LENGTH) ) //questionMark needs to be close to the dot
                    fileExtension = "." + downloadLink.substring(indexOfFileExtensionDot + 1, indexOfQuestionMark);
            }

            filename = photo.getId() + "_" + photo.getTitle().trim().replaceAll("[^a-zA-Z0-9-]", "_") + fileExtension;
        }
        else //is a picture
        {
            String filenameFromUrl = FilenameUtils.getName(downloadLink); //contains already photo_id and originalsecret

            String[] filenameFromUrlTuple = filenameFromUrl.split("\\."); //argument needs to be regex and dot is escaped with an escaped slash
            //dot gets lost in result
            filename = filenameFromUrlTuple[0] + "_" + photo.getTitle().trim().replaceAll("[^a-zA-Z0-9-]", "_") + "." + filenameFromUrlTuple[1];
        }

        filename = filename.substring(0, Math.min(MAX_FILENAME_LENGTH, filename.length())); //limit filename length

        return filename;
    }


    /*
        Download workflow:

        downloadMedia()
        -> downloadWithFileChecks()
        -> if ENABLE_TIMEOUT_CHECK is true: downloadWithTimeoutChecks()
        -> downloadToFile()
     */

    /*
        1. via Call: get download link
        2. via Call: get filename
        3. create destination path (with folders)
        4. via Call: download
     */
    public int downloadMedia(Photo photo) throws IOException, ApiCallInvalidException
    {
        String dlink = getDownloadLink(photo);

        boolean isNotOriginal = false;
        if(dlink.contains("#"))
        {
            if(0 == Integer.parseInt(dlink.substring(dlink.indexOf("#")+1 ) ) )
                isNotOriginal = true;
            dlink = dlink.substring(0,dlink.indexOf("#"));
        }

        String filename = createFilename(photo, dlink);
        final String dstPath =  SAVE_PATH + _currentUserFolderName + "/" + _currentPhotoSetFolderName; //are set externally
        final String dstFilePath = dstPath + "/" + filename;

        new File(dstPath).mkdirs(); //create folders

        boolean isSuccessful = downloadWithFileChecks(dstFilePath,dlink);

        if(!isSuccessful)
            return -1;

        if(isNotOriginal)
            return 0;

        return 1; //original
    }
    /*
    return values:
    -1 means download failed
    0 means download successful but not original quality (large)
    1 means download successful and original quality
     */


    /*
        1. Check if file already exists (finds file via filename and verifies via file size)
        2. Timeout Handling if enabled (retries if timeout)

     */
    private boolean downloadWithFileChecks(String localPath, String remotePath)
    {
        final int MAX_DOWNLOAD_RETRIES = 6;
        final int WAIT_SECONDS_UNTIL_RETRY = 5;

        long remoteFileSize = Util.getRemoteFileSize(remotePath);

        boolean isSameSize = Util.getLocalFileSize(localPath) == remoteFileSize; //obviously this matches only if they have also the same filename.
        if(isSameSize) //file already exists and is not corrupted, then I dont need to download the file
        {
            _logger.log(Level.INFO, "File " + remotePath + " already exist at " + localPath);
            return true; //we dont consider this as a fail //todo should be logged
        }


        int tryCounter;
        for(tryCounter = 0; tryCounter < MAX_DOWNLOAD_RETRIES; tryCounter++)
        {
            if(ENABLE_TIMEOUT_CHECK)
                downloadWithTimeoutChecks(localPath,remotePath); //this downloads the file
            else
                downloadToFile(localPath,remotePath);

            if(_sslExceptionThrown) //exception could be thrown in its own thread //todo needs to be tested
                remotePath = remotePath.replace("https://","http://");


            //check if downloaded correctly;
            isSameSize = Util.getLocalFileSize(localPath) == remoteFileSize;

            if(isSameSize)
                break; //no more iteration needed, since file is correctly downloaded

            Util.sleep(WAIT_SECONDS_UNTIL_RETRY * 1000); //wait before starting new try
        }

        return tryCounter != MAX_DOWNLOAD_RETRIES; //returns false if we were not able to download the file
    }

    /*
        1. create a seperate thread that starts the download (the downloadToFile() method updates the lastUpdateTime global variable)
        2. main thread checks (via polling) the lastUpdateTime variable for changes, which is updated by the download thread
     */

    private void downloadWithTimeoutChecks(final String localPath, final String remotePath)
    {
        //since InputStream cant timeout, we need to poll for updates from the thread

        Runnable runnableDownload = new Runnable()
        {
            public void run()  //@Override
            {
                System.out.println("Thread starting ..." + Thread.currentThread());
                downloadToFile(localPath,remotePath);

                _stillDownloading = false;
                System.out.println("Thread ending ...");
            }
        };

        _stillDownloading = true;
        Thread t = new Thread(runnableDownload);
        t.start();

        _lastUpdateTime = System.nanoTime();


        while(_stillDownloading)
        {
            long currentTime = System.nanoTime();
            long distanceSeconds = Math.abs(currentTime - _lastUpdateTime) / 1000000000;

            System.out.println("distanceSeconds: " + distanceSeconds);
            if(distanceSeconds > TIMEOUT_SECONDS)
            {
                System.out.println("TIMEOUT!");
                t.interrupt(); //this doesnt really stop the thread... the blocking read inputstream is still waiting for bytes...
                return;        // however this just occurs if the internet connection gets interrupted while downloading and never reconnected.
            }

            Util.sleep(TIMEOUT_SECONDS_POLL_CHECK * 1000);
        }

        try
        {
            t.join(); //wait until thread is finished
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }


    /*
        - download file from remote resource
        - update global variable lastUpdateTime to detect timeout from main thread
        - inputStream and ouputStream are declared as global variables, so the main thread can close them if timeout while download occurs
     */
    private void downloadToFile(String localPath, String remotePath)
    {
        _sslExceptionThrown = false;

        System.out.println("Download from URL: " + remotePath);
        System.out.println("Save file at: " + localPath);

        //localPath = "/media/sf_VirtualBox_Share/test.mp4";

        try
        {
            URL url = new URL(remotePath);

            //this just detects timeout on initialization of connection but not after it has been established
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(TIMEOUT_SECONDS * 1000);
            conn.setReadTimeout(TIMEOUT_SECONDS * 1000);

            long size = conn.getContentLength();

            if (size < 0)
                System.out.println("Could not get the file size");
            else
                System.out.println("File size: " + size/(1024.*1024.) + " MB");

            inputStream = new BufferedInputStream(url.openStream());
            outputStream = new FileOutputStream(localPath);
            byte data[] = new byte[1024];
            int count;
            double sumCount = 0.0;

            while ((count = inputStream.read(data, 0, 1024)) != -1)
            {
                _lastUpdateTime = System.nanoTime(); //for timeout check in downloadWithTimeoutChecks()
                outputStream.write(data, 0, count);
                int previousPercentage = (int)(sumCount / size * 100.0);
                sumCount += count;
                int currentPercentage = (int)(sumCount / size * 100.0);

                int difference = previousPercentage - currentPercentage;
                if ( (size > 0) && ( difference != 0) )  //every 1% print
                {
                    System.out.print(currentPercentage+"% ... "); //we want live progress updates from the download

                    if(currentPercentage % 20 == 0)
                        System.out.println();
                }
            }
            System.out.println();

        }
        catch (MalformedURLException e)
        {
            e.printStackTrace();
        }
        catch(SocketTimeoutException e)
        {
            e.printStackTrace();
        }
        catch(SSLException e)
        {
            e.printStackTrace();
            _sslExceptionThrown = true; //caller will retry with http instead of https
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            Util.closeStreamsWithChecks(inputStream,outputStream);
        }
    }


    //    public static void downloadToFileALT(String localPath, String remotePath)
//    {
//        try
//        {
//            URL url = new URL(remotePath);
//            HttpURLConnection connection = (HttpURLConnection)  url.openConnection();
//            connection.setRequestMethod("GET");
//            connection.setDoOutput(true);
//            connection.setConnectTimeout(10000);
//            connection.setReadTimeout(10000);
//            long start = System.currentTimeMillis();
//            Files.copy(connection.getInputStream(), (new File(localPath)).toPath());
//            System.out.println("Time: "+((System.currentTimeMillis() - start) / 1000) + " sec.");
//        }
//        catch(Exception e)
//        {
//            e.printStackTrace();
//        }
//
//    }


    public void setCurrentPhotoSetFolderName(String currentPhotoSetFolderName)
    {
        _currentPhotoSetFolderName = currentPhotoSetFolderName;
    }

    public void setCurrentUserFolderName(String currentUserFolderName)
    {
        _currentUserFolderName = currentUserFolderName;
    }



}
