package com.goldmansachs.elasticdatareprocessing.controller;

import com.goldmansachs.elasticdatareprocessing.model.DataInsertionRequest;
import com.goldmansachs.elasticdatareprocessing.model.DataInsertionResult;
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

/**
 * REST controller for Elasticsearch data processing operations.
 * Provides endpoints for retrieving sample records and processing data batches.
 */
@RestController
@RequestMapping("/api/elastic")
@RequiredArgsConstructor
@Slf4j
public final class ElasticProcessingController {

    /**
     * Service for Elasticsearch operations.
     */
    private final ElasticsearchService elasticsearchService;

    /**
     * Get sample records from source index based on filter criteria.
     *
     * @param request processing request with source index and filter criteria
     * @return response entity with sample records and processing info
     */
    @PostMapping("/sample")
    public ResponseEntity<ProcessingResult> getSampleRecords(
            @Valid @RequestBody final ElasticProcessingRequest request) {
        log.info("Retrieving sample records from index: {}",
                request.getSourceIndex());
        final ProcessingResult result =
                elasticsearchService.getSampleRecords(request);
        return ResponseEntity.ok(result);
    }

    /**
     * Process a batch of records from source index and save to target index.
     *
     * @param request processing request with source/target indices
     * @return response entity with processing results and verification
     */
    @PostMapping("/process")
    public ResponseEntity<ProcessingResult> processBatch(
            @Valid @RequestBody final ElasticProcessingRequest request) {
        log.info("Processing batch from index: {} to index: {}", 
                request.getSourceIndex(), request.getTargetIndex());
        final ProcessingResult result = elasticsearchService.processBatch(request);
        return ResponseEntity.ok(result);
    }
    
    /**
     * Insert a document into a specified Elasticsearch index.
     *
     * @param request insertion request with index name and document data
     * @return response entity with insertion result
     */
    @PostMapping("/insert")
    public ResponseEntity<DataInsertionResult> insertDocument(
            @Valid @RequestBody final DataInsertionRequest request) {
        log.info("Inserting document into index: {}", request.getIndexName());
        final DataInsertionResult result = elasticsearchService.insertDocument(request);
        return ResponseEntity.ok(result);
    }
}
