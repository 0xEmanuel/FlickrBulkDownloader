package flickrbulkdownloader.tools;

import flickrbulkdownloader.extensions.Photo;
import flickrbulkdownloader.extensions.PhotoSet;
import com.flickr4java.flickr.people.User;

import java.util.List;

public class Output
{
    private static void out(String text)
    {
        System.out.println(text);
    }

    private static String OutputMessagePhoto(Photo photo)
    {
        String msg = "Photo | id: " + photo.getId() + " secret: " + photo.getSecret() + " UserId: " + photo.getUserId()
                + " Title: " + photo.getTitle() + " Media: " + photo.getMedia() + " DateAdded: " + photo.getDateAdded()
                + " DateTaken: " + photo.getDateTaken() + "\n";
        return msg;
    }

    private static String OutputMessagePhotoList(List<Photo> photoList)
    {
        String msg = "PhotoList:\n";
        for(Photo photo : photoList)
            msg += OutputMessagePhoto(photo);
        return msg;
    }

    private static String OutputMessagePhotoSet(PhotoSet photoSet)
    {
        String msg = "PhotoSet | id: " + photoSet.getId() + " secret: " + photoSet.getSecret()
                  + " Title: " + photoSet.getTitle() + "\n";
        msg += OutputMessagePhotoList(photoSet.getPhotoList());
        return msg;
    }

    private static String OutputMessagePhotoSetList(List<PhotoSet> photoSetList)
    {
        String msg = "PhotoSetList:\n";
        for(PhotoSet photoSet : photoSetList)
            msg += OutputMessagePhotoSet(photoSet);
        return msg;
    }

    public static String OutputMessageUser(User user)
    {
        String msg = "User | id: " + user.getId() + " username: " + user.getUsername() +
                     " realname: " + user.getRealName() + " photosCount: " + user.getPhotosCount() + "\n";
        return msg;
    }

    public static void OutputPhoto(Photo photo)
    {
        out(OutputMessagePhoto(photo));
    }

    public static void OutputPhotoList(List<Photo> photoList)
    {
        out(OutputMessagePhotoList(photoList));
    }

    public static void OutputPhotoSet(PhotoSet photoSet)
    {
        out(OutputMessagePhotoSet(photoSet));
    }

    public static void OutputPhotoSetList(List<PhotoSet> photoSetList)
    {
        out(OutputMessagePhotoSetList(photoSetList));
    }

    public static void OutputUser(User user)
    {
        out(OutputMessageUser(user));
    }
}
