# Employee Portal Operations Runbook

## Deployment verification

```bash
kubectl get deployment employee-portal -n employee-portal
kubectl rollout status deployment/employee-portal -n employee-portal --timeout=180s
kubectl get pods -n employee-portal -o wide
kubectl get svc -n employee-portal
```

## Common checks

### Pods are not ready

```bash
kubectl describe pod -n employee-portal -l app=employee-portal
kubectl logs -n employee-portal -l app=employee-portal --tail=100
```

Check image pull errors, failed probes, insufficient resources, and application startup failures.

### Image pull fails

```bash
kubectl get secret ecr-secret -n employee-portal
kubectl describe pod -n employee-portal -l app=employee-portal
```

The Jenkins pipeline refreshes `ecr-secret` before deployment. Confirm AWS credentials, account ID, region, repository name, and image tag.

### Rollout stalls

```bash
kubectl rollout status deployment/employee-portal -n employee-portal --timeout=180s
kubectl rollout history deployment/employee-portal -n employee-portal
kubectl describe deployment employee-portal -n employee-portal
```

Review events for probe failures, image pull failures, and resource scheduling issues.

## Rollback

```bash
kubectl rollout undo deployment/employee-portal -n employee-portal
kubectl rollout status deployment/employee-portal -n employee-portal --timeout=180s
```

## Pipeline controls

The Jenkins pipeline includes compile, test, SonarQube analysis, quality gate enforcement, Maven artifact publishing, Docker build, Trivy scan, ECR push, Kubernetes deployment, rollout verification, and workspace cleanup.

## Configuration hygiene

- Keep AWS credentials in Jenkins credential bindings.
- Keep AWS account ID in a Jenkins string credential named `aws-account-id`.
- Do not commit generated credentials, kubeconfigs, registry passwords, or real environment secrets.
- Use immutable build tags for deployment; treat `latest` only as a convenience tag.
