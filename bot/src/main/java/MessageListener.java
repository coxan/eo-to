import com.github.jreddit.entity.Submission;
import com.github.jreddit.oauth.RedditOAuthAgent;
import com.github.jreddit.oauth.RedditToken;
import com.github.jreddit.oauth.app.RedditApp;
import com.github.jreddit.oauth.app.RedditInstalledApp;
import com.github.jreddit.oauth.client.RedditClient;
import com.github.jreddit.oauth.client.RedditHttpClient;
import com.github.jreddit.oauth.exception.RedditOAuthException;
import com.github.jreddit.parser.entity.Thing;
import com.github.jreddit.parser.exception.RedditParseException;
import com.github.jreddit.parser.listing.SubmissionsListingParser;
import com.github.jreddit.request.retrieval.submissions.SubmissionsOfSubredditRequest;
import com.github.jreddit.retrieval.params.SubmissionSort;
import com.google.gson.Gson;
import com.jagrosh.jdautilities.menu.Paginator;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.dv8tion.jda.core.requests.restaction.MessageAction;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.impl.client.HttpClientBuilder;

import javax.security.auth.login.LoginException;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class MessageListener extends ListenerAdapter {
    private static final int DEFAULT_RANGE = 100;
    private List<Coffee> coffeeList = new ArrayList<>();
    private Paginator.Builder pb;
    private SubmissionsOfSubredditRequest request;

    public static void main(String[] args)
            throws LoginException, RateLimitedException, InterruptedException {
        JDA jda = new JDABuilder(AccountType.BOT)
                .setToken("NDY3NTUzOTA4MDU1NjA1MjU4.Diuv1Q.JF2PawTZJYIlUaBMKCZMWybe35M")
                .buildBlocking();
        jda.addEventListener(new MessageListener());
    }



    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        String input = event.getMessage().getContentStripped();
        MessageChannel channel = event.getMessage().getChannel();

        if (!event.getAuthor().isBot()) {
            response(input, channel);
            System.out.println(String.format("Input: %s", input));
        }

    }

    private void response(String input, MessageChannel channel){
        String command = input.substring(1).toLowerCase();
        if (input.startsWith("!")){
            if (command.startsWith("roll")){
                long range = DEFAULT_RANGE;
                if (command.length() > 5 && StringUtils.isNumeric(command.substring(5))) {
                    range = Long.parseLong(command.substring(5));
                    if (range > 0){
                        channel.sendMessage(String.format("%s", ThreadLocalRandom.current().nextLong(range))).queue();
                    }else{
                        channel.sendMessage("Range must be an integer larger than 0.").queue();
                    }
                }else{
                    boolean isNumber = true;
                    for (char x : command.substring(5).toCharArray()){
                        if (!"0123456789.".contains(String.valueOf(x))) {
                            isNumber = false;
                        }
                    }
                    if (isNumber){
                        channel.sendMessage("Range must be an integer larger than 0.").queue();
                    }else{
                        channel.sendMessage(String.format("%s", ThreadLocalRandom.current().nextLong(range))).queue();
                    }
                }
            }else if (command.startsWith("poem")) {
                channel.sendMessage("").queue();
            }else if (command.startsWith("search")){
                File result = null;
                try {
                    result = search(command.substring(7));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (result != null){
                    channel.sendFile(result).queue();
                }else{
                    channel.sendMessage("No results found :(").queue();
                }
            }else if (command.startsWith("coffee") || command.startsWith("kaffee")){
                //TODO
            }else if (command.startsWith("diks")
                    || command.startsWith("dicks")
                    || command.startsWith("dick")
                    || command.startsWith("dik")
                    || command.startsWith("cock")
                    || command.startsWith("suck")
                    || command.startsWith("succ")){
                List<String> responses = new ArrayList<>();
                responses.add("Do you succ diks?");
                responses.add("Do you suck diks?");
                responses.add("Do you succ dicks?");
                responses.add("Do you suck dicks?");
                responses.add("Do you succ dik?");
                responses.add("Do you suck dik?");
                responses.add(":regional_indicator_d: :regional_indicator_i: :regional_indicator_k: :regional_indicator_s:\n" +
                        ":regional_indicator_i:                       :regional_indicator_u:\n" +
                        ":regional_indicator_k:                       :regional_indicator_c:\n" +
                        ":regional_indicator_s: :regional_indicator_u: :regional_indicator_c: :regional_indicator_k:");
                responses.add("__Untitled poem__\n" +
                        "\n" +
                        "Do you suck dicks?\n" +
                        "Dicks suck you do.\n" +
                        "\n" +
                        "Young bucks are let free,\n" +
                        "As I slay pussies.\n" +
                        "\n" +
                        "They call them sissies,\n" +
                        "I call them bitches.\n" +
                        "\n" +
                        "                - Richard Knight, Dear Mr. Kilmer (1999)");

                channel.sendMessage(responses.get(new Random().nextInt(responses.size()-1))).queue();
            }else if (command.startsWith("help")){
                channel.sendMessage("```!roll [limit] - rolls a number from 0 to 100\n" +
                        "!search [query] - fetches an image from google\n" +
                        "!memes - gets a meme" +
                        "!help - this" +
                        "```"
                ).queue();
            }else if (command.startsWith("communism")){
                channel.sendMessage("Profits are the unpaid wages of the workers ").queue();
            }else if (command.startsWith("memes")){
                try {
                    channel.sendFile(memes()).queue();
                } catch (RedditOAuthException e) {
                    System.err.println("Error: Unable to authenticate with reddit.");
                } catch (RedditParseException e) {
                    System.err.println("Error: Unable to parse reddit reponses.");
                }
            }
        }
    }

    private File search(String query) throws IOException {
        String key = "AIzaSyAHf4BvhDOHJwb9iPtLLvNW9sKKKTceWjs";
        String qry = query.replace(" ", "+");
        String cx  = "017261301791384958793:r4g8kzsv5ty";
        String fileType = "png,jpg";
        String searchType = "image";
        URL url = new URL ("https://www.googleapis.com/customsearch/v1?key=" +key+ "&cx=" +cx+ "&q=" +qry+"&fileType="+fileType+"&searchType="+searchType+"&alt=json");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");
        BufferedReader br = new BufferedReader(new InputStreamReader( ( conn.getInputStream() ) ) );
        GResults results = new Gson().fromJson(br, GResults.class);
        conn.disconnect();

        String acceptedChars = "\\/:*\"<>|";
        String queryFileName = query;
        for (char x : acceptedChars.toCharArray()){
            queryFileName = queryFileName.replace(x, Character.MIN_VALUE);
        }

        File result = null;
        for (int i=0; i < 10; i++) {
            String path = "";
            try {
                path  = results.getLink(i);
            }catch(NullPointerException e){
                break;
            }
            File file = new File(String.format(
                    "%s/imageCache/%s%s",
                    System.getProperty("user.dir"),
                    queryFileName,
                    path.substring(path.lastIndexOf("."))
            ));

            if (!file.exists()) {
                try {
                    FileUtils.copyURLToFile(
                            new URL(path),
                            file
                    );
                } catch (IOException e) {
                    System.err.println("Something went wrong with the image.");
                }
            }

            if (file.length() < Message.MAX_FILE_SIZE
                    && (file.getName()
                    .substring(file.getName().lastIndexOf('.')+1)
                    .equalsIgnoreCase("jpg")
                    || file.getName()
                    .substring(file.getName().lastIndexOf('.')+1)
                    .equalsIgnoreCase("png")
                    || file.getName()
                    .substring(file.getName().lastIndexOf('.')+1)
                    .equalsIgnoreCase("gif")
                    || file.getName()
                    .substring(file.getName().lastIndexOf('.')+1)
                    .equalsIgnoreCase("jpeg"))){
                result = file;
                break;
            }
        }

        return result;
    }


    public File memes() throws RedditOAuthException, RedditParseException {
        System.out.println("memes is run");
        String userAgent = "eo-to";
        String clientID = "0BlY1z2yAJmdRg";
        String redirectURI = "https://github.com/coxan/eo-to";

        RedditApp redditApp = new RedditInstalledApp(clientID, redirectURI);
        RedditOAuthAgent agent = new RedditOAuthAgent(userAgent, redditApp);
        RedditClient client = new RedditHttpClient(userAgent, HttpClientBuilder.create().build());
        RedditToken token = agent.tokenAppOnly(false);
        SubmissionsListingParser parser = new SubmissionsListingParser();

        request = (SubmissionsOfSubredditRequest)
                new SubmissionsOfSubredditRequest("memes", com.github.jreddit.request.retrieval.param.SubmissionSort.HOT)
                        .setLimit(50);
        List<com.github.jreddit.parser.entity.Submission> submissions = parser.parse(client.get(token, request));

        File meme = new File(String.format("%s/default", System.getProperty("user.dir")));
        if (!meme.exists()) {
            try {
                meme.createNewFile();
            }catch(IOException e){
                System.err.println("Error: file cannot be created");
            }
        }

        String url = "";
        Thing thing = null;

        while (meme.exists()) {
            for (com.github.jreddit.parser.entity.Submission submission : submissions){
                url = submission.getURL();
                if (!submission.getURL().isEmpty() && makeName(url) != null){
                    meme = new File(makeName(url));
                    if (!meme.exists()){
                        System.out.println(meme.getAbsolutePath());
                        break;
                    }
                }
                thing = new Thing(submission.getFullName()) {
                    @Override
                    public int compareTo(Thing o) {
                        return 0;
                    }
                };
            }
            refresh(thing);
        }

        try {
            FileUtils.copyURLToFile(new URL(url), meme);
        } catch (IOException e) {
            System.err.println("URL is invalid");
        }

        return meme;
    }

    private String makeName(String input){
        String fileName = null;
        if (input.startsWith("https://i.redd.it/") && input.contains("/")) {
            fileName = String.format(
                    "%s/%s",
                    System.getProperty("user.dir"),
                    input.substring(input.lastIndexOf('/')+1
                    ));
        }

        return fileName;
    }

    private void refresh(Thing thing){
        if (thing != null) {
            request = (SubmissionsOfSubredditRequest)
                    new SubmissionsOfSubredditRequest(
                            "memes",
                            com.github.jreddit.request.retrieval.param.SubmissionSort.HOT
                    )
                            .setLimit(100)
                            .setAfter(thing);
        }
    }
}