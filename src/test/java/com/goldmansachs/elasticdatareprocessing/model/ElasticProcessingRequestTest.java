package com.goldmansachs.elasticdatareprocessing.model;

import org.junit.jupiter.api.Test;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import static org.junit.jupiter.api.Assertions.*;

public class ElasticProcessingRequestTest {

    private final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    private final Validator validator = factory.getValidator();

    @Test
    void testValidRequest() {
        ElasticProcessingRequest request = ElasticProcessingRequest.builder()
                .sourceIndex("source-index")
                .targetIndex("target-index")
                .filterField("actionName")
                .filterValue("testAction")
                .inputJsonField("data")
                .masterJsonPath("$.master")
                .batchSize(100)
                .build();

        var violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testInvalidRequest_MissingSourceIndex() {
        ElasticProcessingRequest request = ElasticProcessingRequest.builder()
                .sourceIndex("")
                .targetIndex("target-index")
                .build();

        var violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
    }

    @Test
    void testInvalidRequest_MissingTargetIndex() {
        ElasticProcessingRequest request = ElasticProcessingRequest.builder()
                .sourceIndex("source-index")
                .targetIndex("")
                .build();

        var violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
    }

    @Test
    void testBuilder() {
        ElasticProcessingRequest request = ElasticProcessingRequest.builder()
                .sourceIndex("source-index")
                .targetIndex("target-index")
                .filterField("actionName")
                .filterValue("testAction")
                .inputJsonField("data")
                .masterJsonPath("$.master")
                .batchSize(100)
                .timestampField("timestamp")
                .lastTimestamp("2023-01-01T00:00:00Z")
                .build();

        assertEquals("source-index", request.getSourceIndex());
        assertEquals("target-index", request.getTargetIndex());
        assertEquals("actionName", request.getFilterField());
        assertEquals("testAction", request.getFilterValue());
        assertEquals("data", request.getInputJsonField());
        assertEquals("$.master", request.getMasterJsonPath());
        assertEquals(100, request.getBatchSize());
        assertEquals("timestamp", request.getTimestampField());
        assertEquals("2023-01-01T00:00:00Z", request.getLastTimestamp());
    }
}
