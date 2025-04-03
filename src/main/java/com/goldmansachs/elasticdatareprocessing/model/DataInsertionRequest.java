package com.goldmansachs.elasticdatareprocessing.model;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Request model for inserting data into Elasticsearch indices.
 * Contains the target index name and the data to be inserted.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataInsertionRequest {

    /**
     * Target Elasticsearch index name to insert data into.
     */
    @NotBlank(message = "Index name is required")
    private String indexName;

    /**
     * ID for the document (optional).
     * If not provided, Elasticsearch will generate one.
     */
    private String documentId;

    /**
     * Data to be inserted as a document.
     */
    @NotNull(message = "Document data is required")
    private Map<String, Object> documentData;
}
