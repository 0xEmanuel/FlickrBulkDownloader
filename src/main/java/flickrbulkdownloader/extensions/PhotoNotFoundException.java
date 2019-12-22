package flickrbulkdownloader.extensions;

public class PhotoNotFoundException extends ApiCallInvalidException
{
    // Parameterless Constructor
    public PhotoNotFoundException() {}

    // Constructor that accepts a message
    public PhotoNotFoundException(int code, String message)
    {
        super(code, message);
    }

}
