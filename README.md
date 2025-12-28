# Jenkins CI/CD Spring Boot Application - PayMyBuddy

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Docker](https://img.shields.io/badge/Docker-Enabled-blue.svg)](https://www.docker.com/)
[![Jenkins](https://img.shields.io/badge/CI%2FCD-Jenkins-red.svg)](https://www.jenkins.io/)

A production-ready Spring Boot application with complete CI/CD pipeline using Jenkins, containerized with Docker for seamless deployment and scalability.

## 📋 Table of Contents

- [Overview](#overview)
- [Architecture](#architecture)
- [Prerequisites](#prerequisites)
- [Docker Deployment](#docker-deployment)
- [CI/CD Pipeline](#cicd-pipeline)
- [Environment Variables](#environment-variables)
- [Quick Start](#quick-start)
- [Advanced Configuration](#advanced-configuration)
- [Troubleshooting](#troubleshooting)

## 🎯 Overview

PayMyBuddy is a Spring Boot application that demonstrates enterprise-grade practices including: 

- **Containerization** with Docker using Amazon Corretto 17
- **Automated CI/CD** pipeline with Jenkins
- **Database integration** with MySQL
- **Production-ready** configuration and deployment strategies

## 🏗️ Architecture

![alt text](<CICD Jenkins.png>)

## 📦 Prerequisites

Before you begin, ensure you have the following installed: 

- **Docker**:  Version 20.10 or higher
- **Docker Compose** (optional): For multi-container setup
- **Java 17**:  For local development
- **Maven**: For building the application
- **Jenkins**: For CI/CD automation
- **MySQL**: Version 8.0 or higher

## 🐳 Docker Deployment

### Dockerfile Overview

The application uses a multi-stage optimized Dockerfile:

```dockerfile
FROM amazoncorretto:17-alpine

ARG JAR_FILE=target/paymybuddy.jar

WORKDIR /app

COPY ${JAR_FILE} paymybuddy.jar

ENV SPRING_DATASOURCE_USERNAME=root
ENV SPRING_DATASOURCE_PASSWORD=password
ENV SPRING_DATASOURCE_URL=jdbc: mysql://172.17.0.1:3306/db_paymybuddy

CMD ["java", "-jar" , "paymybuddy. jar"]
```

**Key Features:**
- ✅ Based on **Amazon Corretto 17 Alpine** (lightweight, production-ready)
- ✅ Configurable JAR file via build arguments
- ✅ Environment-based configuration
- ✅ Optimized for container orchestration

### Building the Docker Image

#### Step 1: Build the Application

```bash
# Build with Maven
mvn clean package -DskipTests

# Or with tests
mvn clean package
```

#### Step 2: Build Docker Image

```bash
# Basic build
docker build -t paymybuddy:latest .

# With custom JAR file
docker build --build-arg JAR_FILE=target/custom-name.jar -t paymybuddy:latest .

# With version tag
docker build -t paymybuddy:1.0.0 -t paymybuddy:latest . 
```

### Running the Container

#### Option 1: Quick Start (Standalone)

```bash
docker run -d \
  --name paymybuddy-app \
  -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://host.docker.internal:3306/db_paymybuddy \
  -e SPRING_DATASOURCE_USERNAME=root \
  -e SPRING_DATASOURCE_PASSWORD=your_password \
  paymybuddy:latest
```

#### Option 2: With Docker Network

```bash
# Create a network
docker network create paymybuddy-network

# Run MySQL
docker run -d \
  --name mysql-db \
  --network paymybuddy-network \
  -e MYSQL_ROOT_PASSWORD=password \
  -e MYSQL_DATABASE=db_paymybuddy \
  -p 3306:3306 \
  mysql:8.0

# Run Application
docker run -d \
  --name paymybuddy-app \
  --network paymybuddy-network \
  -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://mysql-db:3306/db_paymybuddy \
  -e SPRING_DATASOURCE_USERNAME=root \
  -e SPRING_DATASOURCE_PASSWORD=password \
  paymybuddy:latest
```

#### Option 3: Docker Compose (Recommended)

Create a `docker-compose.yml`:

```yaml
version: '3.8'

services:
  mysql:
    image: mysql:8.0
    container_name: paymybuddy-mysql
    environment:
      MYSQL_ROOT_PASSWORD: password
      MYSQL_DATABASE: db_paymybuddy
    ports:
      - "3306:3306"
    volumes: 
      - mysql-data:/var/lib/mysql
    healthcheck:
      test:  ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      interval: 10s
      timeout: 5s
      retries: 5

  app:
    build: .
    container_name: paymybuddy-app
    ports: 
      - "8080:8080"
    environment:
      SPRING_DATASOURCE_URL:  jdbc:mysql://mysql:3306/db_paymybuddy
      SPRING_DATASOURCE_USERNAME: root
      SPRING_DATASOURCE_PASSWORD: password
    depends_on:
      mysql:
        condition: service_healthy

volumes:
  mysql-data: 
```

Run with:

```bash
docker-compose up -d
```

## 🔄 CI/CD Pipeline

### Jenkins Pipeline Configuration

The project uses Jenkins for automated build, test, and deployment. 

#### Jenkinsfile Example

```groovy
pipeline {
    agent any
    
    tools {
        maven 'Maven-3.9'
        jdk 'JDK-17'
    }
    
    stages {
        stage('Checkout') {
            steps {
                git branch: 'main', 
                    url: 'https://github.com/AnselmeG300/jenkins-CICD-spring-boot-app.git'
            }
        }
        
        stage('Build') {
            steps {
                sh 'mvn clean package -DskipTests'
            }
        }
        
        stage('Test') {
            steps {
                sh 'mvn test'
            }
            post {
                always {
                    junit '**/target/surefire-reports/*.xml'
                }
            }
        }
        
        stage('Docker Build') {
            steps {
                sh 'docker build -t paymybuddy:${BUILD_NUMBER} .'
                sh 'docker tag paymybuddy:${BUILD_NUMBER} paymybuddy:latest'
            }
        }
        
        stage('Deploy') {
            steps {
                sh '''
                    docker stop paymybuddy-app || true
                    docker rm paymybuddy-app || true
                    docker run -d \
                      --name paymybuddy-app \
                      -p 8080:8080 \
                      -e SPRING_DATASOURCE_URL=jdbc:mysql://172.17.0.1:3306/db_paymybuddy \
                      -e SPRING_DATASOURCE_USERNAME=root \
                      -e SPRING_DATASOURCE_PASSWORD=password \
                      paymybuddy:latest
                '''
            }
        }
    }
    
    post {
        success {
            echo 'Deployment successful!'
        }
        failure {
            echo 'Deployment failed!'
        }
    }
}
```

### Pipeline Stages

1. **Checkout**: Clone repository from GitHub
2. **Build**:  Compile and package with Maven
3. **Test**: Run unit and integration tests
4. **Docker Build**: Create Docker image
5. **Deploy**:  Deploy container to target environment

## 🔧 Environment Variables

| Variable | Description | Default Value |
|----------|-------------|---------------|
| `SPRING_DATASOURCE_URL` | MySQL database connection URL | `jdbc:mysql://172.17.0.1:3306/db_paymybuddy` |
| `SPRING_DATASOURCE_USERNAME` | Database username | `root` |
| `SPRING_DATASOURCE_PASSWORD` | Database password | `password` |
| `SERVER_PORT` | Application port | `8080` |

### Override Environment Variables

```bash
docker run -d \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://prod-db:3306/paymybuddy \
  -e SPRING_DATASOURCE_USERNAME=app_user \
  -e SPRING_DATASOURCE_PASSWORD=secure_password \
  -e SERVER_PORT=9090 \
  -p 9090:9090 \
  paymybuddy:latest
```

## 🚀 Quick Start

### Local Development

```bash
# Clone repository
git clone https://github.com/AnselmeG300/jenkins-CICD-spring-boot-app. git
cd jenkins-CICD-spring-boot-app

# Build application
mvn clean package

# Run locally
java -jar target/paymybuddy.jar
```

### Docker Deployment (Complete Flow)

```bash
# 1. Build the application
mvn clean package

# 2. Build Docker image
docker build -t paymybuddy: latest .

# 3. Start MySQL database
docker run -d \
  --name mysql-db \
  -e MYSQL_ROOT_PASSWORD=password \
  -e MYSQL_DATABASE=db_paymybuddy \
  -p 3306:3306 \
  mysql:8.0

# 4. Wait for MySQL to be ready (30 seconds)
sleep 30

# 5. Run application
docker run -d \
  --name paymybuddy-app \
  -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://172.17.0.1:3306/db_paymybuddy \
  paymybuddy:latest

# 6. Check application health
curl http://localhost:8080/actuator/health
```

## 🔨 Advanced Configuration

### Production Deployment

For production environments, consider: 

1. **Use Docker Secrets** for sensitive data
2. **Implement health checks**
3. **Use external configuration** (ConfigMaps, Secrets)
4. **Enable monitoring** (Prometheus, Grafana)
5. **Set resource limits**

```bash
docker run -d \
  --name paymybuddy-app \
  -p 8080:8080 \
  --memory="512m" \
  --cpus="1.0" \
  --restart=unless-stopped \
  -e SPRING_PROFILES_ACTIVE=prod \
  paymybuddy:latest
```

### Kubernetes Deployment

```yaml
apiVersion: apps/v1
kind:  Deployment
metadata:
  name: paymybuddy
spec:
  replicas: 3
  selector:
    matchLabels:
      app: paymybuddy
  template: 
    metadata:
      labels: 
        app: paymybuddy
    spec:
      containers:
      - name: paymybuddy
        image: paymybuddy:latest
        ports:
        - containerPort:  8080
        env: 
        - name: SPRING_DATASOURCE_URL
          valueFrom:
            secretKeyRef: 
              name: db-secret
              key: url
```

## 🐛 Troubleshooting

### Common Issues

#### 1. Cannot connect to MySQL

**Problem**: `CommunicationsException: Communications link failure`

**Solution**:
```bash
# Check MySQL is running
docker ps | grep mysql

# Verify network connectivity
docker inspect mysql-db | grep IPAddress

# Update SPRING_DATASOURCE_URL with correct IP
```

#### 2. Port already in use

**Problem**: `Bind for 0.0.0.0:8080 failed: port is already allocated`

**Solution**:
```bash
# Find process using port
lsof -i :8080

# Use different port
docker run -p 8081:8080 paymybuddy:latest
```

#### 3. Container exits immediately

**Problem**: Container stops right after starting

**Solution**:
```bash
# Check logs
docker logs paymybuddy-app

# Run in interactive mode for debugging
docker run -it paymybuddy:latest /bin/sh
```

### Useful Commands

```bash
# View container logs
docker logs -f paymybuddy-app

# Execute commands in running container
docker exec -it paymybuddy-app sh

# Inspect container
docker inspect paymybuddy-app

# View resource usage
docker stats paymybuddy-app

# Remove all containers and images (CAUTION)
docker-compose down -v
docker system prune -a
```

## 📊 Monitoring & Logging

### Application Logs

```bash
# Follow logs in real-time
docker logs -f paymybuddy-app

# Last 100 lines
docker logs --tail 100 paymybuddy-app

# With timestamps
docker logs -t paymybuddy-app
```

### Health Checks

Access Spring Boot Actuator endpoints:

- Health: `http://localhost:8080/actuator/health`
- Info: `http://localhost:8080/actuator/info`
- Metrics: `http://localhost:8080/actuator/metrics`

## 🤝 Contributing

1. Fork the repository
2. Create feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit changes (`git commit -m 'Add AmazingFeature'`)
4. Push to branch (`git push origin feature/AmazingFeature`)
5. Open Pull Request

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 👤 Author

**AnselmeG300**

- GitHub: [@AnselmeG300](https://github.com/AnselmeG300)

## 🙏 Acknowledgments

- Spring Boot team for excellent framework
- Jenkins community for CI/CD tools
- Docker for containerization platform
- Amazon Corretto for JDK distribution

---

**⭐ If you find this project useful, please consider giving it a star! **
