package com.goldmansachs.elasticdatareprocessing.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.goldmansachs.elasticdatareprocessing.model.ElasticProcessingRequest;
import com.goldmansachs.elasticdatareprocessing.model.ProcessingResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the ElasticsearchService class.
 * This class focuses on testing methods that don't require complex mocking of the Elasticsearch client.
 */
@ExtendWith(MockitoExtension.class)
public class ElasticsearchServiceTest {

    @Mock
    private ObjectMapper objectMapper;
    
    @Mock
    private ElasticsearchClient elasticsearchClient;

    @InjectMocks
    private ElasticsearchService elasticsearchService;

    @Test
    void testNormalizePath() throws Exception {
        Method normalizePathMethod = ElasticsearchService.class.getDeclaredMethod(
                "normalizePath", String.class);
        normalizePathMethod.setAccessible(true);

        assertEquals("/test/path", normalizePathMethod.invoke(elasticsearchService, "/test/path"));
        assertEquals("/test/path", normalizePathMethod.invoke(elasticsearchService, "test/path"));
    }

    @Test
    void testGetSampleRecords() {
        ElasticProcessingRequest request = ElasticProcessingRequest.builder()
                .sourceIndex("test-source")
                .targetIndex("test-target")
                .filterField("actionName")
                .filterValue("testAction")
                .build();
        
        ProcessingResult result = elasticsearchService.getSampleRecords(request);
        
        assertNotNull(result);
        assertNotNull(result.getSampleSourceRecords());
        assertFalse(result.getSampleSourceRecords().isEmpty());
        assertNotNull(result.getSampleProcessedRecords());
    }
    
    @Test
    void testProcessBatch() {
        ElasticProcessingRequest request = ElasticProcessingRequest.builder()
                .sourceIndex("test-source")
                .targetIndex("test-target")
                .filterField("actionName")
                .filterValue("testAction")
                .batchSize(10)
                .build();
        
        ProcessingResult result = elasticsearchService.processBatch(request);
        
        assertNotNull(result);
        assertNotNull(result.getMessage());
    }
    
    @Test
    void testGetDocument() {
        Map<String, Object> result = elasticsearchService.getDocument("test-index", "test-id");
        
        assertNotNull(result);
        assertEquals("test-id", result.get("id"));
        assertEquals("test-index", result.get("index"));
    }
}
