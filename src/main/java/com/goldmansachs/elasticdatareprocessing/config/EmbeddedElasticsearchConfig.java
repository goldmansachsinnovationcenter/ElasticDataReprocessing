package com.goldmansachs.elasticdatareprocessing.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import pl.allegro.tech.embeddedelasticsearch.EmbeddedElastic;
import pl.allegro.tech.embeddedelasticsearch.PopularProperties;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Configuration for embedded Elasticsearch server.
 * This configuration is used for local development and testing.
 */
@Configuration
@Profile("!prod")
public class EmbeddedElasticsearchConfig {

    private EmbeddedElastic embeddedElastic;

    /**
     * Creates and starts an embedded Elasticsearch server.
     *
     * @return the embedded Elasticsearch instance
     * @throws IOException if there's an issue starting the server
     */
    @Bean(destroyMethod = "stop")
    @Primary
    public EmbeddedElastic embeddedElastic() throws IOException {
        embeddedElastic = EmbeddedElastic.builder()
                .withElasticVersion("7.10.2")
                .withSetting(PopularProperties.HTTP_PORT, 9200)
                .withSetting(PopularProperties.CLUSTER_NAME, "embedded-elasticsearch")
                .withStartTimeout(2, TimeUnit.MINUTES)
                .build()
                .start();
        
        return embeddedElastic;
    }

    /**
     * Stops the embedded Elasticsearch server when the application shuts down.
     */
    @PreDestroy
    public void closeElasticsearch() {
        if (embeddedElastic != null) {
            embeddedElastic.stop();
        }
    }
}
