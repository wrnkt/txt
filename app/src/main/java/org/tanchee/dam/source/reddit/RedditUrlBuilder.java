package org.tanchee.dam.source.reddit;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

// import org.apache.commons.lang3.StringUtils;

public class RedditUrlBuilder {

    private static final String REDDIT_BASE_URL = "www.reddit.com";

    private String subreddit;
    private String requestType;
    private final Map<String, String> queryParams = new LinkedHashMap<>();

    private static final String TIME_FRAME_KEY = "t";
    private static final String LIMIT_KEY = "limit";
    private static final String SHOW_KEY = "show";

    public RedditUrlBuilder subreddit(String subreddit) {
        this.subreddit = subreddit;
        return this;
    }

    public RedditUrlBuilder requestType(String requestType) {
        this.requestType = requestType;
        return this;
    }

    public RedditUrlBuilder requestType(RequestType requestType) {
        this.requestType = requestType.name().toLowerCase();
        return this;
    }

    public RedditUrlBuilder addQueryParam(String k, String v) {
        queryParams.put(k, v);
        return this;
    }

    public RedditUrlBuilder timeFrame(String timeFrame) {
        return addQueryParam(TIME_FRAME_KEY, timeFrame);
    }

    public RedditUrlBuilder timeFrame(Timeframe timeFrame) {
        return addQueryParam(TIME_FRAME_KEY, timeFrame.getOptionName());
    }

    public RedditUrlBuilder limit(int limit) {
        return addQueryParam(LIMIT_KEY, String.valueOf(limit));
    }

    public RedditUrlBuilder show() {
        return addQueryParam(SHOW_KEY, "all");
    }

    public URL build() throws MalformedURLException, URISyntaxException {
        String path = String.format("/r/%s/%s.json", subreddit, requestType);

        String query = queryParams.entrySet().stream()
                        .map(e -> URLEncoder.encode(e.getKey(), StandardCharsets.UTF_8)
                                + "=" +
                                URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
                        .collect(Collectors.joining("&"));

        return new URI("https", REDDIT_BASE_URL, path, query, null).toURL();
    }

    /*
    public static String buildSubRedditUrl(String subreddit) {
        if (StringUtils.isBlank(subreddit))
            throw new IllegalArgumentException("subreddit cannot be blank");
        return String.format("%s/r/%s", REDDIT_BASE_URL, subreddit);
    }

    public static String buildSubRedditJsonUrl(String subreddit) {
        return String.format("%s/top.json", buildSubRedditUrl(subreddit), subreddit);
    }
    */
    
}
