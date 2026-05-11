# Microservices Distributed Project

This project is a distributed microservices application built with Spring Boot, featuring JWT Authentication, SSE Notifications, Kafka for event streaming, and Redis for caching and rate limiting.

## Project Structure

- `api-gateway`: Spring Cloud Gateway with JWT authentication and rate limiting.
- `task-service`: Manages tasks and produces events to Kafka.
- `notification-service`: Consumes events from Kafka and provides real-time notifications via SSE.
- `frontend`: React/Vite application for the user interface.
- `k8s`: Kubernetes manifests for deployment.

## Tech Stack

- **Java 17** / **Spring Boot 3.2.5**
- **Spring Cloud 2023.0.1**
- **Maven** (Build Tool)
- **Redis** (Rate Limiting & Pub/Sub)
- **Kafka** (Message Broker)
- **MSSQL / H2** (Database)
- **Zipkin** (Distributed Tracing)
- **React** (Frontend)

## Getting Started

### Prerequisites

- Java 17+
- Node.js & npm (for frontend)
- Docker (optional, for running infra locally)
- Maven

### Local Build

To build all Java services:

```bash
mvn clean package -DskipTests
```

To build the frontend:

```bash
cd frontend
npm install
npm run build
```

## Configuration

The services are currently configured to connect to a remote infrastructure host (`100.76.67.76`). 
For local development, you may need to update the `application.yml` files or use Spring profiles.

## Deployment

Refer to [DEPLOY_GUIDE.md](DEPLOY_GUIDE.md) for detailed instructions on deploying to a remote Kubernetes environment.
