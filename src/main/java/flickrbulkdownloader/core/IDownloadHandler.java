package flickrbulkdownloader.core;


import flickrbulkdownloader.extensions.Photo;

import java.io.IOException;

public interface IDownloadHandler
{
    int downloadMedia(Photo photo) throws IOException;
    void setCurrentPhotoSetFolderName(String currentPhotoSetFolderName);
    void setCurrentUserFolderName(String currentUserFolderName);
}
