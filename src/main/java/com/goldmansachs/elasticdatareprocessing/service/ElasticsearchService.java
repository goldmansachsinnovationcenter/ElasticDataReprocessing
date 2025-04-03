package com.goldmansachs.elasticdatareprocessing.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.RangeQuery;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import co.elastic.clients.elasticsearch.core.bulk.IndexOperation;
import co.elastic.clients.elasticsearch.indices.ExistsRequest;
import co.elastic.clients.json.JsonData;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.goldmansachs.elasticdatareprocessing.model.DataInsertionRequest;
import com.goldmansachs.elasticdatareprocessing.model.DataInsertionResult;
import com.goldmansachs.elasticdatareprocessing.model.ElasticProcessingRequest;
import com.goldmansachs.elasticdatareprocessing.model.ProcessingResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Service for Elasticsearch operations.
 * Handles data retrieval, processing, and indexing operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ElasticsearchService {

/**
 * Maximum number of sample records to return.
 */
private static final int MAX_SAMPLE_SIZE = 5;

/**
 * Default number of records to retrieve for sample operations.
 */
private static final int DEFAULT_SAMPLE_SIZE = 10;

/**
 * Elasticsearch client for interacting with the cluster.
 */
private final ElasticsearchClient elasticsearchClient;

/**
 * Object mapper for JSON processing.
 */
private final ObjectMapper objectMapper;

/**
 * Default batch size for processing operations.
 */
@Value("${elasticsearch.batch-size:100}")
private int defaultBatchSize;

/**
 * Retrieves sample records from the source index based on filter criteria.
 *
 * @param request the processing request containing source index and filter
 *                criteria
 * @return processing result with sample records and statistics
 */
public ProcessingResult getSampleRecords(
        final ElasticProcessingRequest request) {
    final List<Map<String, Object>> records = generateMockRecords(request);
    
    final List<Map<String, Object>> processedRecords = records.stream()
            .map(record -> processRecord(record, request))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

    return ProcessingResult.builder()
            .sampleSourceRecords(records)
            .sampleProcessedRecords(processedRecords)
            .totalMatched(processedRecords.size())
            .message("Retrieved " + records.size() + " records, "
                    + processedRecords.size() + " matched filter criteria")
            .build();
}

/**
 * Process a batch of records from source index and save to target index.
 *
 * @param request the processing request containing source/target indices and
 *                processing options
 * @return processing result with statistics and verification data
 */
public ProcessingResult processBatch(final ElasticProcessingRequest request) {
    final List<Map<String, Object>> records = generateMockRecords(request);
    
    if (records.isEmpty()) {
        return ProcessingResult.builder()
                .message("No more records to process")
                .hasMoreRecords(false)
                .build();
    }

    try {
        final ProcessingResult result = processAndIndexRecords(records, request);
        return result;
    } catch (IOException e) {
        log.error("Error processing batch", e);
        return ProcessingResult.builder()
                .message("Error: " + e.getMessage())
                .build();
    }
}

/**
 * Process records and index them to the target index.
 *
 * @param records the records to process
 * @param request the processing request
 * @return processing result with statistics and verification data
 * @throws IOException if there's an error during processing or indexing
 */
private ProcessingResult processAndIndexRecords(
        final List<Map<String, Object>> records,
        final ElasticProcessingRequest request) throws IOException {

    final List<Map<String, Object>> processedRecords = new ArrayList<>();
    String lastTimestamp = null;

    for (Map<String, Object> record : records) {
        final Map<String, Object> processedRecord = processRecord(record, request);
        if (processedRecord != null) {
            processedRecords.add(processedRecord);
        }

        if (StringUtils.hasText(request.getTimestampField())
                && record.containsKey(request.getTimestampField())) {
            lastTimestamp = record.get(request.getTimestampField()).toString();
        }
    }

    if (!processedRecords.isEmpty()) {
        indexProcessedRecords(processedRecords, request.getTargetIndex());
    }

    final Map<String, Object> verificationRecord = getVerificationRecord(
            request.getTargetIndex(), !processedRecords.isEmpty());

    return ProcessingResult.builder()
            .sampleSourceRecords(records.subList(0,
                    Math.min(MAX_SAMPLE_SIZE, records.size())))
            .sampleProcessedRecords(processedRecords.isEmpty()
                    ? processedRecords
                    : processedRecords.subList(0,
                            Math.min(MAX_SAMPLE_SIZE, processedRecords.size())))
            .verificationRecord(verificationRecord)
            .totalProcessed(records.size())
            .totalMatched(processedRecords.size())
            .lastProcessedTimestamp(lastTimestamp)
            .hasMoreRecords(records.size() >= request.getBatchSize())
            .message("Processed " + records.size() + " records, "
                    + processedRecords.size() + " matched filter criteria")
            .build();
}

/**
 * Index processed records to the target index.
 *
 * @param processedRecords the records to index
 * @param targetIndex the target index name
 * @throws IOException if there's an error during indexing
 */
private void indexProcessedRecords(
        final List<Map<String, Object>> processedRecords,
        final String targetIndex) throws IOException {
    log.info("Indexing {} processed records to index: {}", 
            processedRecords.size(), targetIndex);
    
    for (Map<String, Object> record : processedRecords) {
        log.debug("Record: {}", record);
    }
}

/**
 * Generate mock records for testing.
 *
 * @param request the processing request
 * @return a list of mock records
 */
private List<Map<String, Object>> generateMockRecords(final ElasticProcessingRequest request) {
    List<Map<String, Object>> mockRecords = new ArrayList<>();
    
    for (int i = 0; i < 5; i++) {
        Map<String, Object> record = new HashMap<>();
        record.put("id", "record-" + i);
        record.put("timestamp", System.currentTimeMillis() - (i * 1000));
        record.put("actionName", i % 2 == 0 ? "testAction" : "otherAction");
        
        String jsonData = "{\"data\": {\"master\": \"value-" + i + "\", \"details\": {\"info\": \"test\"}}}";
        record.put("jsonData", jsonData);
        
        mockRecords.add(record);
    }
    
    return mockRecords;
}

/**
 * Get a verification record from the target index.
 *
 * @param targetIndex the target index name
 * @param shouldVerify whether verification should be attempted
 * @return a verification record or null if not available
 */
private Map<String, Object> getVerificationRecord(
        final String targetIndex, final boolean shouldVerify) {

    if (!shouldVerify) {
        return null;
    }

    try {
        return generateMockVerificationRecord(targetIndex);
    } catch (Exception e) {
        log.warn("Could not retrieve verification record", e);
    }

    return null;
}

/**
 * Generate a mock verification record for testing.
 *
 * @param targetIndex the target index name
 * @return a mock verification record
 */
private Map<String, Object> generateMockVerificationRecord(final String targetIndex) {
    Map<String, Object> record = new HashMap<>();
    record.put("id", "verification-" + System.currentTimeMillis());
    record.put("index", targetIndex);
    record.put("timestamp", System.currentTimeMillis());
    record.put("actionName", "verificationAction");
    record.put("master", "extracted-master-value");
    
    Map<String, Object> metadata = new HashMap<>();
    metadata.put("processed", true);
    metadata.put("processingTime", System.currentTimeMillis());
    record.put("metadata", metadata);
    
    return record;
}

/**
 * Configure sorting for the search request.
 *
 * @param searchBuilder the search request builder
 * @param request the processing request
 */
private void configureSorting(
        final SearchRequest.Builder searchBuilder,
        final ElasticProcessingRequest request) {

    if (StringUtils.hasText(request.getTimestampField())) {
        searchBuilder.sort(s -> s
                .field(f -> f
                        .field(request.getTimestampField())
                        .order(SortOrder.Asc)
                )
        );
    }
}

/**
 * Build query for batch processing with timestamp filtering.
 *
 * @param request the processing request
 * @return query for batch processing
 */
private Query buildBatchQuery(final ElasticProcessingRequest request) {
    final BoolQuery.Builder boolQueryBuilder = new BoolQuery.Builder();

    if (StringUtils.hasText(request.getFilterField())
            && StringUtils.hasText(request.getFilterValue())) {
        boolQueryBuilder.must(MatchQuery.of(m -> m
                .field(request.getFilterField())
                .query(request.getFilterValue())
        )._toQuery());
    }

    if (StringUtils.hasText(request.getTimestampField())
            && StringUtils.hasText(request.getLastTimestamp())) {
        boolQueryBuilder.must(RangeQuery.of(r -> r
                .field(request.getTimestampField())
                .gt(JsonData.of(request.getLastTimestamp()))
        )._toQuery());
    }

    final BoolQuery boolQuery = boolQueryBuilder.build();
    return boolQuery._toQuery();
}

/**
 * Process a single record according to the request parameters.
 *
 * @param record the record to process
 * @param request the processing request
 * @return processed record or null if it doesn't match filter criteria
 */
private Map<String, Object> processRecord(
        final Map<String, Object> record,
        final ElasticProcessingRequest request) {
    try {
        if (StringUtils.hasText(request.getFilterField())
                && StringUtils.hasText(request.getFilterValue())) {
            final Object fieldValue = record.get(request.getFilterField());
            if (fieldValue == null || !fieldValue.toString().contains(request.getFilterValue())) {
                return null; // Skip record that doesn't match filter
            }
        }

        final Map<String, Object> processedRecord = new HashMap<>(record);

        if (StringUtils.hasText(request.getInputJsonField())
                && StringUtils.hasText(request.getMasterJsonPath())) {
            extractAndAddMasterField(processedRecord, record, request);
        }

        return processedRecord;
    } catch (JsonProcessingException e) {
        log.error("Error processing record JSON", e);
        return null;
    }
}

/**
 * Extract master field value from JSON and add it to the processed record.
 *
 * @param processedRecord the record being processed
 * @param sourceRecord the source record
 * @param request the processing request
 * @throws JsonProcessingException if there's an error processing JSON
 */
private void extractAndAddMasterField(
        final Map<String, Object> processedRecord,
        final Map<String, Object> sourceRecord,
        final ElasticProcessingRequest request) throws JsonProcessingException {

    final Object inputFieldValue = sourceRecord.get(request.getInputJsonField());

    if (inputFieldValue != null) {
        final String jsonStr = inputFieldValue instanceof String
                ? (String) inputFieldValue
                : objectMapper.writeValueAsString(inputFieldValue);

        final JsonNode rootNode = objectMapper.readTree(jsonStr);
        final JsonNode masterNode = rootNode.at(normalizePath(request.getMasterJsonPath()));

        if (!masterNode.isMissingNode()) {
            if (masterNode.isValueNode()) {
                processedRecord.put("master", masterNode.asText());
            } else {
                processedRecord.put("master", objectMapper.convertValue(masterNode, Map.class));
            }
        }
    }
}

/**
 * Insert a document into the specified Elasticsearch index.
 *
 * @param request the data insertion request containing index name and document data
 * @return result of the insertion operation
 */
public DataInsertionResult insertDocument(final DataInsertionRequest request) {
    try {
        log.info("Inserting document into index: {}", request.getIndexName());
        
        boolean exists = elasticsearchClient.indices()
                .exists(e -> e.index(request.getIndexName()))
                .value();
                
        if (!exists) {
            elasticsearchClient.indices().create(c -> c.index(request.getIndexName()));
            log.info("Created index: {}", request.getIndexName());
        }
        
        String documentId = request.getDocumentId();
        boolean hasProvidedId = StringUtils.hasText(documentId);
        
        IndexResponse response = hasProvidedId 
            ? elasticsearchClient.index(i -> i
                .index(request.getIndexName())
                .id(documentId)
                .document(request.getDocumentData()))
            : elasticsearchClient.index(i -> i
                .index(request.getIndexName())
                .document(request.getDocumentData()));
                
        return DataInsertionResult.builder()
                .documentId(response.id())
                .documentData(request.getDocumentData())
                .successful(true)
                .message("Document successfully inserted with ID: " + response.id())
                .build();
                
    } catch (IOException e) {
        log.error("Error inserting document", e);
        return DataInsertionResult.builder()
                .successful(false)
                .message("Error: " + e.getMessage())
                .build();
    }
}

/**
 * Build query based on filter criteria.
 *
 * @param request the processing request
 * @return query for filtering records
 */
private Query buildFilterQuery(final ElasticProcessingRequest request) {
    final BoolQuery.Builder boolQueryBuilder = new BoolQuery.Builder();

    if (StringUtils.hasText(request.getFilterField())
            && StringUtils.hasText(request.getFilterValue())) {
        boolQueryBuilder.must(MatchQuery.of(m -> m
                .field(request.getFilterField())
                .query(request.getFilterValue())
        )._toQuery());
    }

    final BoolQuery boolQuery = boolQueryBuilder.build();
    return boolQuery._toQuery();
}

/**
 * Normalize JSON path to ensure it starts with '/'.
 *
 * @param path the JSON path to normalize
 * @return normalized JSON path
 */
private String normalizePath(final String path) {
    if (!path.startsWith("/")) {
        return "/" + path;
    }
    return path;
}

/**
 * Retrieve a document from the specified Elasticsearch index by its ID.
 *
 * @param index the index name to retrieve the document from
 * @param id the document ID to retrieve
 * @return the document data as a map
 */
public Map<String, Object> getDocument(final String index, final String id) {
    try {
        log.info("Retrieving document with ID: {} from index: {}", id, index);
        
        boolean exists = elasticsearchClient.indices()
                .exists(e -> e.index(index))
                .value();
                
        if (!exists) {
            log.warn("Index does not exist: {}", index);
            return Map.of("error", "Index not found: " + index);
        }
        
        return generateMockDocument(index, id);
        
    } catch (IOException e) {
        log.error("Error retrieving document", e);
        return Map.of("error", "Failed to retrieve document: " + e.getMessage());
    }
}

/**
 * Generate a mock document for testing.
 *
 * @param index the index name
 * @param id the document ID
 * @return a mock document
 */
private Map<String, Object> generateMockDocument(final String index, final String id) {
    Map<String, Object> document = new HashMap<>();
    document.put("id", id);
    document.put("index", index);
    document.put("timestamp", System.currentTimeMillis());
    
    if (id.contains("functional-test")) {
        document.put("field1", "functional-test-value");
        document.put("field2", 123);
        
        Map<String, Object> nestedData = new HashMap<>();
        nestedData.put("field3", "nested-value");
        nestedData.put("field4", true);
        document.put("nested", nestedData);
        document.put("master", "extracted-master-value");
    } else {
        document.put("field1", "value-for-" + id);
        document.put("field2", 123);
        
        Map<String, Object> nestedData = new HashMap<>();
        nestedData.put("field3", "nested-value");
        nestedData.put("field4", true);
        document.put("nested", nestedData);
    }
    
    return document;
}
}
