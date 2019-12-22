package flickrbulkdownloader.core;

import flickrbulkdownloader.extensions.ApiCallInvalidException;
import flickrbulkdownloader.extensions.Photo;
import flickrbulkdownloader.extensions.PhotoSet;
import com.flickr4java.flickr.Flickr;
import com.flickr4java.flickr.FlickrException;
import com.flickr4java.flickr.Parameter;
import com.flickr4java.flickr.people.User;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.xml.XmlPage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FlickrApi
{

    //Flickr Api Parameters
    private final static String API_KEY = "api_key";
    private final static String PHOTO_ID = "photo_id";
    private final static String PHOTOSET_ID = "photoset_id";
    private final static String SECRET = "secret";
    private final static String USER_ID = "user_id";
    private final static String OAUTH_CONSUMER_KEY = "oauth_consumer_key";
    private final static String SAFE_SEARCH = "safe_search";
    private final static String OAUTH_TOKEN = "oauth_token";
    private final static String EXTRAS = "extras";
    private final static String PAGE = "page";
    private final static String PER_PAGE = "per_page";
    private final static String USERNAME = "username";

    //possible values for media attribute
    public final static String VIDEO = "video";
    public final static String PHOTO = "photo";



    //From Authentication
    private String _oauthTokenValue;
    private String _tokenSecret;
    private String _tokenKey;
    private Flickr _flickr;


    private String _apiKey;
    private String _apiSecret;
    private Configuration.AuthConfiguration _authConfig;

    private Logger _logger;

    private FlickrApiHelper _flickrApiHelper;

    public FlickrApi(Configuration.AuthConfiguration authConfig, Logger logger) throws IOException, FlickrException
    {
        _apiKey = authConfig.getApiKey();
        _apiSecret = authConfig.getApiSecret();
        _authConfig = authConfig;

        _flickr = createAuthenticatedFlickr();
        _logger = logger;

        _flickrApiHelper = new FlickrApiHelper(logger);
    }

    private Flickr createAuthenticatedFlickr() throws IOException, FlickrException
    {
        Authenticator authenticator = new Authenticator(_authConfig);
        authenticator.oauth();

        Flickr flickr = authenticator.getFlickr();

        _oauthTokenValue = authenticator.getTokenValue();
        _tokenSecret = authenticator.getSecretString();
        _tokenKey = authenticator.getTokenKey();

        return flickr;
    }

    public String queryApiGetUserId(String username) throws IOException, ApiCallInvalidException
    {
        List<Parameter> params = new ArrayList<Parameter>();
        params.add(new Parameter(USERNAME,username));
        params.add(new Parameter(OAUTH_CONSUMER_KEY,_apiKey));
        params.add(new Parameter(OAUTH_TOKEN, _oauthTokenValue));
        XmlPage xmlPage = _flickrApiHelper.apiCall(params, "flickr.people.findByUsername");

        return _flickrApiHelper.extractUserId(xmlPage);
    }

    public User queryApiGetUser(String userId) throws IOException, ApiCallInvalidException
    {
        List<Parameter> params = new ArrayList<Parameter>();
        params.add(new Parameter(USER_ID,userId));
        params.add(new Parameter(OAUTH_CONSUMER_KEY,_apiKey));
        params.add(new Parameter(OAUTH_TOKEN, _oauthTokenValue));
        XmlPage xmlPage = _flickrApiHelper.apiCall(params, "flickr.people.getInfo");

        return _flickrApiHelper.extractUser(xmlPage);
    }

    //this method is not going to be called in the crawling all method
    public Photo queryApiGetPhoto(String photoId) throws IOException, ApiCallInvalidException
    {
        List<Parameter> params = new ArrayList<Parameter>();
        params.add(new Parameter(PHOTO_ID,photoId));
        params.add(new Parameter(API_KEY,_apiKey));
        XmlPage xmlPage = _flickrApiHelper.apiCall(params, "flickr.photos.getInfo");

        return _flickrApiHelper.extractPhoto(xmlPage);
    }


    public List<Photo> queryApiGetPhotos(String userId) throws IOException, ApiCallInvalidException
    {
        return queryApiGetPhotos(new Parameter(USER_ID,userId), "flickr.people.getPhotos", "//photos");
    }

    public List<Photo> queryApiGetPhotos(PhotoSet photoSet) throws IOException, ApiCallInvalidException
    {
        return queryApiGetPhotos(new Parameter(PHOTOSET_ID,photoSet.getId()), "flickr.photosets.getPhotos", "//photoset");
    }

    private List<Photo> queryApiGetPhotos(Parameter parameter, String apiMethod, String xpathForPageNumber) throws IOException, ApiCallInvalidException
    {
        int perPage = 500;
        int pages = 1; //assume at the moment only one page
        int totalPhotos = 1;

        List<Photo> photoListTotal = new ArrayList<Photo>();
        for(int page = 1; page <= pages; page++)
        {
            List<Parameter> params = new ArrayList<Parameter>();
            params.add(new Parameter(OAUTH_CONSUMER_KEY,_apiKey));
            params.add(new Parameter(OAUTH_TOKEN, _oauthTokenValue));
            params.add(parameter); //userId or photoSetId
            params.add(new Parameter(SAFE_SEARCH,3));
            params.add(new Parameter(PAGE, page));
            params.add(new Parameter(PER_PAGE, perPage));
            params.add(new Parameter(EXTRAS, "date_upload,date_taken,media,originalsecret"));
            XmlPage xmlPage = _flickrApiHelper.apiCall(params, apiMethod);


            DomElement photosElement = xmlPage.getFirstByXPath(xpathForPageNumber); //
            pages = Integer.parseInt(photosElement.getAttribute("pages"));
            totalPhotos = Integer.parseInt(photosElement.getAttribute("total")); //overwrite

            //System.out.println("pages: " + pages);
            //System.out.println("totalPhotos: " + totalPhotos);

            List<Photo> photoList = _flickrApiHelper.extractPhotoList(xmlPage);
            photoListTotal.addAll(photoList);
        }

        return photoListTotal;
    }

    public List<PhotoSet> queryApiGetPhotoSetList(String userId) throws IOException, ApiCallInvalidException
    {
        int perPage = 500;
        int pages = 1; //assume at the moment only one page
        int totalPhotoSets = 0;

        List<PhotoSet> photoSetListTotal = new ArrayList<PhotoSet>();
        for(int page = 1; page <= pages; page++)
        {
            List<Parameter> params = new ArrayList<Parameter>();
            params.add(new Parameter(OAUTH_CONSUMER_KEY,_apiKey));
            params.add(new Parameter(OAUTH_TOKEN, _oauthTokenValue));
            params.add(new Parameter(USER_ID,userId));
//        params.add(new Parameter(SAFE_SEARCH,3));
            params.add(new Parameter(PAGE, page));
            params.add(new Parameter(PER_PAGE, perPage));
//        params.add(new Parameter(EXTRAS, "date_upload,date_taken,media"));
            XmlPage xmlPage = _flickrApiHelper.apiCall(params, "flickr.photosets.getList");

            DomElement photoSetsElement = xmlPage.getFirstByXPath("//photosets");
            pages = Integer.parseInt(photoSetsElement.getAttribute("pages"));
            totalPhotoSets = Integer.parseInt(photoSetsElement.getAttribute("total")); //overwrite

            //System.out.println("pages: " + pages);
            //System.out.println("totalPhotos: " + totalPhotos);

            List<PhotoSet> photoSetList = _flickrApiHelper.extractPhotoSetList(xmlPage);
            photoSetListTotal.addAll(photoSetList);
        }

        return photoSetListTotal;
    }

    public String queryApiGetPictureDownloadLink(Photo photo) throws IOException, ApiCallInvalidException
    {
        List<Parameter> params = new ArrayList<Parameter>();
        params.add(new Parameter(FlickrApi.PHOTO_ID,photo.getId() ));
        params.add(new Parameter(FlickrApi.API_KEY,_apiKey ));
        params.add(new Parameter(FlickrApi.SECRET,_apiSecret ));
        XmlPage xmlPage = _flickrApiHelper.apiCall(params, "flickr.photos.getSizes");

        return _flickrApiHelper.extractPictureDownloadLink(xmlPage);
    }

    public String queryApiGetVideoDownloadLink(Photo photo) throws IOException, ApiCallInvalidException
    {
        List<Parameter> params = new ArrayList<Parameter>();
        params.add(new Parameter(FlickrApi.PHOTO_ID,photo.getId() ));
        params.add(new Parameter(FlickrApi.SECRET,photo.getSecret() )); //with this 'normal' secret I can get the original video URL
        params.add(new Parameter(FlickrApi.API_KEY,_apiKey ));
        XmlPage xmlPage = _flickrApiHelper.apiCall(params, "flickr.video.getStreamInfo");

        DomElement element = xmlPage.getFirstByXPath("//stream[@type='orig']");
        String downloadLink = element.getTextContent();

        return downloadLink;
    }

    //    private static void getPhotosByPhotoSet(String photoSetId, String userId, String apiKey) throws IOException
//    {
//        String resource = createStandardApiResource("flickr.photosets.getPhotos",apiKey) +"&photoset_id="+photoSetId + "&user_id="+userId + "&safe_search=3";
//        sendHttpWebRequest(resource);
//    }

    //    private static void getGalleryList(String userId, String apiKey, String page) throws IOException
//    {
//
//        String resource = createStandardApiResource("flickr.galleries.getList",apiKey) + "&user_id="+userId +"&page="+page;
//        sendHttpWebRequest(resource);
//    }
//
//    private static void getCollectionList(String userId, String apiKey) throws IOException
//    {
//        String resource = createStandardApiResource("flickr.collections.getTree",apiKey) + "&user_id="+userId;
//        sendHttpWebRequest(resource);
//    }


    //GETTER

    public String getApiKey()
    {
        return _apiKey;
    }

    public String getApiSecret()
    {
        return _apiSecret;
    }

    public String getOauthTokenValue()
    {
        return _oauthTokenValue;
    }



}
