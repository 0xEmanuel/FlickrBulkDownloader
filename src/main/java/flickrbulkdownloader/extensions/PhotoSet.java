package flickrbulkdownloader.extensions;

import java.util.List;

public class PhotoSet extends com.flickr4java.flickr.photos.PhotoSet
{

    private List<Photo> _photoList;

    public List<Photo> getPhotoList()
    {
        return _photoList;
    }

    public void setPhotoList(List<Photo> photoList)
    {
        _photoList = photoList;
    }

    //TODO: remove unused getter and setter from parent class

}
