# Microservices Distributed: Production Deployment Guide

This guide explains how to build and deploy the updated microservices architecture (JWT Auth, SSE Notifications, Kafka, Redis) to your remote Kubernetes server.

---

## 1. Prerequisites
- **Remote Server**: Fedora host with Minikube/Kubernetes installed.
- **Connectivity**: Tailscale IP `100.76.67.76` must be reachable.
- **Local Dev**: Maven and Docker installed for image building.

---

## 2. Infrastructure Setup (External)
The core infrastructure (MSSQL, Redis, Kafka, Zipkin) is assumed to be running on the server and exposed via Tailscale. Ensure the following proxies are running on the server:
- MSSQL: `30216`
- Redis: `30217`
- Kafka: `30218`
- Zipkin: `30219`

---

## 3. Deployment Steps

### A. Build and Transfer Artifacts (Surgical Method)
This method is the fastest: build everything locally, then transfer **only** the resulting binaries (JARs/dist) and Dockerfiles.

**1. Build locally:**
```bash
# Java Services
mvn clean package -DskipTests

# Frontend
cd frontend && npm install && npm run build && cd ..
```

**2. Transfer MINIMUM files to Remote Server:**
We only copy the `Dockerfile`, the `target` folder (for Java), and the `dist` folder (for Frontend).
```bash
# Create app folder on server
ssh suren@100.76.67.76 "mkdir -p /home/suren/k8s-lab/microservices-app/api-gateway /home/suren/k8s-lab/microservices-app/task-service /home/suren/k8s-lab/microservices-app/notification-service /home/suren/k8s-lab/microservices-app/frontend"

# Copy surgically
scp ./api-gateway/Dockerfile ./api-gateway/target/*.jar suren@100.76.67.76:/home/suren/k8s-lab/microservices-app/api-gateway/
scp ./task-service/Dockerfile ./task-service/target/*.jar suren@100.76.67.76:/home/suren/k8s-lab/microservices-app/task-service/
scp ./notification-service/Dockerfile ./notification-service/target/*.jar suren@100.76.67.76:/home/suren/k8s-lab/microservices-app/notification-service/

# Frontend needs the dist folder and nginx.conf
scp ./frontend/Dockerfile ./frontend/nginx.conf suren@100.76.67.76:/home/suren/k8s-lab/microservices-app/frontend/
scp -r ./frontend/dist suren@100.76.67.76:/home/suren/k8s-lab/microservices-app/frontend/
```

**3. Build and Load on Server:**
SSH into the server to build the images using the transferred artifacts:
```bash
ssh suren@100.76.67.76 "cd /home/suren/k8s-lab/microservices-app && \
  docker build -t local/api-gateway:latest ./api-gateway && \
  docker build -t local/task-service:latest ./task-service && \
  docker build -t local/notification-service:latest ./notification-service && \
  docker build -t local/frontend:latest ./frontend && \
  minikube image load local/api-gateway:latest && \
  minikube image load local/task-service:latest && \
  minikube image load local/notification-service:latest && \
  minikube image load local/frontend:latest"
```

### B. Configure Secrets
Apply the application secrets to your Kubernetes namespace:
```bash
ssh suren@100.76.67.76 "kubectl create secret generic app-secrets --from-literal=mssql-sa-password='P@ssw0rd1234'"
```

### C. Deploy Applications
Apply the unified deployment manifest from your local machine to the remote server:
```bash
# From your local project root:
cat k8s/apps.yaml | ssh suren@100.76.67.76 "kubectl apply -f -"
```

---

## 4. Advanced: Remote Docker Context (Optional)
To avoid manual transfers, you can configure your local Docker to build **directly** on the server:
```bash
docker context create remote-server --docker "host=ssh://suren@100.76.67.76"
docker context use remote-server
# Now builds happen on the server!
docker build -t local/task-service:latest ./task-service
```
### JWT Authentication
- Entry point: `POST /auth/login` (Admin/Password).
- Gateway Filter validates the `Authorization: Bearer <token>` or `?token=<token>` (for SSE).
- Passes `X-Auth-User` header to downstream services.

### Real-Time Notifications (SSE + Redis)
- **Kafka**: Task Service produces events keyed by `ownerId` to ensure ordered user streams.
- **Redis Pub/Sub**: Notification Service instances broadcast events to each other so all connected users receive updates regardless of which pod they are connected to.
- **SSE**: Frontend maintains a persistent stream at `/api/notifications/stream`.

### Database
- **H2 (Local Dev)**: Currently using H2 for stability.
- **MSSQL (Production)**: To switch back, update `task-service/src/main/resources/application.yml` with the connection string in `services-info.md`.

---

## 5. Operational Commands
- **Check Status**: `kubectl get pods -n distributed-services`
- **View Logs**: `kubectl logs -l app=api-gateway -f`
- **Scale Notifications**: `kubectl scale deployment notification-service --replicas=3`
