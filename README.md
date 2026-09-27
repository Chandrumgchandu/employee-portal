# Employee Portal — Secure CI/CD Pipeline

A Spring Boot application used to demonstrate a multi-stage DevSecOps delivery pipeline from source validation through artifact publishing, container security scanning, registry publishing, and Kubernetes deployment.

## Pipeline

```text
Git checkout
  -> Maven compile
  -> Unit tests
  -> SonarQube analysis
  -> Quality Gate
  -> Maven package
  -> Nexus artifact publish
  -> Docker build
  -> Trivy image scan
  -> Amazon ECR
  -> Kubernetes rollout
  -> Deployment verification
```

## Stack

- Java 21 / Spring Boot
- Maven
- Jenkins
- SonarQube
- Nexus Repository
- Docker
- Trivy
- AWS ECR
- Kubernetes

## Repository structure

```text
.
├── Dockerfile
├── Jenkinsfile
├── pom.xml
├── mvnw / mvnw.cmd
└── src/
```

## CI/CD controls

The Jenkins pipeline compiles and tests the application, runs static analysis through SonarQube, blocks on the quality gate, publishes Maven artifacts to Nexus, builds a versioned Docker image, scans the image for HIGH and CRITICAL vulnerabilities with Trivy, authenticates to ECR, pushes build-specific and latest tags, updates the Kubernetes deployment, waits for rollout completion, and verifies deployment, pod, and service state.

Credentials are referenced through Jenkins credential bindings rather than embedded passwords.

## Local build

```bash
./mvnw clean test
./mvnw clean package
docker build -t employee-portal:local .
```

## Environment notes

The checked-in pipeline contains lab-specific service endpoints and AWS/Kubernetes configuration. Treat those as environment configuration when adapting this repository: externalize endpoints, account identifiers, credentials, namespaces, and deployment parameters for reusable environments.

## Engineering purpose

This project demonstrates a gated software-delivery path that combines code quality, artifact management, container security, cloud registry integration, and Kubernetes rollout verification.
