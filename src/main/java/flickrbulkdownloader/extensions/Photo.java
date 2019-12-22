package flickrbulkdownloader.extensions;


import java.util.Date;

public class Photo extends com.flickr4java.flickr.photos.Photo implements Comparable<Photo>
{
    //private String _originalSecret;

    private String _userId; //nsid
    private boolean _isOriginalAvailable;


//    public String getOriginalSecret()
//    {
//        return _originalSecret;
//    }

    public String getUserId()
    {
        return _userId;
    }

    public boolean getIsOriginalAvailable()
    {
        return _isOriginalAvailable;
    }

//    public void setOriginalSecret(String originalSecret)
//    {
//        _originalSecret = originalSecret;
//    }

    public void setUserId(String userId)
    {
        _userId = userId;
    }

    public void setIsOriginalAvailable(boolean isOriginalAvailable)
    {
        _isOriginalAvailable = isOriginalAvailable;
    }

    @Override
    public int compareTo(Photo photo)
    {
        return super.getDateAdded().compareTo(photo.getDateAdded());
    }

    //TODO: remove unused getter and setter from parent class
}
