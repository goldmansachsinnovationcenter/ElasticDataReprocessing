package com.goldmansachs.elasticdatareprocessing.functional;

import com.goldmansachs.elasticdatareprocessing.model.ElasticProcessingRequest;
import com.goldmansachs.elasticdatareprocessing.model.ProcessingResult;
import com.goldmansachs.elasticdatareprocessing.service.ElasticsearchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Functional tests for the Elasticsearch data processing workflow.
 * Tests the complete data processing flow from source to target index.
 */
@SpringBootTest
@ActiveProfiles("test")
public class ElasticDataProcessingFunctionalTest {

    @Autowired
    private ElasticsearchService elasticsearchService;

    private ElasticProcessingRequest request;

    @BeforeEach
    void setUp() {
        request = ElasticProcessingRequest.builder()
                .sourceIndex("test-source-index")
                .targetIndex("test-target-index")
                .filterField("actionName")
                .filterValue("testAction")
                .inputJsonField("jsonData")
                .masterJsonPath("$.data.master")
                .batchSize(100)
                .timestampField("timestamp")
                .build();
    }

    @Test
    void testCompleteDataProcessingWorkflow() {
        ProcessingResult sampleResult = elasticsearchService.getSampleRecords(request);
        
        assertNotNull(sampleResult);
        assertNotNull(sampleResult.getSampleSourceRecords());
        assertFalse(sampleResult.getSampleSourceRecords().isEmpty());
        
        List<Map<String, Object>> filteredRecords = sampleResult.getSampleProcessedRecords();
        assertNotNull(filteredRecords);
        
        for (Map<String, Object> record : filteredRecords) {
            assertEquals("testAction", record.get("actionName"));
        }
        
        ProcessingResult batchResult = elasticsearchService.processBatch(request);
        
        assertNotNull(batchResult);
        assertNotNull(batchResult.getSampleSourceRecords());
        assertNotNull(batchResult.getSampleProcessedRecords());
        
        assertTrue(batchResult.getTotalProcessed() > 0);
        assertTrue(batchResult.getTotalMatched() > 0);
        assertTrue(batchResult.getTotalMatched() <= batchResult.getTotalProcessed());
        
        for (Map<String, Object> record : batchResult.getSampleProcessedRecords()) {
            assertTrue(record.containsKey("master"));
            String masterValue = (String) record.get("master");
            assertNotNull(masterValue);
            
            assertTrue(masterValue.startsWith("value-") || masterValue.equals(""));
        }
        
        Map<String, Object> verificationRecord = batchResult.getVerificationRecord();
        assertNotNull(verificationRecord);
        assertTrue(verificationRecord.containsKey("master"));
        assertNotNull(verificationRecord.get("master"));
        
        ElasticProcessingRequest incrementalRequest = ElasticProcessingRequest.builder()
                .sourceIndex("test-source-index")
                .targetIndex("test-target-index")
                .filterField("actionName")
                .filterValue("testAction")
                .inputJsonField("jsonData")
                .masterJsonPath("$.data.master")
                .batchSize(100)
                .timestampField("timestamp")
                .lastTimestamp("1743512090000") // Set a timestamp to simulate incremental processing
                .build();
        
        ProcessingResult incrementalResult = elasticsearchService.processBatch(incrementalRequest);
        
        assertNotNull(incrementalResult);
        assertNotNull(incrementalResult.getLastProcessedTimestamp());
    }
    
    @Test
    void testBatchProcessingWithDifferentBatchSizes() {
        ElasticProcessingRequest smallBatchRequest = ElasticProcessingRequest.builder()
                .sourceIndex("test-source-index")
                .targetIndex("test-target-index")
                .filterField("actionName")
                .filterValue("testAction")
                .inputJsonField("jsonData")
                .masterJsonPath("$.data.master")
                .batchSize(2) // Small batch size
                .build();
        
        ProcessingResult smallBatchResult = elasticsearchService.processBatch(smallBatchRequest);
        assertNotNull(smallBatchResult);
        
        ElasticProcessingRequest largeBatchRequest = ElasticProcessingRequest.builder()
                .sourceIndex("test-source-index")
                .targetIndex("test-target-index")
                .filterField("actionName")
                .filterValue("testAction")
                .inputJsonField("jsonData")
                .masterJsonPath("$.data.master")
                .batchSize(500) // Large batch size
                .build();
        
        ProcessingResult largeBatchResult = elasticsearchService.processBatch(largeBatchRequest);
        assertNotNull(largeBatchResult);
        
        assertEquals(smallBatchResult.getTotalProcessed(), largeBatchResult.getTotalProcessed());
        assertEquals(smallBatchResult.getTotalMatched(), largeBatchResult.getTotalMatched());
    }
    
    @Test
    void testFilteringWithDifferentConditions() {
        ElasticProcessingRequest matchAllRequest = ElasticProcessingRequest.builder()
                .sourceIndex("test-source-index")
                .targetIndex("test-target-index")
                .filterField("id") // All records have an ID
                .filterValue("record") // All IDs contain "record"
                .inputJsonField("jsonData")
                .masterJsonPath("$.data.master")
                .batchSize(100)
                .build();
        
        ProcessingResult matchAllResult = elasticsearchService.getSampleRecords(matchAllRequest);
        
        ElasticProcessingRequest matchNoneRequest = ElasticProcessingRequest.builder()
                .sourceIndex("test-source-index")
                .targetIndex("test-target-index")
                .filterField("actionName")
                .filterValue("nonExistentAction")
                .inputJsonField("jsonData")
                .masterJsonPath("$.data.master")
                .batchSize(100)
                .build();
        
        ProcessingResult matchNoneResult = elasticsearchService.getSampleRecords(matchNoneRequest);
        
        assertTrue(matchAllResult.getSampleProcessedRecords().size() >= matchNoneResult.getSampleProcessedRecords().size());
        assertEquals(0, matchNoneResult.getSampleProcessedRecords().size());
    }
}
