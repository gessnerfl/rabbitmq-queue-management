# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview
RabbitMQ Queue Management is a Spring Boot application that provides a web interface to manage RabbitMQ queues and messages. It allows listing, deleting, moving, and re-queueing messages from RabbitMQ queues.

## Build Commands
- **Build project**: `./gradlew build`
- **Run all tests**: `./gradlew test`
- **Run a single test class**: `./gradlew test --tests ClassName`
- **Run a single test method**: `./gradlew test --tests ClassName.methodName`
- **Run application**: `./gradlew bootRun`
- **Build Docker image**: `./gradlew jib` (requires Docker Hub credentials in `gradle.properties`)
- **Generate test coverage**: `./gradlew jacocoTestReport`

## Development Setup
- **Start RabbitMQ for development**: `docker-compose up rabbitmq`
- **Application runs on**: http://localhost:8780
- **Management API on**: http://localhost:8781
- **RabbitMQ Management UI**: http://localhost:15672 (guest/guest)

## Architecture Overview

### Core Components
- **RabbitMqFacade** (`service.rabbitmq.RabbitMqFacade`): Main service interface combining management API and message operations
- **ManagementApi** (`service.rabbitmq.remoteapi.ManagementApi`): HTTP API client for RabbitMQ Management API using Feign
- **Operations** (`service.rabbitmq.operations.Operations`): AMQP operations for message manipulation

### Package Structure
- **controller**: Web controllers for REST endpoints and UI
- **service.rabbitmq**: Core RabbitMQ integration
  - **operations**: Message operations (delete, move, requeue)
  - **remoteapi**: HTTP API integration with RabbitMQ Management
  - **config**: RabbitMQ connection configuration
  - **utils**: Message utilities (checksum, mapping)
- **connection**: AMQP connection management
- **model**: Data models for queues, messages, and API responses
- **javaconfig**: Spring configuration

### Key Operations
- **Delete**: Confirms and removes messages from queues
- **Move**: Publishes messages to different exchange/routing key with publisher acknowledgments
- **Requeue**: Republishes dead-lettered messages using x-death header information
- **Message safety**: Uses checksums to prevent unintended operations

### Configuration
- Main config: `src/main/resources/application.yaml`
- RabbitMQ connection settings under `de.gessnerfl.rabbitmq`
- Default ports: 8780 (web), 8781 (management), 5672 (AMQP), 15672 (RabbitMQ Management)

### Testing
- Unit tests: `*Test.java`; Integration tests: `*IntegrationTest.java` (use Testcontainers with a RabbitMQ container)
- Integration test base classes: `AbstractIntegrationTest` (Spring Boot test with random port) and `AbstractIntegrationTestWithRabbitMqContainer` (adds a live RabbitMQ container via Testcontainers)
- Test environment helpers: `RabbitMqTestEnvironment` / `RabbitMqTestEnvironmentBuilder` for programmatically creating queues and exchanges in tests
- Integration tests activate the `integrationtest` Spring profile (`src/test/resources/application-integrationtest.yaml`)
- `TestDataCreator` (in `src/test/java/.../util/`) is a standalone main class to seed a running local RabbitMQ instance with test data

### Message Operation Data Flow
All three operations (Delete, Move, Requeue) follow the same pattern:
1. Fetch message from queue via AMQP
2. Verify checksum (to prevent operating on wrong message)
3. Perform operation; on error, nack the message so it stays in the queue

Move and Requeue use publisher acknowledgements with return listeners to ensure delivery. Requeue reads the `x-death` header to determine the original exchange and routing key. Move and Requeue operations append custom headers (`x-rmqmgmt-move-count`, `x-rmqmgmt-requeue-count`) to track how many times a message has been processed.

### Technology Notes
- Java 21 (declared via Gradle toolchain)
- ManagementApi HTTP client uses OpenFeign; JSON handled by Gson with a custom date format
- UI uses Thymeleaf templates with Bootstrap 5 via WebJars