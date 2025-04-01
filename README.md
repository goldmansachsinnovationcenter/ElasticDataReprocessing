# Elastic Data Reprocessing

A Spring Boot application for reprocessing data from Elasticsearch clusters.

## Features

- Connect to Elasticsearch clusters using configurable settings
- Process data from source Elasticsearch indices
- Apply filtering conditions to select specific records
- Transform data by extracting values from JSON fields
- Store processed data in target Elasticsearch indices
- Batch processing capability for handling large datasets

## Requirements

- Java 17
- Maven 3.6+
- Elasticsearch 7.x or 8.x

## Configuration

The application uses YAML configuration for Elasticsearch cluster settings:

```yaml
elasticsearch:
  host: localhost
  port: 9200
  username: 
  password: 
  use-ssl: false
  connection-timeout: 5000
  socket-timeout: 60000
  batch-size: 100
```

## Running the Application

```bash
mvn spring-boot:run
```

The application will be available at http://localhost:8080

## Testing

```bash
mvn test
```
