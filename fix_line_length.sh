#!/bin/bash

# Fix line length issues in ElasticsearchService.java
sed -i '91s/final List<Map<String, Object>> records = response.hits().hits().stream()/final List<Map<String, Object>> records = \n                    response.hits().hits().stream()/' src/main/java/com/goldmansachs/elasticdatareprocessing/service/ElasticsearchService.java
sed -i '141s/final List<Map<String, Object>> records = response.hits().hits().stream()/final List<Map<String, Object>> records = \n                    response.hits().hits().stream()/' src/main/java/com/goldmansachs/elasticdatareprocessing/service/ElasticsearchService.java
sed -i '152s/final ProcessingResult result = processAndIndexRecords(records, request);/final ProcessingResult result = \n                processAndIndexRecords(records, request);/' src/main/java/com/goldmansachs/elasticdatareprocessing/service/ElasticsearchService.java
sed -i '180s/final Map<String, Object> processedRecord = processRecord(record, request);/final Map<String, Object> processedRecord = \n                processRecord(record, request);/' src/main/java/com/goldmansachs/elasticdatareprocessing/service/ElasticsearchService.java
sed -i '242s/log.error("Errors during bulk indexing: {}", bulkResponse.items().stream()/log.error("Errors during bulk indexing: {}", \n                bulkResponse.items().stream()/' src/main/java/com/goldmansachs/elasticdatareprocessing/service/ElasticsearchService.java
sed -i '264s/final SearchResponse<Map> verificationResponse = elasticsearchClient.search(s -> s/final SearchResponse<Map> verificationResponse = \n                elasticsearchClient.search(s -> s/' src/main/java/com/goldmansachs/elasticdatareprocessing/service/ElasticsearchService.java
sed -i '272s/(Map<String, Object>) verificationResponse.hits().hits().get(0).source();/(Map<String, Object>) \n                    verificationResponse.hits().hits().get(0).source();/' src/main/java/com/goldmansachs/elasticdatareprocessing/service/ElasticsearchService.java
sed -i '345s/if (fieldValue == null || !fieldValue.toString().contains(request.getFilterValue())) {/if (fieldValue == null \n                    || !fieldValue.toString().contains(request.getFilterValue())) {/' src/main/java/com/goldmansachs/elasticdatareprocessing/service/ElasticsearchService.java
sed -i '377s/final Object inputFieldValue = sourceRecord.get(request.getInputJsonField());/final Object inputFieldValue = \n            sourceRecord.get(request.getInputJsonField());/' src/main/java/com/goldmansachs/elasticdatareprocessing/service/ElasticsearchService.java
sed -i '385s/final JsonNode masterNode = rootNode.at(normalizePath(request.getMasterJsonPath()));/final JsonNode masterNode = \n            rootNode.at(normalizePath(request.getMasterJsonPath()));/' src/main/java/com/goldmansachs/elasticdatareprocessing/service/ElasticsearchService.java
sed -i '391s/processedRecord.put("master", objectMapper.convertValue(masterNode, Map.class));/processedRecord.put("master", \n                    objectMapper.convertValue(masterNode, Map.class));/' src/main/java/com/goldmansachs/elasticdatareprocessing/service/ElasticsearchService.java

# Fix line length issues in ElasticProcessingController.java
sed -i '57s/log.info("Processing batch from index: {} to index: {}",/log.info("Processing batch from index: {} to index: {}",\n                /' src/main/java/com/goldmansachs/elasticdatareprocessing/controller/ElasticProcessingController.java

# Fix line length issues in ElasticsearchConfig.java
sed -i '107s/final SSLContext sslContext = SSLContexts.custom()/final SSLContext sslContext = SSLContexts\n                    .custom()/' src/main/java/com/goldmansachs/elasticdatareprocessing/config/ElasticsearchConfig.java
sed -i '112s/throw new RuntimeException("Failed to create SSL context", e);/throw new RuntimeException(\n                    "Failed to create SSL context", e);/' src/main/java/com/goldmansachs/elasticdatareprocessing/config/ElasticsearchConfig.java
sed -i '134s/final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();/final CredentialsProvider credentialsProvider = \n                    new BasicCredentialsProvider();/' src/main/java/com/goldmansachs/elasticdatareprocessing/config/ElasticsearchConfig.java

chmod +x fix_line_length.sh
./fix_line_length.sh
mvn checkstyle:check
