package com.goldmansachs.elasticdatareprocessing.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.indices.CreateIndexResponse;
import co.elastic.clients.elasticsearch.indices.ExistsRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Service to initialize Elasticsearch with test data.
 * This service is only active in dev and test profiles.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Profile({"dev", "test"})
public class ElasticsearchInitService {

    private final ElasticsearchClient elasticsearchClient;

    /**
     * Initialize Elasticsearch with test data when the application is ready.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void initializeElasticsearch() {
        try {
            createIndicesIfNotExist();
            populateTestData();
            log.info("Elasticsearch initialized with test data");
        } catch (IOException e) {
            log.error("Failed to initialize Elasticsearch", e);
        }
    }

    /**
     * Create test indices if they don't exist.
     *
     * @throws IOException if there's an issue with Elasticsearch
     */
    private void createIndicesIfNotExist() throws IOException {
        String[] indices = {"test-source-index", "test-target-index"};
        
        for (String index : indices) {
            boolean exists = elasticsearchClient.indices()
                    .exists(ExistsRequest.of(e -> e.index(index)))
                    .value();
            
            if (!exists) {
                CreateIndexResponse response = elasticsearchClient.indices()
                        .create(c -> c.index(index));
                log.info("Created index {}: {}", index, response.acknowledged());
            } else {
                log.info("Index {} already exists", index);
            }
        }
    }

    /**
     * Populate test indices with sample data.
     *
     * @throws IOException if there's an issue with Elasticsearch
     */
    private void populateTestData() throws IOException {
        for (int i = 1; i <= 10; i++) {
            final int recordId = i;
            Map<String, Object> document = new HashMap<>();
            document.put("id", "record-" + recordId);
            document.put("timestamp", System.currentTimeMillis());
            document.put("actionName", recordId % 2 == 0 ? "testAction" : "otherAction");
            
            Map<String, Object> jsonData = new HashMap<>();
            Map<String, Object> data = new HashMap<>();
            data.put("master", "value-" + recordId);
            jsonData.put("data", data);
            document.put("jsonData", jsonData);
            
            elasticsearchClient.index(idx -> idx
                    .index("test-source-index")
                    .id("record-" + recordId)
                    .document(document)
            );
        }
        
        log.info("Populated test-source-index with 10 test records");
    }
}
