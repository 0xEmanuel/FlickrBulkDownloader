package flickrbulkdownloader.core;


import flickrbulkdownloader.extensions.ApiCallInvalidException;
import flickrbulkdownloader.extensions.Photo;

import java.io.IOException;

public interface IDownloadHandler
{
    int downloadMedia(Photo photo) throws IOException, ApiCallInvalidException;
    void setCurrentPhotoSetFolderName(String currentPhotoSetFolderName);
    void setCurrentUserFolderName(String currentUserFolderName);
}
