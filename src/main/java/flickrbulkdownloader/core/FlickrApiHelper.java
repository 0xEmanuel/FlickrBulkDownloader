package flickrbulkdownloader.core;

import com.flickr4java.flickr.Parameter;
import flickrbulkdownloader.extensions.Photo;
import flickrbulkdownloader.extensions.PhotoSet;
import com.flickr4java.flickr.people.User;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.xml.XmlPage;
import flickrbulkdownloader.tools.Util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


class FlickrApiHelper
{
    static List<PhotoSet> extractPhotoSetList(XmlPage xmlPage)
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

    static List<Photo> extractPhotoList(XmlPage xmlPage)
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

    static User extractUser(XmlPage xmlPage)
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

    static Photo extractPhoto(XmlPage xmlPage)
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

    static XmlPage apiCall(List<Parameter> params, String apiMethod) throws IOException
    {
        String resource = createApiResource(params, apiMethod);
        XmlPage xmlPage = Util.sendHttpWebRequest(resource);

        if(!FlickrApiHelper.apiCallIsValid(xmlPage))
            System.exit(1);

        return xmlPage;
    }

    private static boolean apiCallIsValid(XmlPage xmlPage)
    {
        DomElement rspElement = xmlPage.getFirstByXPath("//rsp");
        String stat = rspElement.getAttribute("stat");

        if(stat.equalsIgnoreCase("ok"))
            return true;

        //error case:
        DomElement errElement = xmlPage.getFirstByXPath("//err");
        String code = errElement.getAttribute("code");
        String msg = errElement.getAttribute("msg");
        System.out.println("ApiCall Error: " + msg + " | code: " + code);

        return false;
    }

    private static String createApiResource(List<Parameter> params, String method)
    {
        String resource = "https://api.flickr.com/services/rest?method=" + method;

        for(Parameter parameter : params)
            resource += "&"+parameter.getName() + "=" + parameter.getValue();
        return resource;
    }
}
