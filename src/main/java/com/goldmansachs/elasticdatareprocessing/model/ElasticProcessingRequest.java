package com.goldmansachs.elasticdatareprocessing.model;

import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request model for Elasticsearch data processing operations.
 * Contains parameters for source and target indices, filtering, and processing
 * options.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ElasticProcessingRequest {

    /**
     * Source Elasticsearch index name to read data from.
     */
    @NotBlank(message = "Source index name is required")
    private String sourceIndex;

    /**
     * Target Elasticsearch index name to write processed data to.
     */
    @NotBlank(message = "Target index name is required")
    private String targetIndex;

    /**
     * Field name to apply filtering on.
     */
    private String filterField;

    /**
     * Value to filter records by, matching against the filterField.
     */
    private String filterValue;

    /**
     * Field containing JSON data to process.
     */
    private String inputJsonField;

    /**
     * JSON path to extract master value from the inputJsonField.
     */
    private String masterJsonPath;

    /**
     * Number of records to process in each batch.
     */
    private Integer batchSize;

    /**
     * Field name containing timestamp for incremental processing.
     */
    private String timestampField;

    /**
     * Last processed timestamp value for continuing batch processing.
     */
    private String lastTimestamp;
}
