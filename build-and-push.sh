#!/bin/bash

# Your ECR registry
ECR_REGISTRY="420465597326.dkr.ecr.us-east-1.amazonaws.com"
AWS_REGION="us-east-1"

echo "========================================="
echo "   Banking System Build & Push to ECR"
echo "========================================="

# Step 1 - Login to ECR
echo "Logging into ECR..."
aws ecr get-login-password --region $AWS_REGION | \
  docker login --username AWS \
  --password-stdin $ECR_REGISTRY

# Step 2 - Build all jars
echo "Building all service jars..."
services=(
  "auth-service"
  "account-service"
  "transaction-service"
  "notification-service"
  "audit-service"
  "fraud-service"
  "approval-service"
  "gateway"
)

for service in "${services[@]}"; do
  echo "Building jar for $service..."
  cd $service
  mvn clean package -DskipTests -q
  if [ $? -ne 0 ]; then
    echo "❌ Failed to build $service"
    exit 1
  fi
  echo "✅ $service jar built"
  cd ..
done

# Step 3 - Build and push Docker images
echo "Building and pushing Docker images..."
for service in "${services[@]}"; do
  echo "Building Docker image for $service..."
  docker build -t $ECR_REGISTRY/banking/$service:latest ./$service
  if [ $? -ne 0 ]; then
    echo " Failed to build Docker image for $service"
    exit 1
  fi

  echo "Pushing $service to ECR..."
  docker push $ECR_REGISTRY/banking/$service:latest
  if [ $? -ne 0 ]; then
    echo "Failed to push $service to ECR"
    exit 1
  fi
  echo "$service pushed to ECR"
done

echo "========================================="
echo " All images pushed to ECR successfully!"
echo "========================================="
echo ""
echo "Now SSH into EC2 and run:"
echo "  cd banking-system"
echo "  docker-compose pull"
echo "  docker-compose up -d"