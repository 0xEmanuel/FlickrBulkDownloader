package flickrbulkdownloader.core;

import com.flickr4java.flickr.Flickr;
import com.flickr4java.flickr.FlickrException;
import com.flickr4java.flickr.REST;
import com.flickr4java.flickr.auth.Auth;
import com.flickr4java.flickr.auth.AuthInterface;
import com.flickr4java.flickr.auth.Permission;
import org.scribe.model.Token;
import org.scribe.model.Verifier;

import java.io.IOException;
import java.util.Scanner;


public class Authenticator
{
    private String _tokenKey;

    private String _tokenVal;
    private String _secretStr;
    private Flickr _flickr;

    private String _apiKey;
    private String _apiSecret;

    private Configuration.AuthConfiguration _authConfig;

    public Authenticator(Configuration.AuthConfiguration authConfig)
    {
        _apiKey = authConfig.getApiKey();
        _apiSecret = authConfig.getApiSecret(); //also known as sharedSecret
        _tokenVal = authConfig.getAuthToken();

        _authConfig = authConfig;

        _flickr = new Flickr(_apiKey, _apiSecret, new REST());
        Flickr.debugStream = false;
        Flickr.debugRequest = false;
    }

    public Flickr getFlickr()
    {
        return _flickr;
    }

    public String getTokenValue()
    {
        return _tokenVal;
    }

    public String getSecretString()
    {
        return _secretStr;
    }

    public String getTokenKey()
    {
        return _tokenKey;
    }

    public boolean tokenIsValid()
    {
        try
        {
//            String authTokenStorageFilename = "authTokenStorage.txt";
//
//            File file = new File(authTokenStorageFilename);
//            if(!file.exists() && !file.isDirectory())
//            {
//                return false; //no token to read
//            }
//
//            _tokenVal = Util.readFile(authTokenStorageFilename);

            if(_tokenVal.isEmpty())
                return false;

            Auth auth =_flickr.getAuthInterface().checkToken(_tokenVal,"");
            String perm = auth.getPermission().toString();

            if(perm.equalsIgnoreCase("read"))
                return true;
        }
        catch(FlickrException e)
        {
            e.printStackTrace();
        }

        return false;
    }

    public void oauth() throws IOException, FlickrException
    {
        if( tokenIsValid() )
            return;

        AuthInterface authInterface = _flickr.getAuthInterface();

        Scanner scanner = new Scanner(System.in);

        Token token = authInterface.getRequestToken();
        System.out.println("token: " + token);

        String url = authInterface.getAuthorizationUrl(token, Permission.READ);
        System.out.println("Follow this URL to authorize yourself on Flickr");
        System.out.println(url);
        System.out.println("Paste in the token it gives you:");
        System.out.print(">>");

        _tokenKey = scanner.nextLine();
        scanner.close();

        Token requestToken = authInterface.getAccessToken(token, new Verifier(_tokenKey));
        System.out.println("Authentication success");

        Auth auth = authInterface.checkToken(requestToken);

        _tokenVal = requestToken.getToken();
        _authConfig.updateAuthToken(_tokenVal);

        _secretStr = requestToken.getSecret();

        //Util.writeToFile(_tokenVal,"authTokenStorage.txt");

        // This token can be used until the user revokes it.
        System.out.println("Token: " + _tokenVal);
        System.out.println("Secret: " + _secretStr);
        System.out.println("TokenKey: " + _tokenKey);
        System.out.println("nsid: " + auth.getUser().getId());
        System.out.println("Realname: " + auth.getUser().getRealName());
        System.out.println("Username: " + auth.getUser().getUsername());
        System.out.println("Permission: " + auth.getPermission().getType());

    }


}
