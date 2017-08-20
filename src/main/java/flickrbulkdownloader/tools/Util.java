package flickrbulkdownloader.tools;

import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.xml.XmlPage;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Util
{
    public static boolean ENABLE_HTTP_OUTPUT;

    public static XmlPage sendHttpWebRequest(String url) throws IOException
    {
        java.net.URL urlObj = new java.net.URL(url);
        WebRequest webRequest = new WebRequest(urlObj);
        webRequest.setAdditionalHeader("Accept-Language", "en-US,en;q=0.5");
        webRequest.setAdditionalHeader("Content-Type","application/json");
        webRequest.setAdditionalHeader("User-Agent", "Mozilla/5.0 (X11; Linux x86_64; rv:45.0) Gecko/20100101 Firefox/45.0");

        WebClient webClient = new WebClient();
        webClient.getOptions().setJavaScriptEnabled(false);
        webClient.getOptions().setCssEnabled(false);
        webClient.getOptions().setAppletEnabled(false);

        Page page = webClient.getPage(webRequest);
        XmlPage xmlPage = (XmlPage) page;

        if(ENABLE_HTTP_OUTPUT)
            System.out.println(page.getWebResponse().getContentAsString().replace("&amp;","&"));

        return xmlPage;
    }

    public static void writeToFile(String text, String path) throws IOException
    {
        FileUtils.writeStringToFile(new File(path), text);
    }

    public static String readFile(String path) throws IOException
    {
        return FileUtils.readFileToString(new File(path), "UTF-8");
    }

    public static String createTimestamp()
    {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        return dateFormat.format(date);
    }

    public static String appendSlashIfNeeded(String path)
    {
        char lastElement = path.charAt(path.length()-1);
        if(lastElement == '/')
            return path;

        return path + "/";
    }

    public static boolean isSameFileSize(String localPath, String remotePath) // remotePath can be a download link
    {
        long localFileSize = getLocalFileSize(localPath);
        long remoteFileSize = getRemoteFileSize(remotePath);

        return localFileSize == remoteFileSize;
    }

    public static long getLocalFileSize(String localPath)
    {
        return (new File(localPath)).length();
    }

    public static long getRemoteFileSize(String remotePath)
    {
        try
        {
            URL url = new URL(remotePath);
            URLConnection conn = url.openConnection();
            return conn.getContentLength();
        }
        catch (MalformedURLException e)
        {
            e.printStackTrace();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }

        return 0;
    }

    public static void sleep(int milliseconds)
    {
        System.out.println("Sleep " + milliseconds + " milliseconds");

        try
        {
            Thread.sleep(milliseconds);
        }
        catch(InterruptedException ex)
        {
            Thread.currentThread().interrupt();
        }
    }

    public static void closeStreamsWithChecks(BufferedInputStream in, FileOutputStream out)
    {
        if (in != null)
            try
            {
                in.close();

            }
            catch (IOException e)
            {
                e.printStackTrace();
            }

        if (out != null)
            try
            {
                out.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
    }

    public static String extractStackTrace(Exception e)
    {
        StringWriter errors = new StringWriter();
        e.printStackTrace(new PrintWriter(errors));
        return errors.toString();
    }

    public static String buildSqlQuery(String sql, List<Object> args) throws SQLException
    {
        try
        {
            Pattern pattern = Pattern.compile("\\?");
            Matcher matcher = pattern.matcher(sql);
            StringBuffer sb = new StringBuffer();
            int i = 0;  // Parameter begin with index 1
            while (matcher.find())
            {
                matcher.appendReplacement(sb,String.valueOf(args.get(i)));
                i++;
            }
            matcher.appendTail(sb);
            return sb.toString();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

        return sql;
    }

}
