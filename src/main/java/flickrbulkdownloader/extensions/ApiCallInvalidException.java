package flickrbulkdownloader.extensions;

public class ApiCallInvalidException extends  Exception
{
    int _code;

    // Parameterless Constructor
    public ApiCallInvalidException() {}

    // Constructor that accepts a message
    public ApiCallInvalidException(int code, String message)
    {
        super(message);
        _code = code;
    }

    public int getCode()
    {
        return _code;
    }
}
