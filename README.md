# Complete AWS CI/CD Setup for Spring Boot + Docker + ECR + ECS + CodeBuild + CodePipeline 🚀

## Project Overview

This project demonstrates complete CI/CD pipeline automation using:

- Spring Boot
- Docker
- GitHub
- Amazon ECR
- Amazon ECS
- AWS CodeBuild
- AWS CodePipeline

![img.png](images/img.png)
---

# CI/CD Flow

```text
GitHub Push
      ↓
AWS CodePipeline Trigger
      ↓
AWS CodeBuild Build Process
      ↓
Docker Image Build
      ↓
Push Docker Image to ECR
      ↓
Deploy to ECS
```

---

# Project Structure

```text
course-service/
│
├── src/
├── pom.xml
├── Dockerfile
├── buildspec.yml
├── .dockerignore
```

---

# STEP 1 — Create Spring Boot Application

Verify application works locally:

```bash
mvn clean package
```

Run application:

```bash
java -jar target/course-service.jar
```

Test:

```text
http://localhost:8085
```

---

# STEP 2 — Create Dockerfile

Create file:

```text
Dockerfile
```

Content:

```dockerfile
FROM openjdk:17

LABEL version="1.0"
LABEL maintainer="prashant"
LABEL description="Course Service"

WORKDIR /app
ADD ./target/course-service.jar course-service.jar
EXPOSE 8085
ENTRYPOINT ["java", "-jar", "course-service.jar"]
CMD ["--server.port=8085"]
```

---

# STEP 3 — Create .dockerignore

Create file:

```text
.dockerignore
```

Content:

```text
.git
.idea
target/*.original
README.md
```

---

# STEP 4 — Test Docker Locally

Build Docker image:

```bash
docker build -t course-service .
```

Verify image:

```bash
docker images
```

Run container:

```bash
docker run -p 8085:8085 course-service
```

Access application:

```text
http://localhost:8085
```

---

# STEP 5 — Push Code to GitHub

```bash
git init
git add .
git commit -m "Initial Commit"
git branch -M main
git remote add origin <github_repo_url>
git push -u origin main
```

---

# STEP 6 — Create ECR Repository

AWS Console:

```text
Amazon ECR → Create Repository
```

Repository Name:

```text
course-service
```

Example URI:

```text
123456789012.dkr.ecr.ap-south-1.amazonaws.com/course-service
```
![img_1.png](images/img_1.png)

![img_2.png](images/img_2.png)
---

# STEP 7 — Create buildspec.yml

Create file:

```text
buildspec.yml
```

Content:

```yaml
version: 0.2

phases:

  pre_build:
    commands:
      - echo Logging in to Amazon ECR...
      - aws --version

      - ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)

      - REGION=ap-south-1

      - REPOSITORY_URI=$ACCOUNT_ID.dkr.ecr.$REGION.amazonaws.com/course-service

      - IMAGE_TAG=latest

      - aws ecr get-login-password --region $REGION | docker login --username AWS --password-stdin $REPOSITORY_URI

  build:
    commands:
      - echo Build started on `date`

      - mvn clean package -DskipTests

      - docker build -t course-service .

      - docker tag course-service:latest $REPOSITORY_URI:$IMAGE_TAG

  post_build:
    commands:
      - echo Build completed on `date`

      - docker push $REPOSITORY_URI:$IMAGE_TAG

      - printf '[{"name":"course-service","imageUri":"%s"}]' $REPOSITORY_URI:$IMAGE_TAG > imagedefinitions.json

artifacts:
  files:
    - imagedefinitions.json
```

---

# STEP 8 — Create CodeBuild Project

AWS Console:

```text
AWS CodeBuild → Create Build Project
```

Source:

```text
GitHub
```
Buildspec:

```text
Use buildspec.yml from source code
```

---

# STEP 9 — Add IAM Permissions to CodeBuild Role

Add these policies:

```text
AmazonEC2ContainerRegistryPowerUser
AmazonECS_FullAccess
AmazonS3FullAccess
CloudWatchLogsFullAccess
```
![img_7.png](images/img_7.png)
---
![img_5.png](images/img_5.png)
---
![img_4.png](images/img_4.png)
---
![img_3.png](images/img_3.png)
---

![img_8.png](images/img_8.png)

---
# STEP 11 - Run the code build
![img_21.png](images/img_21.png)

# STEP 12 — Create Task Definition

![img_16.png](images/img_16.png)
![img_15.png](images/img_15.png)
AWS Console:

```text
ECS → Task Definitions → Create
```

Settings:

```text
Task Definition Name : course-task
Launch Type          : Fargate
Container Name       : course-service
Container Image      : 263856761644.dkr.ecr.ap-south-1.amazonaws.com/course-service:latest or directly repo uri it will pick the latest
Port                 : 8085
CPU                  : 256
Memory               : 512
```

Image URI:63856761644.dkr.ecr.ap-south-1.amazonaws.com/course-service:latest

```text
the same which got crewated
<ECR_REPOSITORY_URI>
```
![img_11.png](images/img_11.png)
![img_17.png](images/img_17.png)

---

# STEP 13 — Create ECS Cluster/Service

![img_18.png](images/img_18.png)
![img_19.png](images/img_19.png)

Inside ECS Cluster:

```text
Create Service
or 
Run new Task either way we can do !!!
```

Settings:

```text
Launch Type : Fargate
Desired Task: 1
```

Networking:

- Public subnet
- Auto assign public IP = ENABLED

Security Group Inbound Rule:

```text
Custom TCP : 8085
Source     : 0.0.0.0/0
```
![img_20.png](images/img_20.png)
![img_22.png](images/img_22.png)

### Test
![img_23.png](images/img_23.png)
---


# STEP 14 — Create CodePipeline

AWS Console:

```text
AWS CodePipeline → Create Pipeline
```

Stages:

## Source Stage

```text
Provider: GitHub
```

## Build Stage

```text
Provider: AWS CodeBuild
```

## Deploy Stage

```text
Provider: Amazon ECS
```

Select:

```text
Cluster : course-cluster
Service : ECS Service Name
```

IMPORTANT:

Container name MUST match:

```text
course-service
```
![img.png](images/img24.png)
---

# STEP 15 — Trigger Pipeline

Push changes:

```bash
git add .
git commit -m "new changes"
git push
```

Pipeline automatically:

- Pulls source code
- Builds Maven project
- Builds Docker image
- Pushes image to ECR
- Deploys latest image to ECS

---

# STEP 16 — Access Application

Go to:

```text
ECS → Tasks
```

Copy Public IP.

Open:

```text
http://<public-ip>:8085
```

---

# Common Issues

## Docker Build Fails

Cause:

```text
Privileged Mode disabled
```

Fix:

Enable privileged mode in CodeBuild.

---

## ECS Deployment Fails

Cause:

```text
Wrong container name
```

Fix:

Container name must match imagedefinitions.json.

---

## Application Not Accessible

Cause:

- Security group missing
- Wrong port
- Public IP disabled

---

## ECR Push Fails

Cause:

```text
Missing IAM permissions
```

Fix:

Add ECR permissions to CodeBuild role.

---

# Complete Flow
| Step | Action                         | Description                                               |
| ---- | ------------------------------ | --------------------------------------------------------- |
| 1    | Spring Boot App                | Create and test Spring Boot application locally           |
| 2    | Create Dockerfile              | Dockerize application                                     |
| 3    | Create `.dockerignore`         | Ignore unnecessary files                                  |
| 4    | Local Docker Test              | Build and run container locally                           |
| 5    | Push to GitHub                 | Push source code to GitHub repository                     |
| 6    | Create ECR Repository          | Create Docker image repository in Amazon Web Services ECR |
| 7    | Create `buildspec.yml`         | Define CodeBuild phases and Docker push process           |
| 8    | Create CodeBuild Project       | Configure AWS CodeBuild                                   |
| 9    | Attach IAM Policies            | Add ECR/ECS/S3/CloudWatch permissions                     |
| 10   | Create ECS Cluster             | Create Fargate ECS cluster                                |
| 11   | Run CodeBuild                  | Build Maven project + Docker image + Push to ECR          |
| 12   | Create Task Definition         | Define container settings for ECS                         |
| 13   | Run Task OR Create ECS Service | Deploy application using Fargate                          |
| 14   | Create CodePipeline            | Automate CI/CD pipeline                                   |
| 15   | Trigger Pipeline               | Push code changes to auto deploy                          |
| 16   | Access Application             | Open app using ECS public IP                              |

---
### Deployment Flow
```
GitHub Push
↓
CodePipeline Trigger
↓
CodeBuild
↓
Maven Build
↓
Docker Build
↓
Push Image to ECR
↓
ECS Cluster
↓
Task Definition
↓
ECS Service / Task
↓
Application Running
```