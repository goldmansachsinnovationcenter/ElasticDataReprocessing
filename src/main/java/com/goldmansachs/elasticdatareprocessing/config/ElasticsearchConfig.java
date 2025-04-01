package com.goldmansachs.elasticdatareprocessing.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.ssl.SSLContexts;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import javax.net.ssl.SSLContext;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

/**
 * Configuration class for Elasticsearch client.
 * Provides connection settings and client bean for the application.
 */
@Configuration
public final class ElasticsearchConfig {

    /**
     * Elasticsearch host address.
     */
    @Value("${elasticsearch.host}")
    private String host;

    /**
     * Elasticsearch port number.
     */
    @Value("${elasticsearch.port}")
    private int port;

    /**
     * Elasticsearch username for authentication.
     */
    @Value("${elasticsearch.username:}")
    private String username;

    /**
     * Elasticsearch password for authentication.
     */
    @Value("${elasticsearch.password:}")
    private String password;

    /**
     * Whether to use SSL for Elasticsearch connection.
     */
    @Value("${elasticsearch.use-ssl:false}")
    private boolean useSsl;

    /**
     * Connection timeout in milliseconds.
     */
    @Value("${elasticsearch.connection-timeout:5000}")
    private int connectionTimeout;

    /**
     * Socket timeout in milliseconds.
     */
    @Value("${elasticsearch.socket-timeout:60000}")
    private int socketTimeout;

    /**
     * Creates and configures an Elasticsearch client.
     *
     * @return configured Elasticsearch client
     * @throws KeyStoreException if there's an issue with the keystore
     * @throws NoSuchAlgorithmException if a required algorithm is not available
     * @throws KeyManagementException if there's an issue with key management
     */
    @Bean
    public ElasticsearchClient elasticsearchClient() throws KeyStoreException,
            NoSuchAlgorithmException, KeyManagementException {
        final RestClientBuilder builder = RestClient.builder(
                new HttpHost(host, port, useSsl ? "https" : "http"))
                .setRequestConfigCallback(requestConfigBuilder ->
                        requestConfigBuilder
                        .setConnectTimeout(connectionTimeout)
                        .setSocketTimeout(socketTimeout));

        configureAuthentication(builder);

        final RestClient restClient = builder.build();
        final ElasticsearchTransport transport = new RestClientTransport(
                restClient, new JacksonJsonpMapper());
        return new ElasticsearchClient(transport);
    }

    /**
     * Configures authentication and SSL settings for the Elasticsearch client.
     *
     * @param builder the RestClientBuilder to configure
     */
    private void configureAuthentication(final RestClientBuilder builder) {
        if (StringUtils.hasText(username) && StringUtils.hasText(password)) {
            final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(AuthScope.ANY,
                    new UsernamePasswordCredentials(username, password));

            builder.setHttpClientConfigCallback(httpClientBuilder -> {
                httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);

                if (useSsl) {
                    configureSsl(httpClientBuilder);
                }

                return httpClientBuilder;
            });
        } else if (useSsl) {
            builder.setHttpClientConfigCallback(httpClientBuilder -> {
                configureSsl(httpClientBuilder);
                return httpClientBuilder;
            });
        }
    }

    /**
     * Configures SSL context for the HTTP client.
     *
     * @param httpClientBuilder the HTTP client builder to configure
     */
    private void configureSsl(
            final org.apache.http.impl.client.HttpClientBuilder httpClientBuilder) {
        try {
            final SSLContext sslContext = SSLContexts.custom()
                    .loadTrustMaterial(null, (chain, authType) -> true)
                    .build();
            httpClientBuilder.setSSLContext(sslContext);
        } catch (NoSuchAlgorithmException | KeyStoreException
                | KeyManagementException e) {
            throw new RuntimeException("Failed to create SSL context", e);
        }
    }
}
