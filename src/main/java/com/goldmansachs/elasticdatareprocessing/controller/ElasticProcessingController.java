package com.goldmansachs.elasticdatareprocessing.controller;

import com.goldmansachs.elasticdatareprocessing.model.ElasticProcessingRequest;
import com.goldmansachs.elasticdatareprocessing.model.ProcessingResult;
import com.goldmansachs.elasticdatareprocessing.service.ElasticsearchService;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/elastic")
@RequiredArgsConstructor
@Slf4j
public class ElasticProcessingController {

    private final ElasticsearchService elasticsearchService;

    /**
     * Get sample records from source index based on filter criteria
     */
    @PostMapping("/sample")
    public ResponseEntity<ProcessingResult> getSampleRecords(@Valid @RequestBody ElasticProcessingRequest request) {
        log.info("Retrieving sample records from index: {}", request.getSourceIndex());
        ProcessingResult result = elasticsearchService.getSampleRecords(request);
        return ResponseEntity.ok(result);
    }

    /**
     * Process a batch of records from source index and save to target index
     */
    @PostMapping("/process")
    public ResponseEntity<ProcessingResult> processBatch(@Valid @RequestBody ElasticProcessingRequest request) {
        log.info("Processing batch from index: {} to index: {}", request.getSourceIndex(), request.getTargetIndex());
        ProcessingResult result = elasticsearchService.processBatch(request);
        return ResponseEntity.ok(result);
    }
}
