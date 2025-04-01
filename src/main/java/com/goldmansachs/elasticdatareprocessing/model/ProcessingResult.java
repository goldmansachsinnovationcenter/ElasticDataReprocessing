package com.goldmansachs.elasticdatareprocessing.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Result model for Elasticsearch data processing operations.
 * Contains processed records, statistics, and status information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessingResult {
    /**
     * Sample records from the source Elasticsearch index.
     */
    private List<Map<String, Object>> sampleSourceRecords;

    /**
     * Sample processed records after applying transformations.
     */
    private List<Map<String, Object>> sampleProcessedRecords;

    /**
     * A verification record retrieved from the target index after processing.
     */
    private Map<String, Object> verificationRecord;

    /**
     * Total number of records processed in the current batch.
     */
    private int totalProcessed;

    /**
     * Total number of records that matched the filter criteria.
     */
    private int totalMatched;

    /**
     * Timestamp of the last processed record for incremental processing.
     */
    private String lastProcessedTimestamp;

    /**
     * Flag indicating whether there are more records to process.
     */
    private boolean hasMoreRecords;

    /**
     * Status or error message related to the processing operation.
     */
    private String message;
}
