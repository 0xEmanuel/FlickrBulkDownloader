package flickrbulkdownloader.core;

import com.flickr4java.flickr.Parameter;
import flickrbulkdownloader.extensions.ApiCallInvalidException;
import flickrbulkdownloader.extensions.Photo;
import flickrbulkdownloader.extensions.PhotoNotFoundException;
import flickrbulkdownloader.extensions.PhotoSet;
import com.flickr4java.flickr.people.User;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.xml.XmlPage;
import flickrbulkdownloader.tools.Util;
import sun.rmi.runtime.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


class FlickrApiHelper
{

    private Logger _logger;

    FlickrApiHelper(Logger logger)
    {
        _logger = logger;
    }

    List<PhotoSet> extractPhotoSetList(XmlPage xmlPage)
    {
        List<PhotoSet> photoSetList = new ArrayList<PhotoSet>();

        List<DomElement> nlist = xmlPage.getByXPath("//photoset");
        int i = 0;
        for (DomElement element : nlist)
        {
            String id = element.getAttribute("id");
            String secret = element.getAttribute("secret");

            List<DomElement> ntitle = element.getByXPath("//title");
            String title = ntitle.get(i).getTextContent();

            PhotoSet photoSet = new PhotoSet();
            photoSet.setId(id);
            photoSet.setSecret(secret);
            photoSet.setTitle(title);

            photoSetList.add(photoSet);
            i++;
        }
        return photoSetList;
    }

    List<Photo> extractPhotoList(XmlPage xmlPage)
    {
        List<Photo> photoList = new ArrayList<Photo>();

        List<DomElement> nlist = xmlPage.getByXPath("//photo");
        for (DomElement element : nlist)
        {
            String id = element.getAttribute("id");
            String secret = element.getAttribute("secret");
            String title = element.getAttribute("title");
            String dateupload = element.getAttribute("dateupload");
            String datetaken = element.getAttribute("datetaken");
            String media = element.getAttribute("media");
            String userId = element.getAttribute("owner"); // needed for getPhotos(String userId)
            if(userId.equals(""))
                userId = ((DomElement)xmlPage.getFirstByXPath("//photoset")).getAttribute("owner"); // needed for getPhotos(PhotoSet photoSet)

            Photo photo = new Photo();
            photo.setId(id);
            photo.setSecret(secret);
            photo.setTitle(title);
            photo.setDateAdded(dateupload);
            photo.setDateTaken(datetaken);
            photo.setMedia(media);
            photo.setUserId(userId);

            photoList.add(photo);
        }
        return photoList;
    }

    String extractUserId(XmlPage xmlPage)
    {
        DomElement element = xmlPage.getFirstByXPath("//user");
        return element.getAttribute("id");
    }

    User extractUser(XmlPage xmlPage)
    {
        DomElement element = xmlPage.getFirstByXPath("//person");

        String userId = element.getAttribute("id");

        String realname = "";
        if(xmlPage.getElementsByTagName("realname").getLength() > 0) //check if set
            realname = xmlPage.getElementsByTagName("realname").get(0).getTextContent();

        String username = xmlPage.getElementsByTagName("username").get(0).getTextContent();
        int photosCount = Integer.parseInt(xmlPage.getElementsByTagName("count").get(0).getTextContent() );

        User user = new User();
        user.setId(userId);
        user.setRealName(realname);
        user.setUsername(username);
        user.setPhotosCount(photosCount);

        return user;
    }

    Photo extractPhoto(XmlPage xmlPage)
    {
        DomElement element = xmlPage.getFirstByXPath("//photo");

        String id = element.getAttribute("id");
        String secret = element.getAttribute("secret");
        String title = xmlPage.getElementsByTagName("title").get(0).getTextContent();
        String userId = xmlPage.getElementsByTagName("owner").get(0).getAttribute("nsid");
        System.out.println(userId);
        String dateupload = element.getAttribute("dateuploaded");
        String media = element.getAttribute("media");
        //String originalSecret = element.getAttribute("originalsecret");

        /*
        returns the originalsecret for the method flickr.photos.getSizes to paste into a URL of following syntax:
        https://www.flickr.com/photos/[nsid]/[photoId]/play/orig/[originalSecret]/
         */

        Photo photo = new Photo();
        photo.setId(id);
        photo.setSecret(secret);
        photo.setTitle(title);
        photo.setDateAdded(dateupload);
        photo.setMedia(media);
        photo.setUserId(userId);
        //photo.setOriginalSecret(originalSecret);

        return photo;
    }

    String extractPictureDownloadLink(XmlPage xmlPage)
    {
        DomElement sizeElement = xmlPage.getFirstByXPath("//size[@label='Original' and @media='photo']");

        String isOriginal = "#1";

        //if original not available, then get the link at least for the picture version with the highest resolution
        if(sizeElement == null)
        {
            isOriginal = "#0";
            List<DomElement> elementList = xmlPage.getByXPath("//size[@media='photo']");

            int maxPixelCount = 0;
            int maxWidth = 0;
            int maxHeight = 0;

            for(DomElement element : elementList)
            {
                int width = Integer.parseInt(element.getAttribute("width"));
                int height = Integer.parseInt(element.getAttribute("height"));

                int pixelCount = width * height;
                if (pixelCount > maxPixelCount)
                {
                    maxPixelCount = pixelCount;
                    maxWidth = width;
                    maxHeight = height;
                }
            }
            System.out.println("maxWidth: " + maxWidth + " maxHeight: " + maxHeight);
            sizeElement = xmlPage.getFirstByXPath("//size[@width='"+maxWidth+"' and @height='" + maxHeight + "' and @media='photo']");
        }

        String downloadLink = sizeElement.getAttribute("source") + isOriginal; //append isOriginal, which is #0 or #1. DownloadHandler wants write an entry in the database for this information
        return downloadLink;
    }

    XmlPage apiCall(List<Parameter> params, String apiMethod) throws IOException, ApiCallInvalidException
    {
        String resource = createApiResource(params, apiMethod);
        XmlPage xmlPage = null;
        int exceptionCounter = 0;
        int LIMIT = 6;
        while(exceptionCounter < LIMIT)
        {
            try
            {
                xmlPage = Util.sendHttpWebRequest(resource);
                checkApiCallIsValid(xmlPage);
                break;
            }

            catch(com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException e)
            {
                exceptionCounter++;
                try
                {
                    Thread.sleep(4000);
                }
                catch(InterruptedException ei)
                {
                    _logger.log(Level.SEVERE, "Caught exception: \n" + Util.extractStackTrace(ei));
                }

                _logger.log(Level.WARNING,"Caught exception with exceptionCounter:" + exceptionCounter + ": \n" + Util.extractStackTrace(e));
            }
        }

        if(exceptionCounter >= LIMIT)
            throw new ApiCallInvalidException(0, "Too many exceptions!");

        return xmlPage;
    }

    private void checkApiCallIsValid(XmlPage xmlPage) throws ApiCallInvalidException
    {
        DomElement rspElement = xmlPage.getFirstByXPath("//rsp");
        String stat = rspElement.getAttribute("stat");

        if(stat.equalsIgnoreCase("ok"))
            return;

        //error case:
        DomElement errElement = xmlPage.getFirstByXPath("//err");
        int code = Integer.parseInt(errElement.getAttribute("code"));
        String msg = errElement.getAttribute("msg");

        if(msg.equalsIgnoreCase("Photo not found"))
        {
            throw new PhotoNotFoundException(code, msg);
        } //TODO: Add more error cases and define more Exception
        else
        {
            _logger.log(Level.SEVERE,"ApiCall Error: " + msg + " | code: " + code); // TODO improve logging...
            throw new ApiCallInvalidException(code, msg);
        }


    }

    private String createApiResource(List<Parameter> params, String method)
    {
        String resource = "https://api.flickr.com/services/rest?method=" + method;

        for(Parameter parameter : params)
            resource += "&"+parameter.getName() + "=" + parameter.getValue();
        return resource;
    }
}
