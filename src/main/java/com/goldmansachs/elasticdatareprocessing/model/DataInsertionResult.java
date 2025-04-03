package com.goldmansachs.elasticdatareprocessing.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Result model for Elasticsearch data insertion operations.
 * Contains the insertion status and inserted document information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataInsertionResult {
    
    /**
     * The ID of the inserted document.
     */
    private String documentId;
    
    /**
     * The inserted document data.
     */
    private Map<String, Object> documentData;
    
    /**
     * Whether the insertion was successful.
     */
    private boolean successful;
    
    /**
     * Status or error message related to the insertion operation.
     */
    private String message;
}
