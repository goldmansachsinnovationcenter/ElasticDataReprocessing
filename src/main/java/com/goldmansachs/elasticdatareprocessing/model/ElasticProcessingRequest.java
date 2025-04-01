package com.goldmansachs.elasticdatareprocessing.model;

import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ElasticProcessingRequest {

    @NotBlank(message = "Source index name is required")
    private String sourceIndex;

    @NotBlank(message = "Target index name is required")
    private String targetIndex;

    private String filterField;
    
    private String filterValue;
    
    private String inputJsonField;
    
    private String masterJsonPath;
    
    private Integer batchSize;
    
    private String timestampField;
    
    private String lastTimestamp;
}
