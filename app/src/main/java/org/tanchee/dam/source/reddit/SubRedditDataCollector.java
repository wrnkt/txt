package org.tanchee.dam.source.reddit;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tanchee.dam.data.Datum;
import org.tanchee.dam.source.DataCollector;
import org.tanchee.dam.source.Source;
import org.tanchee.dam.util.http.HttpUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SubRedditDataCollector implements DataCollector {

    private static Logger LOG = LoggerFactory.getLogger(SubRedditDataCollector.class);

    private static final ObjectMapper mapper = new ObjectMapper();

    public static final String REDDIT_SRC_NAME = "reddit";

    public static final String DATA_KEY = "data";
    public static final String CHILDREN_KEY = "children";
    public static final String TITLE_KEY = "title";
    public static final String SCORE_KEY = "score";
    public static final String URL_KEY = "url";
    public static final String PERMALINK_KEY = "permalink";
    public static final String SELFTEXT_KEY = "selftext";
    public static final String AUTHOR_FULLNAME_KEY = "author_fullname";


    private final String subreddit;
    private final RequestType requestType;
    private final Timeframe timeframe;

    public SubRedditDataCollector(String subreddit, RequestType requestType) {
        this(subreddit, requestType, Timeframe.ALL);
    }

    public SubRedditDataCollector(String subreddit, RequestType requestType, Timeframe timeframe) {
        this.subreddit = subreddit;
        this.requestType = requestType;
        this.timeframe = timeframe;
    }

    public Source getSource() {
        return Source.REDDIT;
    }

    private String getSrcName() {
        return String.format("%s-%s", REDDIT_SRC_NAME, subreddit);
    }

    public List<Datum> fetch() {
        List<Datum> data = new ArrayList<>();

        try {
            RedditUrlBuilder urlBuilder = new RedditUrlBuilder()
                                            .subreddit(subreddit)
                                            .requestType(requestType)
                                            .timeFrame(timeframe);

            URL url = urlBuilder.build();

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("User-Agent", HttpUtils.APP_USER_AGENT);
            conn.setRequestMethod("GET");

            try (InputStream input = conn.getInputStream()) {
                JsonNode root = mapper.readTree(input);
                JsonNode posts = root.path(DATA_KEY).path(CHILDREN_KEY);

                for (JsonNode postNode : posts) {
                    Datum datum = processItemJson(postNode);
                    data.add(datum);
                }
            } catch (Exception e) {

            }
        } catch (Exception e) {

        }
        return data;
    }

    private void addMetadataEntry(Datum datum, JsonNode itemData, String key) {
        Optional<String> value = Optional.ofNullable(itemData.path(key).asText());
        if (value.isPresent()) {
            datum.addMetadataEntry(key, value.get());
        }
    }

    public Datum processItemJson(JsonNode itemNode) throws RuntimeException {
        JsonNode itemData = itemNode.path(DATA_KEY);
        
        Optional<String> titleOpt = Optional.ofNullable(itemData.path(TITLE_KEY).asText());
        if (titleOpt.isEmpty()) {
            LOG.info("No title found");

        }

        Optional<String> selftextOpt = Optional.ofNullable(itemData.path(SELFTEXT_KEY).asText());
        if (selftextOpt.isEmpty() && titleOpt.isEmpty()) {
            // ERROR: unable to extract text content
            throw new RuntimeException();
        }
        
        // TODO: Should I combine title\nselfttext for originalContent?
        //       right now I'm only using selftext and falling back on title

        Optional<String> urlOpt = Optional.ofNullable(itemData.path(URL_KEY).asText());
        if (urlOpt.isEmpty()) {
            throw new RuntimeException();
        }

        String primaryText;

        if (selftextOpt.isEmpty()) {
            if (titleOpt.isEmpty()) {
                throw new RuntimeException();
            } else {
                primaryText = titleOpt.get();
            }
        } else {
            primaryText = selftextOpt.get();
        }

        Datum datum = new Datum(primaryText, getSrcName(), urlOpt.get());

        addMetadataEntry(datum, itemData, TITLE_KEY);
        addMetadataEntry(datum, itemData, AUTHOR_FULLNAME_KEY);
        addMetadataEntry(datum, itemData, PERMALINK_KEY);
        addMetadataEntry(datum, itemData, SCORE_KEY);

        return datum;
    }

}
