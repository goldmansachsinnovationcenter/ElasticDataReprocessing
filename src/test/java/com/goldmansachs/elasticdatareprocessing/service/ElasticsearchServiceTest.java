package com.goldmansachs.elasticdatareprocessing.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.goldmansachs.elasticdatareprocessing.model.ElasticProcessingRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import javax.json.JsonObject;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ElasticsearchServiceTest {

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private ElasticsearchService elasticsearchService;

    @Test
    void testProcessRecord() throws Exception {
    }

    @Test
    void testNormalizePath() throws Exception {
        Method normalizePathMethod = ElasticsearchService.class.getDeclaredMethod(
                "normalizePath", String.class);
        normalizePathMethod.setAccessible(true);

        assertEquals("/test/path", normalizePathMethod.invoke(elasticsearchService, "/test/path"));
        assertEquals("/test/path", normalizePathMethod.invoke(elasticsearchService, "test/path"));
    }

    @Test
    void testBuildFilterQuery() throws Exception {
    }
}
