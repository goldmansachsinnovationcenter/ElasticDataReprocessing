package com.goldmansachs.elasticdatareprocessing.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessingResult {
    private List<Map<String, Object>> sampleSourceRecords;
    private List<Map<String, Object>> sampleProcessedRecords;
    private Map<String, Object> verificationRecord;
    private int totalProcessed;
    private int totalMatched;
    private String lastProcessedTimestamp;
    private boolean hasMoreRecords;
    private String message;
}
