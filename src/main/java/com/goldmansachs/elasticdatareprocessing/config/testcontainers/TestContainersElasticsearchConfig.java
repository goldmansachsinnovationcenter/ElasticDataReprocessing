package com.goldmansachs.elasticdatareprocessing.config.testcontainers;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.utility.DockerImageName;

import javax.annotation.PreDestroy;

/**
 * Configuration for TestContainers Elasticsearch.
 * This configuration is used for local development and testing.
 */
@Configuration
@Profile({"dev", "test"})
@Slf4j
public class TestContainersElasticsearchConfig {

    private static final String ELASTICSEARCH_IMAGE = "docker.elastic.co/elasticsearch/elasticsearch:7.17.10";
    private ElasticsearchContainer elasticsearchContainer;

    /**
     * Creates and starts a TestContainers Elasticsearch container.
     *
     * @return the Elasticsearch container
     */
    @Bean(destroyMethod = "close")
    public ElasticsearchContainer elasticsearchContainer() {
        elasticsearchContainer = new ElasticsearchContainer(
                DockerImageName.parse(ELASTICSEARCH_IMAGE)
        )
        .withEnv("discovery.type", "single-node")
        .withEnv("xpack.security.enabled", "false")
        .withEnv("ES_JAVA_OPTS", "-Xms512m -Xmx512m");
        
        elasticsearchContainer.start();
        log.info("Started Elasticsearch container on port {}", elasticsearchContainer.getMappedPort(9200));
        
        System.setProperty("elasticsearch.host", elasticsearchContainer.getHost());
        System.setProperty("elasticsearch.port", String.valueOf(elasticsearchContainer.getMappedPort(9200)));
        
        return elasticsearchContainer;
    }

    /**
     * Creates and configures an Elasticsearch client for the TestContainers instance.
     *
     * @param container the Elasticsearch container
     * @return configured Elasticsearch client
     */
    @Bean
    @Primary
    public ElasticsearchClient elasticsearchClient(ElasticsearchContainer container) {
        HttpHost httpHost = new HttpHost(
                container.getHost(),
                container.getMappedPort(9200),
                "http"
        );
        
        RestClient restClient = RestClient.builder(httpHost)
                .setRequestConfigCallback(requestConfigBuilder -> 
                        requestConfigBuilder
                        .setConnectTimeout(5000)
                        .setSocketTimeout(60000))
                .build();
        
        ElasticsearchTransport transport = new RestClientTransport(
                restClient, new JacksonJsonpMapper());
        
        return new ElasticsearchClient(transport);
    }

    /**
     * Stops the Elasticsearch container when the application shuts down.
     */
    @PreDestroy
    public void closeElasticsearch() {
        if (elasticsearchContainer != null && elasticsearchContainer.isRunning()) {
            elasticsearchContainer.close();
            log.info("Stopped Elasticsearch container");
        }
    }
}
