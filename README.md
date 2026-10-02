# Employee Portal — DevSecOps Delivery Pipeline

A Spring Boot application used to demonstrate a multi-stage DevSecOps delivery path from source validation through artifact publishing, container security scanning, registry publishing, Kubernetes rollout, and operational verification.

## What this repo proves

- Java 21 / Spring Boot application with Maven wrapper.
- Jenkins pipeline for compile, test, SonarQube analysis, quality gate, package, Nexus deploy, Docker build, Trivy scan, ECR push, and Kubernetes rollout.
- Kubernetes manifests with namespace, deployment, service, probes, resource requests/limits, and image pull secret reference.
- Operations runbook for rollout verification, rollback, image-pull troubleshooting, and pod diagnostics.
- Public hygiene: cloud account values and credentials are expected from Jenkins credentials, not committed as literal secrets.

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
├── docs/runbook.md
├── k8s/
│   ├── namespace.yaml
│   ├── deployment.yaml
│   └── service.yaml
├── pom.xml
├── mvnw / mvnw.cmd
└── src/
```

## Local build

```bash
./mvnw clean test
./mvnw clean package
docker build -t employee-portal:local .
```

## Kubernetes manifests

```bash
kubectl apply -f k8s/namespace.yaml
kubectl apply -f k8s/deployment.yaml
kubectl apply -f k8s/service.yaml
kubectl rollout status deployment/employee-portal -n employee-portal --timeout=180s
```

Before applying the deployment manifest, replace `REPLACE_WITH_ECR_URI:REPLACE_WITH_BUILD_TAG` with an image tag created by the pipeline or through a local test registry.

## Jenkins credential expectations

| Credential ID | Purpose |
|---|---|
| `nexus-creds` | Nexus username/password for Maven artifact publishing. |
| `aws-creds` | AWS credential binding used for ECR login and Kubernetes image-pull secret refresh. |
| `aws-account-id` | Jenkins string credential for the AWS account ID used to build the ECR URI. |

The pipeline still contains lab-specific tool names such as `JDK21`, `Maven`, `SonarScanner`, and `SonarQube`; configure matching names in Jenkins or update the Jenkinsfile for your controller.

## Operations

See [`docs/runbook.md`](docs/runbook.md) for verification commands, rollback, image pull troubleshooting, and rollout triage.

## Engineering purpose

This project is the strongest public DevSecOps evidence in the portfolio. It shows a gated delivery flow, container and image scanning controls, registry publishing, Kubernetes rollout verification, and a runbook-oriented operations mindset.
