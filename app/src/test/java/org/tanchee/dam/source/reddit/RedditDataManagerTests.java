package org.tanchee.dam.source.reddit;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tanchee.dam.util.ResourceLoadingClass;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RedditDataManagerTests implements ResourceLoadingClass {

    private RedditCollectionManager manager;

    @Test
    void testInitA() {
        log.debug("wait is it working?????");
        try (InputStream input = streamResource("reddit-collection-manager-a.json")) {
            String content = new String(input.readAllBytes(), StandardCharsets.UTF_8);

            System.out.printf("content: %s", content);

            manager = new RedditCollectionManager(content, new LinkedBlockingDeque<>());

            List<SubRedditDataCollector> readCollectors = manager.getCollectors();
            System.out.printf("Intialized with %d collectors", readCollectors.size());
        } catch (Exception e) {}
    }
    
}
