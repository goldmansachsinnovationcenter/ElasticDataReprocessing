package com.goldmansachs.elasticdatareprocessing.model;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for the DataInsertionResult model class.
 */
class DataInsertionResultTest {

    @Test
    void testSuccessfulResult() {
        Map<String, Object> documentData = new HashMap<>();
        documentData.put("field1", "value1");
        documentData.put("field2", 123);
        
        DataInsertionResult result = DataInsertionResult.builder()
                .documentId("test-doc-1")
                .documentData(documentData)
                .successful(true)
                .message("Document successfully inserted")
                .build();
        
        assertEquals("test-doc-1", result.getDocumentId(), "Document ID should match");
        assertEquals(documentData, result.getDocumentData(), "Document data should match");
        assertTrue(result.isSuccessful(), "Result should be successful");
        assertEquals("Document successfully inserted", result.getMessage(), "Message should match");
    }
    
    @Test
    void testFailedResult() {
        DataInsertionResult result = DataInsertionResult.builder()
                .successful(false)
                .message("Failed to insert document: index not found")
                .build();
        
        assertFalse(result.isSuccessful(), "Result should not be successful");
        assertEquals("Failed to insert document: index not found", result.getMessage(), "Error message should match");
    }
    
    @Test
    void testNoArgsConstructor() {
        DataInsertionResult result = new DataInsertionResult();
        
        assertFalse(result.isSuccessful(), "Default result should not be successful");
    }
    
    @Test
    void testSettersAndGetters() {
        Map<String, Object> documentData = new HashMap<>();
        documentData.put("field1", "value1");
        
        DataInsertionResult result = new DataInsertionResult();
        result.setDocumentId("test-doc-2");
        result.setDocumentData(documentData);
        result.setSuccessful(true);
        result.setMessage("Document inserted");
        
        assertEquals("test-doc-2", result.getDocumentId(), "Document ID should match");
        assertEquals(documentData, result.getDocumentData(), "Document data should match");
        assertTrue(result.isSuccessful(), "Result should be successful");
        assertEquals("Document inserted", result.getMessage(), "Message should match");
    }
}
