package com.goldmansachs.elasticdatareprocessing.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.goldmansachs.elasticdatareprocessing.model.ElasticProcessingRequest;
import com.goldmansachs.elasticdatareprocessing.model.ProcessingResult;
import com.goldmansachs.elasticdatareprocessing.service.ElasticsearchService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ElasticProcessingController.class)
public class ElasticProcessingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ElasticsearchService elasticsearchService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testControllerEndpoints() throws Exception {
        ElasticProcessingRequest request = ElasticProcessingRequest.builder()
                .sourceIndex("source-index")
                .targetIndex("target-index")
                .build();

        List<Map<String, Object>> sampleRecords = new ArrayList<>();
        Map<String, Object> record = new HashMap<>();
        record.put("id", "1");
        record.put("actionName", "testAction");
        sampleRecords.add(record);

        ProcessingResult result = ProcessingResult.builder()
                .sampleSourceRecords(sampleRecords)
                .message("Test result")
                .build();

        when(elasticsearchService.getSampleRecords(any())).thenReturn(result);
        when(elasticsearchService.processBatch(any())).thenReturn(result);

        mockMvc.perform(post("/api/elastic/sample")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/elastic/process")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void testInvalidRequest() throws Exception {
        ElasticProcessingRequest invalidRequest = ElasticProcessingRequest.builder()
                .sourceIndex("") // Invalid: empty source index
                .targetIndex("target-index")
                .build();

        mockMvc.perform(post("/api/elastic/sample")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
}
