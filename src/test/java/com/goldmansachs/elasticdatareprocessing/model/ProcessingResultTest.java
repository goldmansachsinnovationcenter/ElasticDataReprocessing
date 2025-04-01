package com.goldmansachs.elasticdatareprocessing.model;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

public class ProcessingResultTest {

    @Test
    void testBuilder() {
        List<Map<String, Object>> sourceRecords = new ArrayList<>();
        Map<String, Object> sourceRecord = new HashMap<>();
        sourceRecord.put("id", "1");
        sourceRecord.put("actionName", "testAction");
        sourceRecords.add(sourceRecord);

        List<Map<String, Object>> processedRecords = new ArrayList<>();
        Map<String, Object> processedRecord = new HashMap<>();
        processedRecord.put("id", "1");
        processedRecord.put("actionName", "testAction");
        processedRecord.put("master", "masterValue");
        processedRecords.add(processedRecord);

        Map<String, Object> verificationRecord = new HashMap<>();
        verificationRecord.put("id", "verification-1");
        verificationRecord.put("master", "verificationMasterValue");

        ProcessingResult result = ProcessingResult.builder()
                .sampleSourceRecords(sourceRecords)
                .sampleProcessedRecords(processedRecords)
                .verificationRecord(verificationRecord)
                .totalProcessed(10)
                .totalMatched(5)
                .lastProcessedTimestamp("2023-01-01T00:00:00Z")
                .hasMoreRecords(true)
                .message("Test message")
                .build();

        assertEquals(sourceRecords, result.getSampleSourceRecords());
        assertEquals(processedRecords, result.getSampleProcessedRecords());
        assertEquals(verificationRecord, result.getVerificationRecord());
        assertEquals(10, result.getTotalProcessed());
        assertEquals(5, result.getTotalMatched());
        assertEquals("2023-01-01T00:00:00Z", result.getLastProcessedTimestamp());
        assertTrue(result.isHasMoreRecords());
        assertEquals("Test message", result.getMessage());
    }

    @Test
    void testNoArgsConstructor() {
        ProcessingResult result = new ProcessingResult();
        assertNull(result.getSampleSourceRecords());
        assertNull(result.getSampleProcessedRecords());
        assertNull(result.getVerificationRecord());
        assertEquals(0, result.getTotalProcessed());
        assertEquals(0, result.getTotalMatched());
        assertNull(result.getLastProcessedTimestamp());
        assertFalse(result.isHasMoreRecords());
        assertNull(result.getMessage());
    }

    @Test
    void testAllArgsConstructor() {
        List<Map<String, Object>> sourceRecords = new ArrayList<>();
        List<Map<String, Object>> processedRecords = new ArrayList<>();
        Map<String, Object> verificationRecord = new HashMap<>();

        ProcessingResult result = new ProcessingResult(
                sourceRecords,
                processedRecords,
                verificationRecord,
                10,
                5,
                "2023-01-01T00:00:00Z",
                true,
                "Test message"
        );

        assertEquals(sourceRecords, result.getSampleSourceRecords());
        assertEquals(processedRecords, result.getSampleProcessedRecords());
        assertEquals(verificationRecord, result.getVerificationRecord());
        assertEquals(10, result.getTotalProcessed());
        assertEquals(5, result.getTotalMatched());
        assertEquals("2023-01-01T00:00:00Z", result.getLastProcessedTimestamp());
        assertTrue(result.isHasMoreRecords());
        assertEquals("Test message", result.getMessage());
    }

    @Test
    void testSettersAndGetters() {
        ProcessingResult result = new ProcessingResult();
        
        List<Map<String, Object>> sourceRecords = new ArrayList<>();
        result.setSampleSourceRecords(sourceRecords);
        
        List<Map<String, Object>> processedRecords = new ArrayList<>();
        result.setSampleProcessedRecords(processedRecords);
        
        Map<String, Object> verificationRecord = new HashMap<>();
        result.setVerificationRecord(verificationRecord);
        
        result.setTotalProcessed(10);
        result.setTotalMatched(5);
        result.setLastProcessedTimestamp("2023-01-01T00:00:00Z");
        result.setHasMoreRecords(true);
        result.setMessage("Test message");

        assertEquals(sourceRecords, result.getSampleSourceRecords());
        assertEquals(processedRecords, result.getSampleProcessedRecords());
        assertEquals(verificationRecord, result.getVerificationRecord());
        assertEquals(10, result.getTotalProcessed());
        assertEquals(5, result.getTotalMatched());
        assertEquals("2023-01-01T00:00:00Z", result.getLastProcessedTimestamp());
        assertTrue(result.isHasMoreRecords());
        assertEquals("Test message", result.getMessage());
    }
}
