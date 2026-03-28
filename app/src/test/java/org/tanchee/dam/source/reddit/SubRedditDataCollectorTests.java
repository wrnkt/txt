package org.tanchee.dam.source.reddit;

import java.util.List;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.tanchee.dam.data.Datum;

public class SubRedditDataCollectorTests {

    @Test
    @Disabled
    void test_LPT_TOP_ALL() {
        String subreddit = "LifeProTips";

        System.out.printf("building collector for r/%s\n", subreddit);

        var collector = new SubRedditDataCollector(subreddit, RequestType.TOP, Timeframe.ALL);

        List<Datum> data = collector.fetch();

        System.out.printf("length of data: %d\n", data.size());

        data.forEach(System.out::println);
    }

    @Test
    @Disabled
    void test_LPT_NEW_ALL() {
        String subreddit = "LifeProTips";

        System.out.printf("building collector for r/%s\n", subreddit);

        var collector = new SubRedditDataCollector(subreddit, RequestType.NEW, Timeframe.ALL);

        List<Datum> data = collector.fetch();

        System.out.printf("length of data: %d\n", data.size());

        data.forEach(System.out::println);
    }
}
