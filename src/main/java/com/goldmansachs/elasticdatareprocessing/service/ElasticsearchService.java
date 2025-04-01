package com.goldmansachs.elasticdatareprocessing.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.RangeQuery;
import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import co.elastic.clients.elasticsearch.core.bulk.IndexOperation;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.JsonData;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.goldmansachs.elasticdatareprocessing.model.ElasticProcessingRequest;
import com.goldmansachs.elasticdatareprocessing.model.ProcessingResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ElasticsearchService {

    private final ElasticsearchClient elasticsearchClient;
    private final ObjectMapper objectMapper;

    @Value("${elasticsearch.batch-size:100}")
    private int defaultBatchSize;

    /**
     * Retrieves sample records from the source index based on filter criteria
     */
    public ProcessingResult getSampleRecords(ElasticProcessingRequest request) {
        try {
            int batchSize = request.getBatchSize() != null ? request.getBatchSize() : 10;
            
            Query query = buildFilterQuery(request);
            
            SearchResponse<Map> response = elasticsearchClient.search(s -> s
                    .index(request.getSourceIndex())
                    .query(query)
                    .size(batchSize), 
                    Map.class);
            
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> records = response.hits().hits().stream()
                    .map(hit -> (Map<String, Object>) hit.source())
                    .collect(Collectors.toList());
            
            List<Map<String, Object>> processedRecords = records.stream()
                    .map(record -> processRecord(record, request))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            
            return ProcessingResult.builder()
                    .sampleSourceRecords(records)
                    .sampleProcessedRecords(processedRecords)
                    .totalMatched(processedRecords.size())
                    .message("Retrieved " + records.size() + " records, " + processedRecords.size() + " matched filter criteria")
                    .build();
            
        } catch (IOException e) {
            log.error("Error retrieving sample records", e);
            return ProcessingResult.builder()
                    .message("Error: " + e.getMessage())
                    .build();
        }
    }
    
    /**
     * Process a batch of records from source index and save to target index
     */
    public ProcessingResult processBatch(ElasticProcessingRequest request) {
        try {
            int batchSize = request.getBatchSize() != null ? request.getBatchSize() : defaultBatchSize;
            
            BoolQuery.Builder boolQueryBuilder = new BoolQuery.Builder();
            
            if (StringUtils.hasText(request.getFilterField()) && StringUtils.hasText(request.getFilterValue())) {
                boolQueryBuilder.must(MatchQuery.of(m -> m
                        .field(request.getFilterField())
                        .query(request.getFilterValue())
                )._toQuery());
            }
            
            if (StringUtils.hasText(request.getTimestampField()) && StringUtils.hasText(request.getLastTimestamp())) {
                boolQueryBuilder.must(RangeQuery.of(r -> r
                        .field(request.getTimestampField())
                        .gt(JsonData.of(request.getLastTimestamp()))
                )._toQuery());
            }
            
            Query query = boolQueryBuilder.build()._toQuery();
            
            SearchRequest.Builder searchBuilder = new SearchRequest.Builder()
                    .index(request.getSourceIndex())
                    .query(query)
                    .size(batchSize);
            
            if (StringUtils.hasText(request.getTimestampField())) {
                searchBuilder.sort(s -> s
                        .field(f -> f
                                .field(request.getTimestampField())
                                .order(SortOrder.Asc)
                        )
                );
            }
            
            SearchResponse<Map> response = elasticsearchClient.search(searchBuilder.build(), Map.class);
            
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> records = response.hits().hits().stream()
                    .map(hit -> (Map<String, Object>) hit.source())
                    .collect(Collectors.toList());
            
            if (records.isEmpty()) {
                return ProcessingResult.builder()
                        .message("No more records to process")
                        .hasMoreRecords(false)
                        .build();
            }
            
            List<Map<String, Object>> processedRecords = new ArrayList<>();
            String lastTimestamp = null;
            
            for (Map<String, Object> record : records) {
                Map<String, Object> processedRecord = processRecord(record, request);
                if (processedRecord != null) {
                    processedRecords.add(processedRecord);
                }
                
                if (StringUtils.hasText(request.getTimestampField()) && record.containsKey(request.getTimestampField())) {
                    lastTimestamp = record.get(request.getTimestampField()).toString();
                }
            }
            
            if (!processedRecords.isEmpty()) {
                List<BulkOperation> bulkOperations = new ArrayList<>();
                
                for (Map<String, Object> processedRecord : processedRecords) {
                    bulkOperations.add(BulkOperation.of(op -> op
                            .index(IndexOperation.of(idx -> idx
                                    .document(processedRecord)
                            ))
                    ));
                }
                
                BulkResponse bulkResponse = elasticsearchClient.bulk(b -> b
                        .index(request.getTargetIndex())
                        .operations(bulkOperations)
                );
                
                if (bulkResponse.errors()) {
                    log.error("Errors during bulk indexing: {}", bulkResponse.items().stream()
                            .filter(item -> item.error() != null)
                            .map(item -> item.error().reason())
                            .collect(Collectors.joining(", ")));
                }
            }
            
            Map<String, Object> verificationRecord = null;
            if (!processedRecords.isEmpty()) {
                try {
                    SearchResponse<Map> verificationResponse = elasticsearchClient.search(s -> s
                            .index(request.getTargetIndex())
                            .size(1), 
                            Map.class);
                    
                    if (!verificationResponse.hits().hits().isEmpty()) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> sourceMap = (Map<String, Object>) verificationResponse.hits().hits().get(0).source();
                        verificationRecord = sourceMap;
                    }
                } catch (Exception e) {
                    log.warn("Could not retrieve verification record", e);
                }
            }
            
            return ProcessingResult.builder()
                    .sampleSourceRecords(records.subList(0, Math.min(5, records.size())))
                    .sampleProcessedRecords(processedRecords.subList(0, Math.min(5, processedRecords.size())))
                    .verificationRecord(verificationRecord)
                    .totalProcessed(records.size())
                    .totalMatched(processedRecords.size())
                    .lastProcessedTimestamp(lastTimestamp)
                    .hasMoreRecords(records.size() >= batchSize)
                    .message("Processed " + records.size() + " records, " + processedRecords.size() + " matched filter criteria")
                    .build();
            
        } catch (IOException e) {
            log.error("Error processing batch", e);
            return ProcessingResult.builder()
                    .message("Error: " + e.getMessage())
                    .build();
        }
    }
    
    /**
     * Process a single record according to the request parameters
     */
    private Map<String, Object> processRecord(Map<String, Object> record, ElasticProcessingRequest request) {
        try {
            if (StringUtils.hasText(request.getFilterField()) && StringUtils.hasText(request.getFilterValue())) {
                Object fieldValue = record.get(request.getFilterField());
                if (fieldValue == null || !fieldValue.toString().contains(request.getFilterValue())) {
                    return null; // Skip record that doesn't match filter
                }
            }
            
            Map<String, Object> processedRecord = new HashMap<>(record);
            
            if (StringUtils.hasText(request.getInputJsonField()) && StringUtils.hasText(request.getMasterJsonPath())) {
                Object inputFieldValue = record.get(request.getInputJsonField());
                
                if (inputFieldValue != null) {
                    String jsonStr = inputFieldValue instanceof String 
                            ? (String) inputFieldValue 
                            : objectMapper.writeValueAsString(inputFieldValue);
                    
                    JsonNode rootNode = objectMapper.readTree(jsonStr);
                    JsonNode masterNode = rootNode.at(normalizePath(request.getMasterJsonPath()));
                    
                    if (!masterNode.isMissingNode()) {
                        if (masterNode.isValueNode()) {
                            processedRecord.put("master", masterNode.asText());
                        } else {
                            processedRecord.put("master", objectMapper.convertValue(masterNode, Map.class));
                        }
                    }
                }
            }
            
            return processedRecord;
        } catch (JsonProcessingException e) {
            log.error("Error processing record JSON", e);
            return null;
        }
    }
    
    /**
     * Build query based on filter criteria
     */
    private Query buildFilterQuery(ElasticProcessingRequest request) {
        BoolQuery.Builder boolQueryBuilder = new BoolQuery.Builder();
        
        if (StringUtils.hasText(request.getFilterField()) && StringUtils.hasText(request.getFilterValue())) {
            boolQueryBuilder.must(MatchQuery.of(m -> m
                    .field(request.getFilterField())
                    .query(request.getFilterValue())
            )._toQuery());
        }
        
        BoolQuery boolQuery = boolQueryBuilder.build();
        return boolQuery._toQuery();
    }
    
    /**
     * Normalize JSON path to ensure it starts with '/'
     */
    private String normalizePath(String path) {
        if (!path.startsWith("/")) {
            return "/" + path;
        }
        return path;
    }
}
