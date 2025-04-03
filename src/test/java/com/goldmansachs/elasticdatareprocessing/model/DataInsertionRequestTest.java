package com.goldmansachs.elasticdatareprocessing.model;

import org.junit.jupiter.api.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for the DataInsertionRequest model class.
 */
class DataInsertionRequestTest {

    private final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    private final Validator validator = factory.getValidator();

    @Test
    void testValidRequest() {
        Map<String, Object> documentData = new HashMap<>();
        documentData.put("field1", "value1");
        documentData.put("field2", 123);
        
        DataInsertionRequest request = DataInsertionRequest.builder()
                .indexName("test-index")
                .documentId("test-doc-1")
                .documentData(documentData)
                .build();
        
        Set<ConstraintViolation<DataInsertionRequest>> violations = validator.validate(request);
        
        assertTrue(violations.isEmpty(), "Valid request should not have validation errors");
    }
    
    @Test
    void testMissingIndexName() {
        Map<String, Object> documentData = new HashMap<>();
        documentData.put("field1", "value1");
        
        DataInsertionRequest request = DataInsertionRequest.builder()
                .indexName(null) // Missing index name
                .documentData(documentData)
                .build();
        
        Set<ConstraintViolation<DataInsertionRequest>> violations = validator.validate(request);
        
        assertEquals(1, violations.size(), "Should have one validation error");
        ConstraintViolation<DataInsertionRequest> violation = violations.iterator().next();
        assertEquals("indexName", violation.getPropertyPath().toString(), "Error should be on indexName field");
        assertEquals("Index name is required", violation.getMessage(), "Error message should match");
    }
    
    @Test
    void testMissingDocumentData() {
        DataInsertionRequest request = DataInsertionRequest.builder()
                .indexName("test-index")
                .documentData(null) // Missing document data
                .build();
        
        Set<ConstraintViolation<DataInsertionRequest>> violations = validator.validate(request);
        
        assertEquals(1, violations.size(), "Should have one validation error");
        ConstraintViolation<DataInsertionRequest> violation = violations.iterator().next();
        assertEquals("documentData", violation.getPropertyPath().toString(), "Error should be on documentData field");
        assertEquals("Document data is required", violation.getMessage(), "Error message should match");
    }
    
    @Test
    void testEmptyIndexName() {
        Map<String, Object> documentData = new HashMap<>();
        documentData.put("field1", "value1");
        
        DataInsertionRequest request = DataInsertionRequest.builder()
                .indexName("") // Empty index name
                .documentData(documentData)
                .build();
        
        Set<ConstraintViolation<DataInsertionRequest>> violations = validator.validate(request);
        
        assertEquals(1, violations.size(), "Should have one validation error");
        ConstraintViolation<DataInsertionRequest> violation = violations.iterator().next();
        assertEquals("indexName", violation.getPropertyPath().toString(), "Error should be on indexName field");
        assertEquals("Index name is required", violation.getMessage(), "Error message should match");
    }
}
