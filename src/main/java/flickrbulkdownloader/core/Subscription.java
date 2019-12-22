package flickrbulkdownloader.core;


import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

import flickrbulkdownloader.extensions.ApiCallInvalidException;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class Subscription
{
    public String pathSubscriptionFile = "/home/user/subscriptions.xml";
    public List<String> userList;

    private ICrawler _crawler;

    public Subscription(ICrawler crawler)
    {
        _crawler = crawler;
        userList = extractSubscribedUsers();
    }

    public void crawlSubscribedUsers() throws IOException, SQLException, ParseException, ApiCallInvalidException {
        for(int i=0; i < userList.size(); i++)
        {
            _crawler.crawlAllPhotos(userList.get(i));
        }
    }

    private List<String> extractSubscribedUsers()
    {
        List<String> userList = new ArrayList<String>();
        try
        {
            File xmlFile = new File(pathSubscriptionFile);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(xmlFile);
            doc.getDocumentElement().normalize();

            Node usersNode = doc.getElementsByTagName("users").item(0);
            NodeList userNodeList = usersNode.getChildNodes();
            for (int i = 0; i < userNodeList.getLength(); i++)
            {
                Node userNode = userNodeList.item(i);
                if (userNode.getNodeType() == Node.ELEMENT_NODE)
                {
                    Element element = (Element) userNode;
                    userList.add(element.getTextContent());
                }
            }

        }
        catch (ParserConfigurationException e)
        {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return userList;
    }
}
