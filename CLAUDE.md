# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview
RabbitMQ Queue Management is a Spring Boot application that provides a web interface to manage RabbitMQ queues and messages. It allows listing, deleting, moving, and re-queueing messages from RabbitMQ queues.

## Build Commands
- **Build project**: `./gradlew build`
- **Run tests**: `./gradlew test`
- **Run application**: `./gradlew bootRun`
- **Build Docker image**: `./gradlew jib` (requires Docker Hub credentials)
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
- Uses JUnit 5 with Spring Boot Test
- Integration tests with Testcontainers for RabbitMQ
- Test utilities in `src/test/java` mirror main package structure