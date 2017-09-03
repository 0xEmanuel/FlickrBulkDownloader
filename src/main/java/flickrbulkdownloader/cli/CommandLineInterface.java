package flickrbulkdownloader.cli;

import java.io.IOException;
import java.sql.SQLException;

import flickrbulkdownloader.core.ICrawler;
import flickrbulkdownloader.core.IDatabaseHandler;
import org.apache.commons.cli.*;

public class CommandLineInterface
{
    //private static final Logger log = Logger.getLogger(CommandLineInterface.class.getName());
    private String[] args = null;
    private Options options = new Options();

    private ICrawler _crawler;

    //Options
    private final static String LIST_PHOTOS = "lp";
    private final static String LIST_USERS = "lu";

    private final static String REMOVE_PHOTOS = "rp";
    private final static String REMOVE_USERS = "ru";

    private final static String CRAWL_ALL = "c";
    private final static String CRAWL_SINGLE = "cs";

    public CommandLineInterface(String[] args, ICrawler crawler)
    {
        System.out.println("PWD: " + System.getProperty("user.dir"));
        _crawler = crawler;

        this.args = args;

        //false means its a flag and doesnt need an argument
        //true needs argument

        intializeOptions();


    }

    private void intializeOptions()
    {
        options.addOption("h", "help", false, "show help.");

        OptionBuilder.withArgName("crawlall=userId/username");
        OptionBuilder.withLongOpt("crawlall");
        OptionBuilder.hasArg(true);
        OptionBuilder.withValueSeparator();
        OptionBuilder.withDescription("Pass an userId or username as argument to crawl everything");
        options.addOption(OptionBuilder.create(CRAWL_ALL));

        OptionBuilder.withArgName("crawlsingle=mediaId");
        OptionBuilder.withLongOpt("crawlsingle");
        OptionBuilder.hasArg(true);
        OptionBuilder.withValueSeparator();
        OptionBuilder.withDescription("Pass a photoId/videoId as argument to crawl this single media");
        options.addOption(OptionBuilder.create( CRAWL_SINGLE ));

        OptionBuilder.withLongOpt("listusers");
        OptionBuilder.hasArg(false);
        OptionBuilder.withValueSeparator();
        OptionBuilder.withDescription("Lists all crawled users from database");
        options.addOption(OptionBuilder.create(LIST_USERS));

        OptionBuilder.withArgName("listphotos=userid");
        OptionBuilder.withLongOpt("listphotos");
        OptionBuilder.hasOptionalArgs(1);
        OptionBuilder.withValueSeparator();
        OptionBuilder.withDescription("Lists all crawled photos from database. If userId specified, it will only list entries of that user.");
        options.addOption(OptionBuilder.create(LIST_PHOTOS));

        OptionBuilder.withLongOpt("removeusers");
        OptionBuilder.hasArg(false);
        OptionBuilder.withValueSeparator();
        OptionBuilder.withDescription("Removes all crawled users from database");
        options.addOption(OptionBuilder.create(REMOVE_USERS));

        OptionBuilder.withArgName("removephotos=userid");
        OptionBuilder.withLongOpt("removephotos");
        OptionBuilder.hasOptionalArgs(1);
        OptionBuilder.withValueSeparator();
        OptionBuilder.withDescription("Removes all crawled photos from database. If userId specified, it will only remove entries of that user.");
        options.addOption(OptionBuilder.create(REMOVE_PHOTOS));
    }

    public void parse()
    {
        CommandLineParser parser = new BasicParser();

        CommandLine cmd = null;
        try
        {
            cmd = parser.parse(options, args);

            if (cmd.hasOption("h"))
                help();

            else if(cmd.hasOption(CRAWL_ALL))
            {
                String identification = cmd.getOptionValue(CRAWL_ALL);

                if(!(identification == null) )
                    _crawler.crawlAllPhotos(identification);
                else
                    System.out.println("No <userId/username> specified!");

            }
            else if(cmd.hasOption(CRAWL_SINGLE))
            {
                String photoId = cmd.getOptionValue(CRAWL_SINGLE);

                if(!(photoId == null) )
                    _crawler.crawlPhoto(photoId);
                else
                    System.out.println("No <mediaId> specified!");

            }
            else if(cmd.hasOption(LIST_PHOTOS))
            {
                String userId = cmd.getOptionValue(LIST_PHOTOS);
                IDatabaseHandler dbHandler = _crawler.getDatabaseHandler();

                if(!(userId == null))
                    dbHandler.OutputPhotoEntries(userId);
                else
                    dbHandler.OutputPhotoEntries();

            }
            else if(cmd.hasOption(LIST_USERS))
            {
                IDatabaseHandler dbHandler = _crawler.getDatabaseHandler();
                dbHandler.OutputUserEntries();
            }
            else if(cmd.hasOption(REMOVE_PHOTOS))
            {
                String userId = cmd.getOptionValue(REMOVE_PHOTOS);
                IDatabaseHandler dbHandler = _crawler.getDatabaseHandler();

                if(!(userId == null))
                    dbHandler.RemovePhotoEntries(userId);
                else
                    dbHandler.RemovePhotoEntries();

            }
            else if(cmd.hasOption(REMOVE_USERS))
            {
                IDatabaseHandler dbHandler = _crawler.getDatabaseHandler();
                dbHandler.RemoveUserEntries();
            }


            System.out.println("Job done!");

        }
        catch (ParseException e)
        {
            //log.log(Level.SEVERE, "Failed to parse comand line properties", e);
            help();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        //_crawler.close();
    }

    private void help()
    {
        // This prints out some help
        HelpFormatter formatter = new HelpFormatter();

        formatter.printHelp("Some help", options);
        System.exit(0);
    }
}
