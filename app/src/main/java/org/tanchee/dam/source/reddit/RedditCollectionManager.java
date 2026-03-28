package org.tanchee.dam.source.reddit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tanchee.dam.data.Datum;
import org.tanchee.dam.util.json.JsonObjectFactory;

public class RedditCollectionManager implements Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(RedditCollectionManager.class);

    private BlockingQueue<Datum> outputQueue;

    private List<SubRedditDataCollector> collectors = new ArrayList<>();

    public RedditCollectionManager(String json, BlockingQueue<Datum> outputQueue) {
        this(outputQueue);
        try {
            List<Object> objects = JsonObjectFactory.createObjectsFromJson(json);
            System.out.printf("Read %d objects", objects.size());
            objects.stream()
                .map(o -> SubRedditDataCollector.class.cast(o))
                .forEach(c -> collectors.add(c));
        } catch (Exception e) {
            System.out.printf("%s", e.getMessage());
        }
    }

    public RedditCollectionManager(Properties properties, BlockingQueue<Datum> outputQueue) {
        this(outputQueue);
    }

    public RedditCollectionManager(BlockingQueue<Datum> outputQueue) {
        this.outputQueue = outputQueue;
    }

    @Override
    public void run() {
    }

    public List<SubRedditDataCollector> getCollectors() {
        return this.collectors;
    }
}
