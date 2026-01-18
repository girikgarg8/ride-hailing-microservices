# 🚀 UBER MICROSERVICES PLATFORM - AWS Deployment Guide

**Production-Ready Architecture | AWS Deployment**

---

## 💪 Your Achievement

While 99.99% of engineers only talk about microservices, **you're building and deploying one**. This production-grade distributed system with 7 microservices, API Gateway, Kafka, and AWS deployment will set you apart in interviews.

**What makes this exceptional:**
- ✨ **7 microservices** orchestrated together (Gateway, Auth, Booking, Location, Socket, Review, Eureka)
- ✨ **API Gateway** with JWT authentication (delegates to Auth Service)
- ✨ **Service Discovery** with Eureka (dynamic service resolution)
- ✨ **Event-driven architecture** with Kafka (async communication)
- ✨ **Real-time WebSocket** communication (driver notifications)
- ✨ **Redis Geospatial** queries (nearby driver search)
- ✨ **AWS deployment** with proper security architecture

**You're not just coding - you're architecting. Keep going!** 🚀

---

## 🏗️ Architecture

### **Microservices:**
1. **Uber-API-Gateway** - Port 9001 (Public) - Routing + JWT Auth (delegates to Auth Service)
2. **Uber-Service-Discovery** - Port 8761 (Public) - Eureka
3. **Uber-Auth-Service** - Port 9090 (Private) - JWT Creation & Validation
4. **Uber-Booking-Service** - Port 7475 (Private) - Booking management + Kafka consumer
5. **Uber-Location-Service** - Port 7477 (Private) - Redis geospatial
6. **Uber-Socket-Service** - Port 8080 (Private) - WebSocket + Kafka producer
7. **Uber-Review-Service** - Port 7272 (Private) - Reviews (CRUD)
8. **Uber-Entity-Service** - (Migration Utility) - Shared entities + Flyway migrations (not a web service)
9. **Uber-Driver-WebSocket-Client** - Port 3000 (Public) - Driver interface for ride requests

### **Infrastructure:**
- **VPC**: Public subnet (Gateway, Eureka, Client) + Private subnet (Backend services)
- **RDS MySQL**: Booking, Review, Auth data
- **ElastiCache Redis**: Geospatial driver locations
- **Amazon MSK / Self-hosted Kafka**: Event streaming
- **ALB**: Routes traffic to Gateway only
- **NAT Gateway**: Private subnet outbound access
- **CloudWatch**: Logs and monitoring

### **Architecture Diagram:**
```
Internet → ALB → API Gateway (Public) → Eureka (Public) → Client (Public)
                        ↓
            ┌───────────┴───────────┐
            │   Private Subnet      │
            ├───────────────────────┤
            │ Auth Service          │
            │ Booking Service       │
            │ Location Service      │
            │ Socket Service        │
            │ Review Service        │
            └───────────────────────┘
                   ↓   ↓   ↓
            MySQL  Redis  Kafka
```

**Security:** Backend services in private subnet, accessible only through Gateway.

---

## ✅ Prerequisites

### **AWS Account Setup:**
```bash
# Install AWS CLI
brew install awscli  # macOS

# Configure credentials
aws configure
# Enter: Access Key, Secret Key, Region (ap-south-1), Output (json)

# Verify
aws sts get-caller-identity
```

### **Local Requirements:**
- Java 17
- Gradle
- MySQL client
- Redis CLI
- SSH client

---

## 🛠️ AWS Infrastructure Setup (CLI Commands)


### **📋 Manual Step-by-Step Setup**

If you prefer to run commands step by step:

### **Phase 1: VPC & Networking (10 mins)**

#### **Step 1: Create VPC and Core Components**
```bash
# Set variables for consistent naming
export PROJECT_NAME="uber-platform"
export REGION="ap-south-1"  # Asia Pacific (Mumbai)
export AZ1="${REGION}a"
export AZ2="${REGION}b"

# Create VPC
VPC_ID=$(aws ec2 create-vpc \
  --cidr-block 10.0.0.0/16 \
  --tag-specifications "ResourceType=vpc,Tags=[{Key=Name,Value=${PROJECT_NAME}-vpc}]" \
  --query 'Vpc.VpcId' --output text)

# Enable DNS hostnames and resolution
aws ec2 modify-vpc-attribute --vpc-id $VPC_ID --enable-dns-hostnames
aws ec2 modify-vpc-attribute --vpc-id $VPC_ID --enable-dns-support

# Create Internet Gateway
IGW_ID=$(aws ec2 create-internet-gateway \
  --tag-specifications "ResourceType=internet-gateway,Tags=[{Key=Name,Value=${PROJECT_NAME}-igw}]" \
  --query 'InternetGateway.InternetGatewayId' --output text)

# Attach Internet Gateway to VPC
aws ec2 attach-internet-gateway --vpc-id $VPC_ID --internet-gateway-id $IGW_ID
```

#### **Step 2: Create Subnets**
```bash
# Create Public Subnet 1 (AZ-a)
PUBLIC_SUBNET_1=$(aws ec2 create-subnet \
  --vpc-id $VPC_ID \
  --cidr-block 10.0.1.0/24 \
  --availability-zone $AZ1 \
  --tag-specifications "ResourceType=subnet,Tags=[{Key=Name,Value=${PROJECT_NAME}-subnet-public1-${AZ1}}]" \
  --query 'Subnet.SubnetId' --output text)

# Create Public Subnet 2 (AZ-b)
PUBLIC_SUBNET_2=$(aws ec2 create-subnet \
  --vpc-id $VPC_ID \
  --cidr-block 10.0.2.0/24 \
  --availability-zone $AZ2 \
  --tag-specifications "ResourceType=subnet,Tags=[{Key=Name,Value=${PROJECT_NAME}-subnet-public2-${AZ2}}]" \
  --query 'Subnet.SubnetId' --output text)

# Create Private Subnet 1 (AZ-a)
PRIVATE_SUBNET_1=$(aws ec2 create-subnet \
  --vpc-id $VPC_ID \
  --cidr-block 10.0.10.0/24 \
  --availability-zone $AZ1 \
  --tag-specifications "ResourceType=subnet,Tags=[{Key=Name,Value=${PROJECT_NAME}-subnet-private1-${AZ1}}]" \
  --query 'Subnet.SubnetId' --output text)

# Create Private Subnet 2 (AZ-b)
PRIVATE_SUBNET_2=$(aws ec2 create-subnet \
  --vpc-id $VPC_ID \
  --cidr-block 10.0.11.0/24 \
  --availability-zone $AZ2 \
  --tag-specifications "ResourceType=subnet,Tags=[{Key=Name,Value=${PROJECT_NAME}-subnet-private2-${AZ2}}]" \
  --query 'Subnet.SubnetId' --output text)

# Enable auto-assign public IP for public subnets
aws ec2 modify-subnet-attribute --subnet-id $PUBLIC_SUBNET_1 --map-public-ip-on-launch
aws ec2 modify-subnet-attribute --subnet-id $PUBLIC_SUBNET_2 --map-public-ip-on-launch
```

#### **Step 3: Create NAT Gateway**
```bash
# Allocate Elastic IP for NAT Gateway
NAT_EIP=$(aws ec2 allocate-address \
  --domain vpc \
  --tag-specifications "ResourceType=elastic-ip,Tags=[{Key=Name,Value=${PROJECT_NAME}-nat-eip}]" \
  --query 'AllocationId' --output text)

# Create NAT Gateway in Public Subnet 1
NAT_GW_ID=$(aws ec2 create-nat-gateway \
  --subnet-id $PUBLIC_SUBNET_1 \
  --allocation-id $NAT_EIP \
  --query 'NatGateway.NatGatewayId' --output text)

# Tag NAT Gateway after creation (NAT Gateways don't support tagging during creation)
aws ec2 create-tags \
  --resources $NAT_GW_ID \
  --tags Key=Name,Value=${PROJECT_NAME}-nat-gw

# Wait for NAT Gateway to become available
aws ec2 wait nat-gateway-available --nat-gateway-ids $NAT_GW_ID
```

#### **Step 4: Create Route Tables**
```bash
# Create Public Route Table
PUBLIC_RT=$(aws ec2 create-route-table \
  --vpc-id $VPC_ID \
  --tag-specifications "ResourceType=route-table,Tags=[{Key=Name,Value=${PROJECT_NAME}-rt-public}]" \
  --query 'RouteTable.RouteTableId' --output text)

# Create Private Route Table
PRIVATE_RT=$(aws ec2 create-route-table \
  --vpc-id $VPC_ID \
  --tag-specifications "ResourceType=route-table,Tags=[{Key=Name,Value=${PROJECT_NAME}-rt-private}]" \
  --query 'RouteTable.RouteTableId' --output text)

# Add routes
aws ec2 create-route --route-table-id $PUBLIC_RT --destination-cidr-block 0.0.0.0/0 --gateway-id $IGW_ID
aws ec2 create-route --route-table-id $PRIVATE_RT --destination-cidr-block 0.0.0.0/0 --nat-gateway-id $NAT_GW_ID

# Associate subnets with route tables
aws ec2 associate-route-table --subnet-id $PUBLIC_SUBNET_1 --route-table-id $PUBLIC_RT
aws ec2 associate-route-table --subnet-id $PUBLIC_SUBNET_2 --route-table-id $PUBLIC_RT
aws ec2 associate-route-table --subnet-id $PRIVATE_SUBNET_1 --route-table-id $PRIVATE_RT
aws ec2 associate-route-table --subnet-id $PRIVATE_SUBNET_2 --route-table-id $PRIVATE_RT
```

#### **Step 5: Create Security Groups**
```bash
# Allow SSH access from anywhere (demo project - acceptable risk)
MY_IP="0.0.0.0/0"

# Create ALB Security Group (optional - for production)
ALB_SG=$(aws ec2 create-security-group \
  --group-name "${PROJECT_NAME}-alb-sg" \
  --description "Security group for Application Load Balancer" \
  --vpc-id $VPC_ID \
  --tag-specifications "ResourceType=security-group,Tags=[{Key=Name,Value=${PROJECT_NAME}-alb-sg}]" \
  --query 'GroupId' --output text)

# ALB Security Group Rules
aws ec2 authorize-security-group-ingress --group-id $ALB_SG --protocol tcp --port 80 --cidr 0.0.0.0/0
aws ec2 authorize-security-group-ingress --group-id $ALB_SG --protocol tcp --port 443 --cidr 0.0.0.0/0

# Create Gateway Security Group (Public Subnet)
GATEWAY_SG=$(aws ec2 create-security-group \
  --group-name "${PROJECT_NAME}-gateway-sg" \
  --description "Security group for API Gateway" \
  --vpc-id $VPC_ID \
  --tag-specifications "ResourceType=security-group,Tags=[{Key=Name,Value=${PROJECT_NAME}-gateway-sg}]" \
  --query 'GroupId' --output text)

# Gateway Security Group Rules
aws ec2 authorize-security-group-ingress --group-id $GATEWAY_SG --protocol tcp --port 22 --cidr $MY_IP
aws ec2 authorize-security-group-ingress --group-id $GATEWAY_SG --protocol tcp --port 9001 --cidr 0.0.0.0/0

# Create WebSocket Client Security Group (Public Subnet)
CLIENT_SG=$(aws ec2 create-security-group \
  --group-name "${PROJECT_NAME}-client-sg" \
  --description "Security group for WebSocket Client" \
  --vpc-id $VPC_ID \
  --tag-specifications "ResourceType=security-group,Tags=[{Key=Name,Value=${PROJECT_NAME}-client-sg}]" \
  --query 'GroupId' --output text)

# Client Security Group Rules
aws ec2 authorize-security-group-ingress --group-id $CLIENT_SG --protocol tcp --port 22 --cidr $MY_IP
aws ec2 authorize-security-group-ingress --group-id $CLIENT_SG --protocol tcp --port 3000 --cidr 0.0.0.0/0  # HTTP server for client

# Create Eureka Security Group (Public Subnet)
EUREKA_SG=$(aws ec2 create-security-group \
  --group-name "${PROJECT_NAME}-eureka-sg" \
  --description "Security group for Eureka Server" \
  --vpc-id $VPC_ID \
  --tag-specifications "ResourceType=security-group,Tags=[{Key=Name,Value=${PROJECT_NAME}-eureka-sg}]" \
  --query 'GroupId' --output text)

# Eureka Security Group Rules
aws ec2 authorize-security-group-ingress --group-id $EUREKA_SG --protocol tcp --port 22 --cidr $MY_IP
aws ec2 authorize-security-group-ingress --group-id $EUREKA_SG --protocol tcp --port 8761 --cidr 0.0.0.0/0  # Dashboard access for demo

# Create Private Services Security Group
PRIVATE_SG=$(aws ec2 create-security-group \
  --group-name "${PROJECT_NAME}-backend-sg" \
  --description "Security group for backend services" \
  --vpc-id $VPC_ID \
  --tag-specifications "ResourceType=security-group,Tags=[{Key=Name,Value=${PROJECT_NAME}-backend-sg}]" \
  --query 'GroupId' --output text)

# Private Services Security Group Rules
aws ec2 authorize-security-group-ingress --group-id $PRIVATE_SG --protocol tcp --port 22 --source-group $GATEWAY_SG
aws ec2 authorize-security-group-ingress --group-id $PRIVATE_SG --protocol tcp --port 9090 --source-group $GATEWAY_SG  # Auth
aws ec2 authorize-security-group-ingress --group-id $PRIVATE_SG --protocol tcp --port 7475 --source-group $GATEWAY_SG  # Booking
aws ec2 authorize-security-group-ingress --group-id $PRIVATE_SG --protocol tcp --port 7477 --source-group $GATEWAY_SG  # Location
aws ec2 authorize-security-group-ingress --group-id $PRIVATE_SG --protocol tcp --port 8080 --source-group $GATEWAY_SG  # Socket
aws ec2 authorize-security-group-ingress --group-id $PRIVATE_SG --protocol tcp --port 7272 --source-group $GATEWAY_SG  # Review
aws ec2 authorize-security-group-ingress --group-id $PRIVATE_SG --protocol tcp --port 8761 --source-group $EUREKA_SG   # Eureka


# Create RDS Security Group
RDS_SG=$(aws ec2 create-security-group \
  --group-name "${PROJECT_NAME}-rds-sg" \
  --description "Security group for RDS MySQL" \
  --vpc-id $VPC_ID \
  --tag-specifications "ResourceType=security-group,Tags=[{Key=Name,Value=${PROJECT_NAME}-rds-sg}]" \
  --query 'GroupId' --output text)

# RDS Security Group Rules
aws ec2 authorize-security-group-ingress --group-id $RDS_SG --protocol tcp --port 3306 --source-group $PRIVATE_SG
aws ec2 authorize-security-group-ingress --group-id $RDS_SG --protocol tcp --port 3306 --source-group $GATEWAY_SG

# Create Redis Security Group
REDIS_SG=$(aws ec2 create-security-group \
  --group-name "${PROJECT_NAME}-redis-sg" \
  --description "Security group for ElastiCache Redis" \
  --vpc-id $VPC_ID \
  --tag-specifications "ResourceType=security-group,Tags=[{Key=Name,Value=${PROJECT_NAME}-redis-sg}]" \
  --query 'GroupId' --output text)

# Redis Security Group Rules
aws ec2 authorize-security-group-ingress --group-id $REDIS_SG --protocol tcp --port 6379 --source-group $PRIVATE_SG

# Create Kafka Security Group
KAFKA_SG=$(aws ec2 create-security-group \
  --group-name "${PROJECT_NAME}-kafka-sg" \
  --description "Security group for MSK Kafka" \
  --vpc-id $VPC_ID \
  --tag-specifications "ResourceType=security-group,Tags=[{Key=Name,Value=${PROJECT_NAME}-kafka-sg}]" \
  --query 'GroupId' --output text)

# Kafka Security Group Rules
aws ec2 authorize-security-group-ingress --group-id $KAFKA_SG --protocol tcp --port 9092 --source-group $PRIVATE_SG

```

**⚠️ Important Security Notes:**

1. **Principle of Least Privilege**: Each security group only allows the minimum required access
2. **No Direct Internet Access**: Backend services only access internet via NAT Gateway
3. **Bastion Access**: SSH to private instances only through Gateway instance
4. **Service-to-Service**: All inter-service communication uses security group references
5. **Database Isolation**: RDS/Redis/Kafka only accessible from backend services
6. **Demo Accessibility**: Eureka dashboard (port 8761) is publicly accessible for demonstration purposes - restrict in production

---

### **Phase 2: Databases & Caching (15 mins)**

#### **Step 1: Create DB Subnet Group**
```bash
# Create DB Subnet Group for RDS
aws rds create-db-subnet-group \
  --db-subnet-group-name "${PROJECT_NAME}-db-subnet-group" \
  --db-subnet-group-description "Subnet group for Uber RDS" \
  --subnet-ids $PRIVATE_SUBNET_1 $PRIVATE_SUBNET_2 \
  --tags Key=Name,Value="${PROJECT_NAME}-db-subnet-group"
```

#### **Step 2: Create RDS MySQL Database**
```bash
# Create RDS MySQL Instance
RDS_INSTANCE_ID="${PROJECT_NAME}-mysql-db"
RDS_PASSWORD="girikgarg"  # CHANGE THIS to your own secure password

aws rds create-db-instance \
  --db-instance-identifier $RDS_INSTANCE_ID \
  --db-instance-class db.t3.micro \
  --engine mysql \
  --engine-version 8.0.44 \
  --master-username admin \
  --master-user-password $RDS_PASSWORD \
  --allocated-storage 20 \
  --storage-type gp2 \
  --vpc-security-group-ids $RDS_SG \
  --db-subnet-group-name "${PROJECT_NAME}-db-subnet-group" \
  --backup-retention-period 0 \
  --no-publicly-accessible \
  --storage-encrypted \
  --tags Key=Name,Value="${PROJECT_NAME}-mysql-db"

# Note: RDS creation takes 5-10 minutes
```

#### **Step 3: Create ElastiCache Subnet Group**
```bash
# Create Cache Subnet Group
aws elasticache create-cache-subnet-group \
  --cache-subnet-group-name "${PROJECT_NAME}-cache-subnet-group" \
  --cache-subnet-group-description "Subnet group for Uber Redis" \
  --subnet-ids $PRIVATE_SUBNET_1 $PRIVATE_SUBNET_2
```

#### **Step 4: Create ElastiCache Redis**
```bash
# Create Redis Cache Cluster
REDIS_CLUSTER_ID="${PROJECT_NAME}-redis-cluster"

aws elasticache create-cache-cluster \
  --cache-cluster-id $REDIS_CLUSTER_ID \
  --engine redis \
  --cache-node-type cache.t3.micro \
  --num-cache-nodes 1 \
  --cache-subnet-group-name "${PROJECT_NAME}-cache-subnet-group" \
  --security-group-ids $REDIS_SG \
  --tags Key=Name,Value="${PROJECT_NAME}-redis-cluster"

# Note: Redis creation takes 3-5 minutes
```

#### **Step 5: Create MSK Kafka Cluster**
```bash
# Create MSK Cluster
MSK_CLUSTER_NAME="${PROJECT_NAME}-kafka-cluster"

# Create MSK cluster
# Note: AWS MSK requires broker nodes to be a multiple of AZs (2 AZs = minimum 2 brokers)
MSK_ARN=$(aws kafka create-cluster \
  --cluster-name $MSK_CLUSTER_NAME \
  --kafka-version "3.7.x" \
  --number-of-broker-nodes 2 \
  --broker-node-group-info "InstanceType=kafka.t3.small,ClientSubnets=[$PRIVATE_SUBNET_1,$PRIVATE_SUBNET_2],SecurityGroups=[$KAFKA_SG],StorageInfo={EbsStorageInfo={VolumeSize=10}}" \
  --encryption-info "EncryptionInTransit={ClientBroker=PLAINTEXT,InCluster=false}" \
  --tags Name=$MSK_CLUSTER_NAME \
  --query 'ClusterArn' --output text)

# Note: MSK creation takes 15-20 minutes (AWS MSK requires minimum 2 brokers across 2 AZs)
```

#### **Step 6: Continue with EC2 Setup (Don't Wait for Databases)**
```bash
echo "🚀 Database resources are provisioning in the background..."
echo "📋 RDS: $RDS_INSTANCE_ID (5-10 minutes)"
echo "📋 Redis: $REDIS_CLUSTER_ID (3-5 minutes)" 
echo "📋 MSK: $MSK_CLUSTER_NAME (15-20 minutes)"
echo ""
echo "⏭️  Continuing with EC2 setup while databases provision..."
echo "💡 We'll get database endpoints later when needed"
```

---

### **Phase 3: EC2 Instances (10 mins)**

#### **Step 1: Create IAM Role for EC2 Instances (CloudWatch & Resource Access)**
```bash
# Create IAM role for EC2 instances to access CloudWatch and AWS services
cat > /tmp/trust-policy.json << EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Principal": {
        "Service": "ec2.amazonaws.com"
      },
      "Action": "sts:AssumeRole"
    }
  ]
}
EOF

# Create the IAM role
aws iam create-role \
  --role-name "${PROJECT_NAME}-ec2-role" \
  --assume-role-policy-document file:///tmp/trust-policy.json

# Create comprehensive IAM policy for CloudWatch and resource discovery
cat > /tmp/ec2-policy.json << EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "logs:CreateLogGroup",
        "logs:CreateLogStream",
        "logs:PutLogEvents",
        "logs:DescribeLogStreams",
        "logs:DescribeLogGroups",
        "ec2:DescribeVolumes",
        "ec2:DescribeTags",
        "ec2:DescribeInstances",
        "cloudwatch:PutMetricData",
        "rds:DescribeDBInstances",
        "elasticache:DescribeCacheClusters",
        "kafka:ListClusters",
        "kafka:DescribeCluster",
        "kafka:GetBootstrapBrokers"
      ],
      "Resource": "*"
    }
  ]
}
EOF

# Create and attach the policy
aws iam create-policy \
  --policy-name "${PROJECT_NAME}-ec2-policy" \
  --policy-document file:///tmp/ec2-policy.json

# Get policy ARN
POLICY_ARN=$(aws iam list-policies \
  --query "Policies[?PolicyName=='${PROJECT_NAME}-ec2-policy'].Arn" --output text)

# Attach policy to role
aws iam attach-role-policy \
  --role-name "${PROJECT_NAME}-ec2-role" \
  --policy-arn $POLICY_ARN

# Create instance profile
aws iam create-instance-profile \
  --instance-profile-name "${PROJECT_NAME}-ec2-profile"

# Add role to instance profile
aws iam add-role-to-instance-profile \
  --instance-profile-name "${PROJECT_NAME}-ec2-profile" \
  --role-name "${PROJECT_NAME}-ec2-role"

# Clean up temp files
rm -f /tmp/trust-policy.json /tmp/ec2-policy.json

echo "✅ IAM role and instance profile created for CloudWatch and resource discovery"
```

#### **Step 2: Create Key Pair**
```bash
# Create SSH key pair and save private key
aws ec2 create-key-pair --key-name "${PROJECT_NAME}-key" \
  --query 'KeyMaterial' --output text > ${PROJECT_NAME}-key.pem

# Set proper permissions
chmod 400 ${PROJECT_NAME}-key.pem
```

#### **Step 3: Create User Data Scripts**
```bash
# Create User Data script for all instances
cat > /tmp/user-data.sh << 'EOF'
#!/bin/bash
yum update -y
yum install -y java-17-amazon-corretto-devel python3 mysql

# Install CloudWatch Agent via YUM (more reliable)
yum install -y amazon-cloudwatch-agent

# Create CloudWatch config with separate log groups per service
mkdir -p /opt/aws/amazon-cloudwatch-agent/etc
tee /opt/aws/amazon-cloudwatch-agent/etc/config.json > /dev/null << 'CWEOF'
{
  "logs": {
    "logs_collected": {
      "files": {
        "collect_list": [
          {
            "file_path": "/var/log/uber-services/eureka.log",
            "log_group_name": "/aws/uber-microservices/eureka",
            "log_stream_name": "{instance_id}",
            "timezone": "UTC"
          },
          {
            "file_path": "/var/log/uber-services/gateway.log",
            "log_group_name": "/aws/uber-microservices/gateway",
            "log_stream_name": "{instance_id}",
            "timezone": "UTC"
          },
          {
            "file_path": "/var/log/uber-services/auth.log",
            "log_group_name": "/aws/uber-microservices/auth",
            "log_stream_name": "{instance_id}",
            "timezone": "UTC"
          },
          {
            "file_path": "/var/log/uber-services/booking.log",
            "log_group_name": "/aws/uber-microservices/booking",
            "log_stream_name": "{instance_id}",
            "timezone": "UTC"
          },
          {
            "file_path": "/var/log/uber-services/location.log",
            "log_group_name": "/aws/uber-microservices/location",
            "log_stream_name": "{instance_id}",
            "timezone": "UTC"
          },
          {
            "file_path": "/var/log/uber-services/socket.log",
            "log_group_name": "/aws/uber-microservices/socket",
            "log_stream_name": "{instance_id}",
            "timezone": "UTC"
          },
          {
            "file_path": "/var/log/uber-services/review.log",
            "log_group_name": "/aws/uber-microservices/review",
            "log_stream_name": "{instance_id}",
            "timezone": "UTC"
          }
        ]
      }
    }
  }
}
CWEOF

# Create log directory
mkdir -p /var/log/uber-services
chown ec2-user:ec2-user /var/log/uber-services

# Start CloudWatch agent
/opt/aws/amazon-cloudwatch-agent/bin/amazon-cloudwatch-agent-ctl \
  -a fetch-config -m ec2 -s \
  -c file:/opt/aws/amazon-cloudwatch-agent/etc/config.json

# Enable CloudWatch agent service
systemctl enable amazon-cloudwatch-agent
systemctl start amazon-cloudwatch-agent
EOF

# Base64 encode the user data
USER_DATA=$(base64 -i /tmp/user-data.sh)
```

#### **Step 4: Launch EC2 Instances**
```bash
# Get latest Amazon Linux 2023 AMI ID
AMI_ID=$(aws ec2 describe-images \
  --owners amazon \
  --filters "Name=name,Values=al2023-ami-*-x86_64" "Name=state,Values=available" \
  --query 'Images | sort_by(@, &CreationDate) | [-1].ImageId' --output text)

# Launch Gateway Instance (Public Subnet)
GATEWAY_INSTANCE=$(aws ec2 run-instances \
  --image-id $AMI_ID \
  --count 1 \
  --instance-type t3.micro \
  --key-name "${PROJECT_NAME}-key" \
  --security-group-ids $GATEWAY_SG \
  --subnet-id $PUBLIC_SUBNET_1 \
  --iam-instance-profile Name="${PROJECT_NAME}-ec2-profile" \
  --user-data $USER_DATA \
  --tag-specifications "ResourceType=instance,Tags=[{Key=Name,Value=${PROJECT_NAME}-gateway}]" \
  --query 'Instances[0].InstanceId' --output text)

# Launch Eureka Instance (Public Subnet)
EUREKA_INSTANCE=$(aws ec2 run-instances \
  --image-id $AMI_ID \
  --count 1 \
  --instance-type t3.micro \
  --key-name "${PROJECT_NAME}-key" \
  --security-group-ids $EUREKA_SG \
  --subnet-id $PUBLIC_SUBNET_2 \
  --iam-instance-profile Name="${PROJECT_NAME}-ec2-profile" \
  --user-data $USER_DATA \
  --tag-specifications "ResourceType=instance,Tags=[{Key=Name,Value=${PROJECT_NAME}-eureka}]" \
  --query 'Instances[0].InstanceId' --output text)

# Launch WebSocket Client Instance (Public Subnet)
CLIENT_INSTANCE=$(aws ec2 run-instances \
  --image-id $AMI_ID \
  --count 1 \
  --instance-type t3.micro \
  --key-name "${PROJECT_NAME}-key" \
  --security-group-ids $CLIENT_SG \
  --subnet-id $PUBLIC_SUBNET_1 \
  --iam-instance-profile Name="${PROJECT_NAME}-ec2-profile" \
  --user-data $USER_DATA \
  --tag-specifications "ResourceType=instance,Tags=[{Key=Name,Value=${PROJECT_NAME}-client}]" \
  --query 'Instances[0].InstanceId' --output text)

# Launch Backend Services Instance (Private Subnet)
BACKEND_INSTANCE=$(aws ec2 run-instances \
  --image-id $AMI_ID \
  --count 1 \
  --instance-type t3.small \
  --key-name "${PROJECT_NAME}-key" \
  --security-group-ids $PRIVATE_SG \
  --subnet-id $PRIVATE_SUBNET_1 \
  --iam-instance-profile Name="${PROJECT_NAME}-ec2-profile" \
  --user-data $USER_DATA \
  --tag-specifications "ResourceType=instance,Tags=[{Key=Name,Value=${PROJECT_NAME}-backend}]" \
  --query 'Instances[0].InstanceId' --output text)

# Clean up temp file
rm -f /tmp/user-data.sh

echo "✅ All EC2 instances launched with CloudWatch agent and IAM roles"

# Wait for IAM role to propagate
echo "⏳ Waiting for IAM roles to propagate (30 seconds)..."
sleep 30
```

#### **Step 5: Wait for Instances and Get IPs**
```bash
# Wait for instances to be running
aws ec2 wait instance-running --instance-ids $GATEWAY_INSTANCE $EUREKA_INSTANCE $CLIENT_INSTANCE $BACKEND_INSTANCE

# Get instance IPs
GATEWAY_PUBLIC_IP=$(aws ec2 describe-instances \
  --instance-ids $GATEWAY_INSTANCE \
  --query 'Reservations[0].Instances[0].PublicIpAddress' --output text)

GATEWAY_PRIVATE_IP=$(aws ec2 describe-instances \
  --instance-ids $GATEWAY_INSTANCE \
  --query 'Reservations[0].Instances[0].PrivateIpAddress' --output text)

EUREKA_PUBLIC_IP=$(aws ec2 describe-instances \
  --instance-ids $EUREKA_INSTANCE \
  --query 'Reservations[0].Instances[0].PublicIpAddress' --output text)

EUREKA_PRIVATE_IP=$(aws ec2 describe-instances \
  --instance-ids $EUREKA_INSTANCE \
  --query 'Reservations[0].Instances[0].PrivateIpAddress' --output text)

CLIENT_PUBLIC_IP=$(aws ec2 describe-instances \
  --instance-ids $CLIENT_INSTANCE \
  --query 'Reservations[0].Instances[0].PublicIpAddress' --output text)

CLIENT_PRIVATE_IP=$(aws ec2 describe-instances \
  --instance-ids $CLIENT_INSTANCE \
  --query 'Reservations[0].Instances[0].PrivateIpAddress' --output text)

BACKEND_PRIVATE_IP=$(aws ec2 describe-instances \
  --instance-ids $BACKEND_INSTANCE \
  --query 'Reservations[0].Instances[0].PrivateIpAddress' --output text)

echo ""
echo "✅ EC2 instances are ready!"
echo "🔄 Now let's get database endpoints (they should be ready by now)..."
```

#### **Step 6: Get Database Endpoints (Wait if Needed)**
```bash
# Function to wait for and get database endpoints
get_database_endpoints() {
    echo "🔍 Checking database status..."
    
    # Wait for RDS if not ready
    echo "⏳ Waiting for RDS to be available..."
    aws rds wait db-instance-available --db-instance-identifier $RDS_INSTANCE_ID
    RDS_ENDPOINT=$(aws rds describe-db-instances \
      --db-instance-identifier $RDS_INSTANCE_ID \
      --query 'DBInstances[0].Endpoint.Address' --output text)
    echo "✅ RDS Ready: $RDS_ENDPOINT"
    
    # Wait for Redis if not ready
    echo "⏳ Waiting for Redis to be available..."
    aws elasticache wait cache-cluster-available --cache-cluster-id $REDIS_CLUSTER_ID
    REDIS_ENDPOINT=$(aws elasticache describe-cache-clusters \
      --cache-cluster-id $REDIS_CLUSTER_ID \
      --show-cache-node-info \
      --query 'CacheClusters[0].CacheNodes[0].Endpoint.Address' --output text)
    echo "✅ Redis Ready: $REDIS_ENDPOINT"
    
    # Wait for MSK if not ready
    echo "⏳ Waiting for MSK to be active..."
    while true; do
        MSK_STATE=$(aws kafka describe-cluster --cluster-arn $MSK_ARN --query 'ClusterInfo.State' --output text 2>/dev/null || echo "CREATING")
        if [ "$MSK_STATE" = "ACTIVE" ]; then
            echo "✅ MSK is now ACTIVE"
            break
        else
            echo "   MSK Status: $MSK_STATE (waiting 30 seconds...)"
            sleep 30
        fi
    done
    
    MSK_BROKERS=$(aws kafka get-bootstrap-brokers \
      --cluster-arn $MSK_ARN \
      --query 'BootstrapBrokerStringVpcConnectivityOnly' --output text)
    echo "✅ MSK Ready: $MSK_BROKERS"
    
    echo ""
    echo "🎉 All database resources are ready!"
}

# Call the function to get endpoints
get_database_endpoints
```

---

### **Phase 4: CloudWatch Logs Setup**

#### **Create Log Groups:**
```bash
# Create CloudWatch log groups for all services
LOG_GROUPS=(
  "/aws/uber-microservices/eureka"
  "/aws/uber-microservices/gateway"
  "/aws/uber-microservices/auth"
  "/aws/uber-microservices/booking"
  "/aws/uber-microservices/location"
  "/aws/uber-microservices/socket"
  "/aws/uber-microservices/review"
)

for LOG_GROUP in "${LOG_GROUPS[@]}"; do
  aws logs create-log-group --log-group-name "$LOG_GROUP" 2>/dev/null || echo "Log group $LOG_GROUP already exists"
  
  # Set retention policy (optional - 7 days)
  aws logs put-retention-policy \
    --log-group-name "$LOG_GROUP" \
    --retention-in-days 7 2>/dev/null || echo "Retention policy set for $LOG_GROUP"
done
```

**💡 Note:** CloudWatch agent is automatically installed via User Data scripts in the EC2 instance configurations above.

### **🔧 Manual IAM Role Attachment (If Needed)**

If you need to attach IAM roles to existing instances:

```bash
# Attach IAM roles to all instances (run from local terminal)
echo "🔧 Attaching IAM roles to all EC2 instances..."

# Get instance IDs
GATEWAY_INSTANCE_ID=$(aws ec2 describe-instances \
  --filters "Name=tag:Name,Values=uber-platform-gateway" "Name=instance-state-name,Values=running" \
  --query 'Reservations[0].Instances[0].InstanceId' --output text)

EUREKA_INSTANCE_ID=$(aws ec2 describe-instances \
  --filters "Name=tag:Name,Values=uber-platform-eureka" "Name=instance-state-name,Values=running" \
  --query 'Reservations[0].Instances[0].InstanceId' --output text)

CLIENT_INSTANCE_ID=$(aws ec2 describe-instances \
  --filters "Name=tag:Name,Values=uber-platform-client" "Name=instance-state-name,Values=running" \
  --query 'Reservations[0].Instances[0].InstanceId' --output text)

BACKEND_INSTANCE_ID=$(aws ec2 describe-instances \
  --filters "Name=tag:Name,Values=uber-platform-backend" "Name=instance-state-name,Values=running" \
  --query 'Reservations[0].Instances[0].InstanceId' --output text)

# Attach IAM instance profile to all instances
for INSTANCE_ID in $GATEWAY_INSTANCE_ID $EUREKA_INSTANCE_ID $CLIENT_INSTANCE_ID $BACKEND_INSTANCE_ID; do
  if [ "$INSTANCE_ID" != "None" ] && [ ! -z "$INSTANCE_ID" ]; then
    echo "Attaching IAM role to instance: $INSTANCE_ID"
    aws ec2 associate-iam-instance-profile \
      --instance-id $INSTANCE_ID \
      --iam-instance-profile Name="uber-platform-ec2-profile" 2>/dev/null || echo "IAM role already attached to $INSTANCE_ID"
  fi
done

echo "✅ IAM roles attached to all instances"
echo "⏳ Wait 30 seconds for IAM roles to propagate..."
sleep 30
```

### **🔧 Manual CloudWatch Agent Installation (If Needed)**

If CloudWatch agent wasn't installed via User Data:

```bash
# SSH to each instance and run:
sudo yum update -y
sudo yum install -y amazon-cloudwatch-agent

# Create log directory
sudo mkdir -p /var/log/uber-services
sudo chown ec2-user:ec2-user /var/log/uber-services

# Create appropriate CloudWatch config for each instance:
# - Eureka: monitors all service logs
# - Gateway: monitors gateway.log only  
# - Backend: monitors all backend service logs
# - Client: no CloudWatch needed

# For Gateway instance:
sudo tee /opt/aws/amazon-cloudwatch-agent/etc/config.json > /dev/null << 'EOF'
{
  "logs": {
    "logs_collected": {
      "files": {
        "collect_list": [
          {
            "file_path": "/var/log/uber-services/gateway.log",
            "log_group_name": "/aws/uber-microservices/gateway",
            "log_stream_name": "{instance_id}",
            "timezone": "UTC"
          }
        ]
      }
    }
  }
}
EOF

# Start CloudWatch agent
sudo /opt/aws/amazon-cloudwatch-agent/bin/amazon-cloudwatch-agent-ctl \
  -a fetch-config -m ec2 -s \
  -c file:/opt/aws/amazon-cloudwatch-agent/etc/config.json

sudo systemctl enable amazon-cloudwatch-agent
sudo systemctl start amazon-cloudwatch-agent
```

### **🔧 Troubleshooting Service Discovery Issues**

#### **Problem: Services Register with 127.0.0.1 Instead of Private IP**

**Symptoms:**
- Gateway shows "Connection refused" errors when calling backend services
- Services appear in Eureka but with `hostName: 127.0.0.1` and `ipAddr: 127.0.0.1`
- 500 Internal Server Error when calling APIs through Gateway

**Root Cause:**
Services are registering with Eureka using localhost (127.0.0.1) instead of their actual private IP addresses, making them unreachable from other instances.

**Solution:**
Restart services with explicit IP configuration:

```bash
# For ANY service that shows 127.0.0.1 in Eureka, restart with:
pkill -f "ServiceName"

nohup java -jar \
  -Dspring.profiles.active=prod \
  -Dserver.address=0.0.0.0 \
  -Deureka.instance.prefer-ip-address=true \
  -Deureka.instance.ip-address=$(curl -s http://169.254.169.254/latest/meta-data/local-ipv4) \
  -Deureka.instance.hostname=$(curl -s http://169.254.169.254/latest/meta-data/local-ipv4) \
  ServiceName.jar > /var/log/uber-services/service.log 2>&1 &
```

**Key Parameters:**
- `-Deureka.instance.prefer-ip-address=true` - Use IP instead of hostname
- `-Deureka.instance.ip-address=$(...)` - Set explicit IP address  
- `-Deureka.instance.hostname=$(...)` - Set hostname to IP as well
- `-Dserver.address=0.0.0.0` - Bind to all interfaces (for Location Service)

**Verification:**
```bash
# Check Eureka registration shows correct private IP
curl -s http://<eureka-ip>:8761/eureka/apps/SERVICE-NAME | grep -E "(hostName|ipAddr)"
# Should show: <hostName>10.0.x.x</hostName> and <ipAddr>10.0.x.x</ipAddr>
```

#### **Problem: Location Service Cannot Connect to Redis**

**Symptoms:**
- Location Service returns `false` for save operations
- Logs show `java.net.ConnectException: Connection refused`
- Redis connectivity test passes but service still fails

**Solution:**
Restart Location Service with explicit Redis configuration:

```bash
pkill -f "Uber-Location-Service"

nohup java -jar \
  -Dspring.profiles.active=prod \
  -Dserver.address=0.0.0.0 \
  -Deureka.instance.prefer-ip-address=true \
  -Deureka.instance.ip-address=$(curl -s http://169.254.169.254/latest/meta-data/local-ipv4) \
  -Deureka.instance.hostname=$(curl -s http://169.254.169.254/latest/meta-data/local-ipv4) \
  -Dspring.data.redis.host=$REDIS_ENDPOINT \
  -Dspring.data.redis.port=6379 \
  Uber-Location-Service-0.0.1-SNAPSHOT.jar > /var/log/uber-services/location.log 2>&1 &
```

### **🔧 Troubleshooting CloudWatch Agent**

If CloudWatch agent fails or needs reconfiguration:

#### **Restart CloudWatch Agent (if needed)**
```bash
# Stop and restart CloudWatch agent
sudo systemctl stop amazon-cloudwatch-agent

# Restart with configuration
sudo /opt/aws/amazon-cloudwatch-agent/bin/amazon-cloudwatch-agent-ctl \
  -a fetch-config -m ec2 -s \
  -c file:/opt/aws/amazon-cloudwatch-agent/etc/config.json

# Check status
sudo systemctl status amazon-cloudwatch-agent

# Check logs
sudo tail -20 /opt/aws/amazon-cloudwatch-agent/logs/amazon-cloudwatch-agent.log
```

#### **Kill Process on Port (Alternative Methods)**
```bash
# Method 1: Kill by process name (most reliable)
pkill -f "Uber-Service-Discovery"
pkill -f "eureka"

# Method 2: Using netstat (if lsof not available)
PID=$(netstat -tulpn 2>/dev/null | grep :8761 | awk '{print $7}' | cut -d'/' -f1)
if [ ! -z "$PID" ]; then
    kill -9 $PID
    echo "Killed process $PID on port 8761"
fi

# Verify port is free
netstat -tulpn | grep :8761 || echo "Port 8761 is free"
```

---

### **🎯 Infrastructure Summary & Environment Variables**

#### **Save All Resource Information**
```bash
# Create infrastructure summary file
cat > infrastructure-summary.txt << EOF
# Uber Platform Infrastructure Summary
# Generated: $(date)

# VPC & Networking
VPC_ID=$VPC_ID
PUBLIC_SUBNET_1=$PUBLIC_SUBNET_1
PUBLIC_SUBNET_2=$PUBLIC_SUBNET_2
PRIVATE_SUBNET_1=$PRIVATE_SUBNET_1
PRIVATE_SUBNET_2=$PRIVATE_SUBNET_2
IGW_ID=$IGW_ID
NAT_GW_ID=$NAT_GW_ID
NAT_EIP=$NAT_EIP

# Security Groups
ALB_SG=$ALB_SG
GATEWAY_SG=$GATEWAY_SG
EUREKA_SG=$EUREKA_SG
CLIENT_SG=$CLIENT_SG
PRIVATE_SG=$PRIVATE_SG
RDS_SG=$RDS_SG
REDIS_SG=$REDIS_SG
KAFKA_SG=$KAFKA_SG

# EC2 Instances
GATEWAY_INSTANCE=$GATEWAY_INSTANCE
EUREKA_INSTANCE=$EUREKA_INSTANCE
CLIENT_INSTANCE=$CLIENT_INSTANCE
BACKEND_INSTANCE=$BACKEND_INSTANCE

# Instance IPs
GATEWAY_PUBLIC_IP=$GATEWAY_PUBLIC_IP
GATEWAY_PRIVATE_IP=$GATEWAY_PRIVATE_IP
EUREKA_PUBLIC_IP=$EUREKA_PUBLIC_IP
EUREKA_PRIVATE_IP=$EUREKA_PRIVATE_IP
CLIENT_PUBLIC_IP=$CLIENT_PUBLIC_IP
CLIENT_PRIVATE_IP=$CLIENT_PRIVATE_IP
BACKEND_PRIVATE_IP=$BACKEND_PRIVATE_IP

# Database & Cache
RDS_INSTANCE_ID=$RDS_INSTANCE_ID
RDS_PASSWORD=$RDS_PASSWORD
REDIS_CLUSTER_ID=$REDIS_CLUSTER_ID
MSK_ARN=$MSK_ARN

# Key Pair
KEY_NAME=${PROJECT_NAME}-key

# Project Info
PROJECT_NAME=$PROJECT_NAME
REGION=$REGION
EOF

```

#### **Get Resource Endpoints (Run after resources are ready)**
```bash
# Function to get all endpoints
get_endpoints() {
    # RDS Endpoint
    if [ ! -z "$RDS_INSTANCE_ID" ]; then
        RDS_ENDPOINT=$(aws rds describe-db-instances \
          --db-instance-identifier $RDS_INSTANCE_ID \
          --query 'DBInstances[0].Endpoint.Address' --output text 2>/dev/null || echo "Not ready")
        echo "RDS_ENDPOINT=$RDS_ENDPOINT"
    fi
    
    # Redis Endpoint
    if [ ! -z "$REDIS_CLUSTER_ID" ]; then
        REDIS_ENDPOINT=$(aws elasticache describe-cache-clusters \
          --cache-cluster-id $REDIS_CLUSTER_ID \
          --show-cache-node-info \
          --query 'CacheClusters[0].CacheNodes[0].Endpoint.Address' --output text 2>/dev/null || echo "Not ready")
        echo "REDIS_ENDPOINT=$REDIS_ENDPOINT"
    fi
    
    # MSK Brokers
    if [ ! -z "$MSK_ARN" ]; then
        MSK_BROKERS=$(aws kafka get-bootstrap-brokers \
          --cluster-arn $MSK_ARN \
          --query 'BootstrapBrokerStringVpcConnectivityOnly' --output text 2>/dev/null || echo "Not ready")
        echo "MSK_BROKERS=$MSK_BROKERS"
    fi
    
    # ALB DNS (if created)
    if [ ! -z "$ALB_ARN" ]; then
        ALB_DNS=$(aws elbv2 describe-load-balancers \
          --load-balancer-arns $ALB_ARN \
          --query 'LoadBalancers[0].DNSName' --output text 2>/dev/null || echo "Not created")
        echo "ALB_DNS=$ALB_DNS"
    fi
}

# Call the function
get_endpoints
```

---

## 📦 Application Deployment

### **Phase 0: Spring Profiles Configuration (COMPLETED)**

✅ **Production configuration files have been created** in all microservices using Spring Profiles:
- `application.properties` → Common configuration (all environments)
- `application-prod.properties` → AWS production-specific configuration

This approach allows the same JAR to work in both local and AWS environments by simply changing the active profile.


#### **Step 3: Environment Variables Setup Guide**

**🔧 Environment Variables Required by Each Service:**

| Service | Variables Needed |
|---------|------------------|
| **API Gateway** | `EUREKA_PRIVATE_IP`, `EC2_PRIVATE_IP` |
| **Eureka Server** | `EC2_PRIVATE_IP` |
| **Auth Service** | `RDS_ENDPOINT`, `RDS_USERNAME`, `RDS_PASSWORD`, `EUREKA_PRIVATE_IP`, `EC2_PRIVATE_IP`, `JWT_SECRET` |
| **Entity Service** | `RDS_ENDPOINT`, `RDS_USERNAME`, `RDS_PASSWORD`, `EUREKA_PRIVATE_IP`, `EC2_PRIVATE_IP` |
| **Booking Service** | `RDS_ENDPOINT`, `RDS_USERNAME`, `RDS_PASSWORD`, `MSK_BROKERS`, `EUREKA_PRIVATE_IP`, `EC2_PRIVATE_IP` |
| **Location Service** | `ELASTICACHE_ENDPOINT`, `EUREKA_PRIVATE_IP`, `EC2_PRIVATE_IP` |
| **Socket Service** | `MSK_BROKERS`, `EUREKA_PRIVATE_IP`, `EC2_PRIVATE_IP` |
| **Review Service** | `RDS_ENDPOINT`, `RDS_USERNAME`, `RDS_PASSWORD`, `EUREKA_PRIVATE_IP`, `EC2_PRIVATE_IP` |

**📋 How to Set Environment Variables on EC2 Instances:**

Create `~/aws-env.sh` script on each EC2 instance with the required variables:

**🔐 Security Best Practices for Environment Variables:**

1. **Generate Strong JWT Secret:**
```bash
# Generate a secure JWT secret
JWT_SECRET=$(openssl rand -base64 32)
echo "Generated JWT Secret: $JWT_SECRET"
```

2. **Restrict File Permissions:**
```bash
# Make environment script readable only by owner
chmod 600 ~/aws-env.sh
```


**📝 Automated Environment Variable Setup:**

**🎯 One-Command Setup Script (Run from LOCAL terminal):**

```bash
# Create automated setup script
cat > setup-env-vars.sh << 'EOF'
#!/bin/bash
echo "🔍 Getting all AWS resource endpoints..."

# Get database endpoints
RDS_ENDPOINT=$(aws rds describe-db-instances --db-instance-identifier uber-platform-mysql-db --query 'DBInstances[0].Endpoint.Address' --output text)
REDIS_ENDPOINT=$(aws elasticache describe-cache-clusters --cache-cluster-id uber-platform-redis-cluster --show-cache-node-info --query 'CacheClusters[0].CacheNodes[0].Endpoint.Address' --output text)
MSK_ARN=$(aws kafka list-clusters --query "ClusterInfoList[?ClusterName=='uber-platform-kafka-cluster'].ClusterArn" --output text)
MSK_BROKERS=$(aws kafka get-bootstrap-brokers --cluster-arn $MSK_ARN --query 'BootstrapBrokerStringVpcConnectivityOnly' --output text)

# Get instance IPs
EUREKA_PRIVATE_IP=$(aws ec2 describe-instances --filters "Name=tag:Name,Values=uber-platform-eureka" "Name=instance-state-name,Values=running" --query 'Reservations[0].Instances[0].PrivateIpAddress' --output text)
GATEWAY_PUBLIC_IP=$(aws ec2 describe-instances --filters "Name=tag:Name,Values=uber-platform-gateway" "Name=instance-state-name,Values=running" --query 'Reservations[0].Instances[0].PublicIpAddress' --output text)
CLIENT_PUBLIC_IP=$(aws ec2 describe-instances --filters "Name=tag:Name,Values=uber-platform-client" "Name=instance-state-name,Values=running" --query 'Reservations[0].Instances[0].PublicIpAddress' --output text)
BACKEND_PRIVATE_IP=$(aws ec2 describe-instances --filters "Name=tag:Name,Values=uber-platform-backend" "Name=instance-state-name,Values=running" --query 'Reservations[0].Instances[0].PrivateIpAddress' --output text)

# Set your password
RDS_PASSWORD="girikgarg"  # Replace with your actual secure password

echo "✅ All endpoints retrieved:"
echo "   RDS_ENDPOINT: $RDS_ENDPOINT"
echo "   REDIS_ENDPOINT: $REDIS_ENDPOINT"
echo "   MSK_BROKERS: $MSK_BROKERS"
echo "   EUREKA_PRIVATE_IP: $EUREKA_PRIVATE_IP"
echo "   GATEWAY_PUBLIC_IP: $GATEWAY_PUBLIC_IP"
echo "   CLIENT_PUBLIC_IP: $CLIENT_PUBLIC_IP"
echo "   BACKEND_PRIVATE_IP: $BACKEND_PRIVATE_IP"

echo ""
echo "🚀 Setting up environment variables on all instances..."

# Setup Gateway Instance
echo "📡 Setting up Gateway instance..."
ssh -i uber-platform-key.pem ec2-user@$GATEWAY_PUBLIC_IP << GATEWAY_EOF
cat > ~/.bashrc_uber << 'INNER_EOF'
#!/bin/bash
# Gateway Environment Variables
TOKEN=\$(curl -X PUT "http://169.254.169.254/latest/api/token" -H "X-aws-ec2-metadata-token-ttl-seconds: 21600" 2>/dev/null)
export EC2_PRIVATE_IP=\$(curl -H "X-aws-ec2-metadata-token: \$TOKEN" -s http://169.254.169.254/latest/meta-data/local-ipv4 2>/dev/null)
export EUREKA_PRIVATE_IP="$EUREKA_PRIVATE_IP"
INNER_EOF
echo "source ~/.bashrc_uber" >> ~/.bashrc
source ~/.bashrc_uber
echo "✅ Gateway configured"
GATEWAY_EOF

# Setup Eureka Instance
echo "🔍 Setting up Eureka instance..."
ssh -i uber-platform-key.pem ec2-user@$EUREKA_PRIVATE_IP << EUREKA_EOF
cat > ~/.bashrc_uber << 'INNER_EOF'
#!/bin/bash
# Eureka Environment Variables
TOKEN=\$(curl -X PUT "http://169.254.169.254/latest/api/token" -H "X-aws-ec2-metadata-token-ttl-seconds: 21600" 2>/dev/null)
export EC2_PRIVATE_IP=\$(curl -H "X-aws-ec2-metadata-token: \$TOKEN" -s http://169.254.169.254/latest/meta-data/local-ipv4 2>/dev/null)
INNER_EOF
echo "source ~/.bashrc_uber" >> ~/.bashrc
source ~/.bashrc_uber
echo "✅ Eureka configured"
EUREKA_EOF

# Setup Backend Instance (via Gateway as bastion)
echo "⚙️ Setting up Backend instance..."
# First copy SSH key to Gateway
scp -i uber-platform-key.pem uber-platform-key.pem ec2-user@$GATEWAY_PUBLIC_IP:~/
# Then setup backend via Gateway
ssh -i uber-platform-key.pem ec2-user@$GATEWAY_PUBLIC_IP << BACKEND_EOF
ssh -i uber-platform-key.pem ec2-user@$BACKEND_PRIVATE_IP << 'INNER_BACKEND_EOF'
cat > ~/.bashrc_uber << 'UBER_EOF'
#!/bin/bash
# Backend Environment Variables
TOKEN=\$(curl -X PUT "http://169.254.169.254/latest/api/token" -H "X-aws-ec2-metadata-token-ttl-seconds: 21600" 2>/dev/null)
export EC2_PRIVATE_IP=\$(curl -H "X-aws-ec2-metadata-token: \$TOKEN" -s http://169.254.169.254/latest/meta-data/local-ipv4 2>/dev/null)
export RDS_ENDPOINT="$RDS_ENDPOINT"
export REDIS_ENDPOINT="$REDIS_ENDPOINT"
export MSK_BROKERS="$MSK_BROKERS"
export EUREKA_PRIVATE_IP="$EUREKA_PRIVATE_IP"
export RDS_USERNAME="admin"
export RDS_PASSWORD="$RDS_PASSWORD"
export JWT_SECRET="\$(openssl rand -base64 32)"
export ELASTICACHE_ENDPOINT="\$REDIS_ENDPOINT"
export MYSQL_LOCAL_PASSWORD="\$RDS_PASSWORD"
UBER_EOF
echo "source ~/.bashrc_uber" >> ~/.bashrc
source ~/.bashrc_uber
echo "✅ Backend configured"
INNER_BACKEND_EOF
BACKEND_EOF

# Setup Client Instance
echo "🚗 Setting up Client instance..."
ssh -i uber-platform-key.pem ec2-user@$CLIENT_PUBLIC_IP << CLIENT_EOF
cat > ~/.bashrc_uber << 'INNER_EOF'
#!/bin/bash
# Client Environment Variables
TOKEN=\$(curl -X PUT "http://169.254.169.254/latest/api/token" -H "X-aws-ec2-metadata-token-ttl-seconds: 21600" 2>/dev/null)
export EC2_PRIVATE_IP=\$(curl -H "X-aws-ec2-metadata-token: \$TOKEN" -s http://169.254.169.254/latest/meta-data/local-ipv4 2>/dev/null)
export GATEWAY_PUBLIC_IP="$GATEWAY_PUBLIC_IP"
INNER_EOF
echo "source ~/.bashrc_uber" >> ~/.bashrc
source ~/.bashrc_uber
echo "✅ Client configured"
CLIENT_EOF

echo ""
echo "🎉 All instances configured! Environment variables are persistent across SSH sessions."
echo ""
echo "🔍 To verify, SSH to any instance and run: env | grep -E '(RDS|REDIS|MSK|EUREKA)'"
EOF

chmod +x setup-env-vars.sh

# Run the setup script
./setup-env-vars.sh
```

**✨ Benefits of This Approach:**
- **One command does everything** - No manual exports needed
- **Fully automated** - Retrieves endpoints and configures all instances
- **Persistent variables** - Survive SSH disconnections  
- **No manual copying** - Script handles all SSH connections
- **Handles bastion host** - Automatically uses Gateway for Backend access

**🎯 That's it! The automated script handles everything:**

- ✅ **Retrieves all endpoints** from AWS
- ✅ **SSH to each instance** automatically  
- ✅ **Creates persistent environment files** (`~/.bashrc_uber`)
- ✅ **Configures auto-loading** on SSH login
- ✅ **Handles bastion host** for Backend instance access
- ✅ **No manual exports needed** - Everything is automated

**To verify setup worked:**
```bash
# SSH to any instance and check variables
ssh -i uber-platform-key.pem ec2-user@<instance-ip>
env | grep -E '(RDS|REDIS|MSK|EUREKA)'
```

**🔧 Get AWS Resource Endpoints (Use values from infrastructure setup):**

```bash
# Load infrastructure summary (if you saved it)
source infrastructure-summary.txt 2>/dev/null || echo "Run infrastructure setup first"

# Get RDS Endpoint
RDS_ENDPOINT=$(aws rds describe-db-instances \
  --db-instance-identifier $RDS_INSTANCE_ID \
  --query 'DBInstances[0].Endpoint.Address' --output text)

# Get ElastiCache Endpoint
ELASTICACHE_ENDPOINT=$(aws elasticache describe-cache-clusters \
  --cache-cluster-id $REDIS_CLUSTER_ID \
  --show-cache-node-info \
  --query 'CacheClusters[0].CacheNodes[0].Endpoint.Address' --output text)

# Get MSK Brokers (Private endpoint for VPC-internal access)
MSK_BROKERS=$(aws kafka get-bootstrap-brokers \
  --cluster-arn $MSK_ARN \
  --query 'BootstrapBrokerStringVpcConnectivityOnly' --output text)

```

**🚀 Usage Before Starting Services:**

```bash
# Always source environment variables before starting any service
source ~/aws-env.sh

# Then start services with production profile
java -jar -Dspring.profiles.active=prod UberAuth-Service-0.0.1-SNAPSHOT.jar
```

---

### **Phase 1: Build All Services (30 mins)**

```bash
cd "Microservices-Based Ride-Hailing Platform"

# Build Entity Service first (dependency for other services)
cd Uber-Entity-Service
./gradlew clean build publishToMavenLocal -x test
cd ..

# Build each service with production profile included
for service in Uber-Service-Discovery Uber-Auth-Service Uber-Booking-Service \
               Uber-Location-Service Uber-Socket-Service Uber-Review-Service \
               Uber-API-Gateway; do
  echo "Building $service..."
  cd $service && ./gradlew clean build -x test && cd ..
done

echo "✅ All services built successfully!"
```

**Build Order Matters:**
1. Entity Service (shared dependency) - MUST BE FIRST
2. All other services (any order)
3. API Gateway (last, but can be built anytime)

**CRITICAL: Entity Service Setup**
The Entity Service contains:
- All JPA entities (Driver, Passenger, Booking, etc.)
- Flyway database migrations
- Must be built and published to local Maven first
- Must be started first in AWS to run migrations

### **Phase 2: Upload JARs to EC2**

**💡 Important: JAR File Names**
- Gradle generates JARs with format: `Service-Name-0.0.1-SNAPSHOT.jar` (fat JAR with dependencies)
- Also creates: `Service-Name-0.0.1-SNAPSHOT-plain.jar` (classes only - don't use this)
- Always use the **fat JAR** (larger file) for deployment

```bash
# Gateway (to public instance)
scp -i uber-platform-key.pem \
  Uber-API-Gateway/build/libs/*.jar \
  ec2-user@<gateway-public-ip>:~/

# Eureka (to public instance)
scp -i uber-platform-key.pem \
  Uber-Service-Discovery/build/libs/*.jar \
  ec2-user@<eureka-public-ip>:~/

# Backend services (to private instance via Gateway as bastion)
# First, create directory on Gateway and upload JARs
ssh -i uber-platform-key.pem ec2-user@<gateway-public-ip> "mkdir -p ~/backend"

scp -i uber-platform-key.pem \
  Uber-Entity-Service/build/libs/*.jar \
  Uber-Auth-Service/build/libs/*.jar \
  Uber-Booking-Service/build/libs/*.jar \
  Uber-Location-Service/build/libs/*.jar \
  Uber-Socket-Service/build/libs/*.jar \
  Uber-Review-Service/build/libs/*.jar \
  ec2-user@<gateway-public-ip>:~/backend/

# Then from Gateway to Private instance
ssh -i uber-platform-key.pem ec2-user@<gateway-public-ip>
scp -i uber-platform-key.pem ~/backend/*-SNAPSHOT.jar ec2-user@<backend-private-ip>:~/
```
---

### **Phase 3: Database Initialization (10 mins)**

#### **Step 1: Install MySQL Client (if not already installed)**

```bash
# Install MySQL 8.0 client on Amazon Linux 2023
sudo dnf update -y
sudo dnf install mysql -y

# Verify installation
mysql --version
# Should show: mysql  Ver 8.0.x for Linux on x86_64
```

#### **Step 2: Initialize Database**

```bash
# Connect to RDS (ensure RDS_ENDPOINT is set from environment variables)
source ~/aws-env.sh
echo "Connecting to RDS: $RDS_ENDPOINT"

mysql -h $RDS_ENDPOINT -u admin -p

# Create database and verify
CREATE DATABASE IF NOT EXISTS Uber_Db_Prod;
USE Uber_Db_Prod;
SHOW TABLES;  -- Should be empty initially

# Exit MySQL
SHOW TABLES;
```

**💡 Alternative if `mysql` command still not found:**
```bash
# For Amazon Linux 2023, try mariadb-connector-c which includes mysql client
sudo dnf install mariadb105 -y

# Or install from MySQL official repository
sudo dnf install https://dev.mysql.com/get/mysql80-community-release-el9-1.noarch.rpm -y
sudo dnf install mysql-community-client -y
```

---

### **Phase 4: Start Services with Production Profile**

**⚠️ CRITICAL: Service Startup Order**

Services MUST be started in this exact order due to dependencies:

1. **Eureka Server** → Service Discovery (all other services register here)
2. **API Gateway** → Registers with Eureka, validates with Auth Service  
3. **Entity Service** → Runs Flyway migrations (database schema)
4. **Auth Service** → Registers with Eureka, depends on Entity Service schema
5. **Other Backend Services** → Register with Eureka, depend on Entity Service schema

**Start in order:**

#### **Step 1: Start Eureka Server (MUST BE FIRST) 🔥**

**Why First?** All other services need to register with Eureka for service discovery. Without Eureka running, other services will fail to start or won't be discoverable.

```bash
ssh -i uber-platform-key.pem ec2-user@<eureka-public-ip>

# Load environment variables (created using templates from lines 544-676)
source ~/aws-env.sh

# Start Eureka Server (logs to CloudWatch-monitored directory)
nohup java -jar \
  -Dspring.profiles.active=prod \
  -Deureka.instance.prefer-ip-address=true \
  -Deureka.instance.ip-address=$(curl -s http://169.254.169.254/latest/meta-data/local-ipv4) \
  -Deureka.instance.hostname=$(curl -s http://169.254.169.254/latest/meta-data/local-ipv4) \
  Uber-Service-Discovery-0.0.1-SNAPSHOT.jar > /var/log/uber-services/eureka.log 2>&1 &

# CRITICAL: Wait for Eureka to fully start before proceeding
echo "⏳ Waiting for Eureka Server to start (30 seconds)..."
sleep 30

# Verify Eureka is running and ready
curl http://localhost:8761/actuator/health
echo "✅ Eureka health check completed"

# Check Eureka dashboard is accessible
curl -s http://localhost:8761/ | grep -q "Eureka" && echo "✅ Eureka dashboard accessible" || echo "❌ Eureka dashboard not accessible"

# Check logs for successful startup
tail -f /var/log/uber-services/eureka.log
# Should see: "Started UberServiceDiscoveryApplication"
# Press Ctrl+C to exit log viewing
```

**🎯 Success Criteria:**
- Health check returns `{"status":"UP"}`
- Eureka dashboard accessible at `http://<eureka-ip>:8761`
- Logs show "Started UberServiceDiscoveryApplication"

#### **Step 2: Start API Gateway (Depends on Eureka) 🚪**

**Dependency:** Gateway registers with Eureka and discovers backend services through it.

```bash
ssh -i uber-platform-key.pem ec2-user@<gateway-public-ip>

# Load environment variables (created using templates from lines 544-676)
source ~/aws-env.sh

# Verify Eureka is reachable before starting Gateway
echo "🔍 Testing Eureka connectivity..."
timeout 5 bash -c "</dev/tcp/$EUREKA_PRIVATE_IP/8761" && echo "✅ Eureka reachable" || echo "❌ Eureka not reachable - check Eureka server"

# Start API Gateway (logs to CloudWatch-monitored directory)
nohup java -jar \
  -Dspring.profiles.active=prod \
  -Deureka.instance.prefer-ip-address=true \
  -Deureka.instance.ip-address=$(curl -s http://169.254.169.254/latest/meta-data/local-ipv4) \
  -Deureka.instance.hostname=$(curl -s http://169.254.169.254/latest/meta-data/local-ipv4) \
  Uber-API-Gateway-0.0.1-SNAPSHOT.jar > /var/log/uber-services/gateway.log 2>&1 &

# Wait for Gateway to start and register with Eureka
echo "⏳ Waiting for Gateway to start and register (20 seconds)..."
sleep 20

# Verify Gateway health
curl http://localhost:9001/actuator/health
echo "✅ Gateway health check completed"

# CRITICAL: Verify Gateway registered with Eureka
echo "🔍 Checking Eureka registration..."
curl -s http://$EUREKA_PRIVATE_IP:8761/eureka/apps/UBER-API-GATEWAY | grep -q "UBER-API-GATEWAY" && echo "✅ Gateway registered with Eureka" || echo "❌ Gateway NOT registered - check logs"

# Check Gateway logs for any errors
echo "📋 Recent Gateway logs:"
tail -10 /var/log/uber-services/gateway.log
```

**🎯 Success Criteria:**
- Gateway health check returns `{"status":"UP"}`
- Gateway appears in Eureka dashboard: `http://<eureka-ip>:8761`
- No connection errors in Gateway logs

#### **Step 3: Start Backend Services (Depends on Eureka + Entity Service) ⚙️**

**Critical Dependencies & Startup Order:** 
- **Eureka Server** must be running first (service discovery)
- **Entity Service** must complete migrations before other services  
- **Socket Service** must start before Booking Service (Retrofit dependency)
- **Location Service** must start before Booking Service (Retrofit dependency for nearby driver search)
- **Auth Service** provides authentication for all services
- **Review Service** is independent
- All services register with Eureka
- All services depend on Entity Service for database schema (Flyway migrations)

```bash
ssh -i uber-platform-key.pem -J ec2-user@<gateway-ip> ec2-user@<backend-private-ip>

# Load environment variables (created using templates from lines 544-676)
source ~/aws-env.sh

# Verify Eureka connectivity from private subnet
echo "🔍 Testing Eureka connectivity from private subnet..."
timeout 5 bash -c "</dev/tcp/$EUREKA_PRIVATE_IP/8761" && echo "✅ Eureka reachable" || echo "❌ Eureka not reachable"

# STEP 3A: Run Entity Service (Database Schema & Flyway Migrations)
echo "🗄️ Running Entity Service (Database Schema & Migrations)..."

# Entity Service is NOT a web server - it just runs Flyway migrations and exits
java -jar \
  -Dspring.profiles.active=prod \
  uber-entity-service-0.0.1-SNAPSHOT.jar

# Check if migrations completed successfully
echo "📋 Checking if Flyway migrations completed successfully:"
if [ $? -eq 0 ]; then
    echo "✅ Entity Service completed successfully (Flyway migrations applied)"
else
    echo "❌ Entity Service failed - check database connectivity and credentials"
    echo "💡 Common issues: RDS not accessible, wrong credentials, network connectivity"
fi

# Verify database tables were created
echo "🔍 Verifying database schema was created..."
mysql -h $RDS_ENDPOINT -u $RDS_USERNAME -p$RDS_PASSWORD -e "USE Uber_Db_Prod; SHOW TABLES;" 2>/dev/null && echo "✅ Database tables created" || echo "❌ Database verification failed"

# STEP 3B: Start Auth Service (Depends on Entity Service schema)
echo "🔐 Starting Auth Service (JWT Authentication)..."
nohup java -jar \
  -Dspring.profiles.active=prod \
  -Deureka.instance.prefer-ip-address=true \
  -Deureka.instance.ip-address=$(curl -s http://169.254.169.254/latest/meta-data/local-ipv4) \
  -Deureka.instance.hostname=$(curl -s http://169.254.169.254/latest/meta-data/local-ipv4) \
  UberAuth-Service-0.0.1-SNAPSHOT.jar > /var/log/uber-services/auth.log 2>&1 &

echo "⏳ Auth Service starting... waiting 15s"
sleep 15

# Verify Auth Service started successfully
# Check Auth Service (try actuator, fall back to process check)
if curl -s --max-time 3 http://localhost:9090/actuator/health | grep -q "UP" 2>/dev/null; then
    echo "✅ Auth Service is UP"
elif ps aux | grep -q "[U]berAuth-Service"; then
    echo "⏳ Auth Service is running (no actuator endpoint)"
else
    echo "❌ Auth Service failed to start"
fi

# STEP 3C: Start Socket Service FIRST (Booking Service depends on it)
echo "🔌 Starting Socket Service (WebSocket + Kafka Producer)..."
nohup java -jar \
  -Dspring.profiles.active=prod \
  -Deureka.instance.prefer-ip-address=true \
  -Deureka.instance.ip-address=$(curl -s http://169.254.169.254/latest/meta-data/local-ipv4) \
  -Deureka.instance.hostname=$(curl -s http://169.254.169.254/latest/meta-data/local-ipv4) \
  Uber-Client-Socket-Service-0.0.1-SNAPSHOT.jar > /var/log/uber-services/socket.log 2>&1 &

echo "⏳ Socket Service starting... waiting 20s"
sleep 20
# Check Socket Service (try actuator, fall back to process check)
if curl -s --max-time 3 http://localhost:8080/actuator/health | grep -q "UP" 2>/dev/null; then
    echo "✅ Socket Service is UP"
elif ps aux | grep -q "[U]ber-Client-Socket-Service"; then
    echo "⏳ Socket Service is running (no actuator endpoint)"
else
    echo "❌ Socket Service failed to start"
fi

# STEP 3D: Start Location Service FIRST (Booking Service depends on it)
echo "📍 Starting Location Service (Redis Geospatial)..."
nohup java -jar \
  -Dspring.profiles.active=prod \
  -Dserver.address=0.0.0.0 \
  -Deureka.instance.prefer-ip-address=true \
  -Deureka.instance.ip-address=$(curl -s http://169.254.169.254/latest/meta-data/local-ipv4) \
  -Deureka.instance.hostname=$(curl -s http://169.254.169.254/latest/meta-data/local-ipv4) \
  -Dspring.data.redis.host=$REDIS_ENDPOINT \
  -Dspring.data.redis.port=6379 \
  Uber-Location-Service-0.0.1-SNAPSHOT.jar > /var/log/uber-services/location.log 2>&1 &

echo "⏳ Location Service starting... waiting 30s for Eureka registration"
sleep 30
# Check Location Service (try actuator, fall back to process check)
if curl -s --max-time 3 http://localhost:7477/actuator/health | grep -q "UP" 2>/dev/null; then
    echo "✅ Location Service is UP"
elif ps aux | grep -q "[U]ber-Location-Service"; then
    echo "⏳ Location Service is running (no actuator endpoint)"
else
    echo "❌ Location Service failed to start"
fi

# Verify Location Service registered with Eureka before starting Booking Service
echo "🔍 Verifying Location Service registered with Eureka..."
curl -s http://$EUREKA_PRIVATE_IP:8761/eureka/apps | grep -i "UBER-LOCATION-SERVICE" && echo "✅ Location Service registered" || echo "❌ Location Service not registered yet"

# STEP 3E: Start Booking Service (depends on Location Service for Retrofit communication)
echo "📋 Starting Booking Service (MySQL + Kafka Consumer + Location Service Client)..."
nohup java -jar \
  -Dspring.profiles.active=prod \
  -Deureka.instance.prefer-ip-address=true \
  -Deureka.instance.ip-address=$(curl -s http://169.254.169.254/latest/meta-data/local-ipv4) \
  -Deureka.instance.hostname=$(curl -s http://169.254.169.254/latest/meta-data/local-ipv4) \
  Uber-Booking-Service-0.0.1-SNAPSHOT.jar > /var/log/uber-services/booking.log 2>&1 &

echo "⏳ Booking Service starting... waiting 20s"
sleep 20
# Check Booking Service (try actuator, fall back to process check)
if curl -s --max-time 3 http://localhost:7475/actuator/health | grep -q "UP" 2>/dev/null; then
    echo "✅ Booking Service is UP"
elif ps aux | grep -q "[U]ber-Booking-Service"; then
    echo "⏳ Booking Service is running (no actuator endpoint)"
else
    echo "❌ Booking Service failed to start"
fi

# STEP 3F: Start Review Service (independent - needs RDS)
echo "⭐ Starting Review Service (MySQL CRUD)..."
nohup java -jar \
  -Dspring.profiles.active=prod \
  -Deureka.instance.prefer-ip-address=true \
  -Deureka.instance.ip-address=$(curl -s http://169.254.169.254/latest/meta-data/local-ipv4) \
  -Deureka.instance.hostname=$(curl -s http://169.254.169.254/latest/meta-data/local-ipv4) \
  uber-review-service-0.0.1-SNAPSHOT.jar > /var/log/uber-services/review.log 2>&1 &

echo "⏳ Review Service starting... waiting 15s"
sleep 15
# Check Review Service (try actuator, fall back to process check)
if curl -s --max-time 3 http://localhost:7272/actuator/health | grep -q "UP" 2>/dev/null; then
    echo "✅ Review Service is UP"
elif ps aux | grep -q "[u]ber-review-service"; then
    echo "⏳ Review Service is running (no actuator endpoint)"
else
    echo "❌ Review Service failed to start"
fi

# STEP 3G: Final Verification
echo ""
echo "🏥 FINAL SERVICE STATUS CHECK:"
echo "================================"

# Check all Java processes
echo "📋 Running Java processes:"
ps aux | grep java | grep -v grep | awk '{print "  PID " $2 ": " $11}'

echo ""
echo "🔍 Service Registration Summary (via Eureka Server):"
echo "  ✅ entity (migrations) - COMPLETED (not a web service)"

# Check service registration with Eureka (more reliable than actuator endpoints)
eureka_apps=$(curl -s http://$EUREKA_PRIVATE_IP:8761/eureka/apps 2>/dev/null || echo "")

for service_name in "UBER-AUTH-SERVICE" "UBER-SOCKET-SERVICE" "UBER-BOOKING-SERVICE" "UBER-LOCATION-SERVICE" "UBER-REVIEW-SERVICE"; do
    if echo "$eureka_apps" | grep -q "$service_name"; then
        echo "  ✅ $(echo $service_name | tr '[:upper:]' '[:lower:]' | sed 's/uber-//g' | sed 's/-service//g') - REGISTERED with Eureka"
    else
        echo "  ❌ $(echo $service_name | tr '[:upper:]' '[:lower:]' | sed 's/uber-//g' | sed 's/-service//g') - NOT REGISTERED (may still be starting)"
    fi
done

echo ""
echo "📋 Full Eureka Registry:"
curl -s http://$EUREKA_PRIVATE_IP:8761/eureka/apps | grep -o '<name>[^<]*</name>' | sed 's/<name>//g' | sed 's/<\/name>//g' | sort | uniq || echo "❌ Could not connect to Eureka server"

echo ""
echo "🎯 Next Step: Verify all services registered with Eureka (Step 4)"
```

#### **Verify All Services Health:**
```bash
# From backend instance (Entity Service not included - it's not a web server)
for port in 9090 7477 7475 8080 7272; do
  echo "Checking port $port:"
  curl -s http://localhost:$port/actuator/health | jq
done

# Note: Entity Service (port 7476) is not checked as it's a migration utility, not a web service
```

#### **Step 4: Verify All Services are Registered:**
```bash
# Check Eureka dashboard (from your browser)
open http://<eureka-public-ip>:8761

# Or via API
curl http://<eureka-public-ip>:8761/eureka/apps | grep "<app>"

# Should see 6 services registered (7 including Eureka):
# - UBER-API-GATEWAY (1 instance)
# - UBER-AUTH-SERVICE (1 instance)
# - UBER-BOOKING-SERVICE (1 instance)
# - UBER-LOCATION-SERVICE (1 instance)
# - UBER-SOCKET-SERVICE (1 instance)
# - UBER-REVIEW-SERVICE (1 instance)
```

---

## 🚗 Driver WebSocket Client Deployment

### **Phase 5: Deploy Driver WebSocket Client**

The Driver WebSocket Client runs on a dedicated EC2 instance in the public subnet, providing a real-time interface for drivers to receive and respond to ride requests. It integrates with the API Gateway for authentication and WebSocket communication.

#### **Step 1: Prepare Client Files**

```bash
# From your local machine, prepare the client files
cd "Microservices-Based Ride-Hailing Platform/Uber-Driver-WebSocket-Client"

# Update API Gateway URL in index.html for AWS deployment
# Replace GATEWAY_PUBLIC_IP with your actual Gateway public IP
sed -i.bak "s/GATEWAY_PUBLIC_IP/$GATEWAY_PUBLIC_IP/g" index.html

# Or manually edit index.html and update this line:
# const API_GATEWAY_URL = window.location.hostname === 'localhost' 
#     ? "http://localhost:9001" 
#     : "http://YOUR_GATEWAY_PUBLIC_IP:9001";
```

#### **Step 2: Deploy to Client Instance (Dedicated Instance)**

```bash
# Upload client files to Client instance (public subnet)
scp -i uber-platform-key.pem -r \
  Uber-Driver-WebSocket-Client/ \
  ec2-user@$CLIENT_PUBLIC_IP:~/

# SSH to Client instance
ssh -i uber-platform-key.pem ec2-user@$CLIENT_PUBLIC_IP

# Install Python (if not already installed) for HTTP server
sudo dnf update -y
sudo dnf install python3 -y

# Start the client server
cd ~/Uber-Driver-WebSocket-Client
chmod +x start-client.sh
./start-client.sh
```

#### **Step 3: Access the Driver Client**

```bash
# Open in your browser (Client instance has its own public IP)
open http://$CLIENT_PUBLIC_IP:3000/index.html

# Or use the public IP directly
echo "🚗 Driver Client URL: http://$CLIENT_PUBLIC_IP:3000/index.html"
```

#### **Step 5: Test Driver Authentication Flow**

**Login Credentials** (create these during testing):
- **Email**: `your-driver@example.com`
- **Password**: `your-secure-password`

**Test Flow**:
1. **Login**: Enter credentials and click "Login"
2. **Go Online**: Click "Go Online" to connect WebSocket
3. **Receive Requests**: Create bookings via API to test notifications
4. **Accept/Reject**: Use buttons to respond to ride requests

---

## ✅ Testing Your Deployment

### **Verify All Services Are Running:**

```bash
# Check Eureka dashboard
open http://<eureka-public-ip>:8761

# Should show all 6 services registered:
# - UBER-API-GATEWAY
# - UBER-AUTH-SERVICE  
# - UBER-BOOKING-SERVICE
# - UBER-LOCATION-SERVICE
# - UBER-SOCKET-SERVICE
# - UBER-REVIEW-SERVICE
```

### **Test Authentication Flow:**

```bash
# Test user registration
curl -X POST http://<gateway-public-ip>:9001/api/v1/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test-passenger@example.com",
    "password": "TestPass123",
    "name": "Test User",
    "phoneNumber": "+1234567890",
    "role": "PASSENGER"
  }'

# Test sign in
curl -v -c /tmp/cookies.txt \
  -X POST http://<gateway-public-ip>:9001/api/v1/auth/signin \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test-passenger@example.com",
    "password": "TestPass123"
  }'

# Test protected endpoint
curl -b /tmp/cookies.txt \
  -X POST http://<gateway-public-ip>:9001/api/v1/auth/validate \
  -H "Content-Type: application/json" \
  -d '{"role": "PASSENGER"}'
```

### **Test Location Service:**

```bash
# Add driver location
curl -b /tmp/cookies.txt \
  -X POST http://<gateway-public-ip>:9001/api/v1/location/drivers \
  -H "Content-Type: application/json" \
  -d '{"driverId": 1, "latitude": 37.7749, "longitude": -122.4194}'

# Search nearby drivers
curl -b /tmp/cookies.txt \
  -X POST http://<gateway-public-ip>:9001/api/v1/location/nearby/drivers \
  -H "Content-Type: application/json" \
  -d '{"latitude": 37.7749, "longitude": -122.4194}'
```

### **Test WebSocket Client:**

```bash
# Access driver client (now on dedicated instance)
open http://$CLIENT_PUBLIC_IP:3000

# Login with test driver credentials you created
# Test WebSocket connection by going online
```

---

## 🧹 Complete AWS Resource Cleanup Guide

### **⚠️ CRITICAL: Delete Resources Immediately After Demo**

**Estimated Monthly Cost if Left Running**: $150-300/month
**Time to Complete Cleanup**: 15-20 minutes

---

### **Phase 1: Stop All Services (5 mins)**

#### **Step 1: Stop Application Services**
```bash
# SSH to backend instance and stop all services
ssh -i uber-platform-key.pem -J ec2-user@<gateway-ip> ec2-user@<backend-private-ip>

# Kill all Java processes
pkill -f java
pkill -f "uber.*jar"
pkill -f "Uber.*jar"

# Verify all stopped
ps aux | grep java
```

#### **Step 2: Stop Client Server**
```bash
# SSH to Client instance
ssh -i uber-platform-key.pem ec2-user@$CLIENT_PUBLIC_IP

# Stop client server
pkill -f "python.*http.server"
pkill -f "SimpleHTTPServer"

# SSH to Gateway instance and stop Gateway
ssh -i uber-platform-key.pem ec2-user@<gateway-public-ip>
pkill -f java

# SSH to Eureka instance and stop Eureka
ssh -i uber-platform-key.pem ec2-user@<eureka-public-ip>
pkill -f java
```

---

### **Phase 2: Delete AWS Resources (15 mins)**

#### **⚠️ CRITICAL: Delete in This Exact Order**

Resources have dependencies - deleting in wrong order will cause failures.

#### **Step 1: Load Infrastructure Summary**
```bash
# Load infrastructure summary if available
if [ -f infrastructure-summary.txt ]; then
    source infrastructure-summary.txt
else
    export PROJECT_NAME="uber-platform"  # Default project name
fi
```

#### **Step 2: EC2 Instances**
```bash
# Use specific instance IDs if available, otherwise search by tags
if [ ! -z "$GATEWAY_INSTANCE" ] && [ ! -z "$EUREKA_INSTANCE" ] && [ ! -z "$CLIENT_INSTANCE" ] && [ ! -z "$BACKEND_INSTANCE" ]; then
    INSTANCE_IDS="$GATEWAY_INSTANCE $EUREKA_INSTANCE $CLIENT_INSTANCE $BACKEND_INSTANCE"
else
    INSTANCE_IDS=$(aws ec2 describe-instances \
      --filters "Name=tag:Name,Values=*${PROJECT_NAME}*" "Name=instance-state-name,Values=running,stopped" \
      --query 'Reservations[].Instances[].InstanceId' --output text)
fi

if [ ! -z "$INSTANCE_IDS" ]; then
    aws ec2 terminate-instances --instance-ids $INSTANCE_IDS
    aws ec2 wait instance-terminated --instance-ids $INSTANCE_IDS
fi
```

#### **Step 3: MSK Kafka Cluster**
```bash
# Use specific MSK ARN if available, otherwise search by name
if [ ! -z "$MSK_ARN" ]; then
    aws kafka delete-cluster --cluster-arn $MSK_ARN
else
    # Search for MSK clusters by project name
    MSK_ARN=$(aws kafka list-clusters \
      --query "ClusterInfoList[?contains(ClusterName, \`${PROJECT_NAME}\`)].ClusterArn" --output text)
    
    if [ ! -z "$MSK_ARN" ]; then
        aws kafka delete-cluster --cluster-arn $MSK_ARN
    fi
fi
# Note: MSK deletion takes 10-15 minutes
```

#### **Step 4: ElastiCache Redis**
```bash
# Use specific Redis cluster ID if available, otherwise search by project name
if [ ! -z "$REDIS_CLUSTER_ID" ]; then
    aws elasticache delete-cache-cluster \
      --cache-cluster-id $REDIS_CLUSTER_ID \
      --no-final-snapshot
else
    # Search for Redis clusters by project name
    CACHE_CLUSTERS=$(aws elasticache describe-cache-clusters \
      --query "CacheClusters[?contains(CacheClusterId, \`${PROJECT_NAME}\`)].CacheClusterId" --output text)
    
    for CLUSTER_ID in $CACHE_CLUSTERS; do
        if [ ! -z "$CLUSTER_ID" ]; then
            aws elasticache delete-cache-cluster \
              --cache-cluster-id $CLUSTER_ID \
              --no-final-snapshot
        fi
    done
fi

# Delete subnet groups
SUBNET_GROUPS=$(aws elasticache describe-cache-subnet-groups \
  --query "CacheSubnetGroups[?contains(CacheSubnetGroupName, \`${PROJECT_NAME}\`)].CacheSubnetGroupName" --output text)

for SG_NAME in $SUBNET_GROUPS; do
    if [ ! -z "$SG_NAME" ]; then
        aws elasticache delete-cache-subnet-group --cache-subnet-group-name $SG_NAME
    fi
done
```

#### **Step 5: RDS Database**
```bash
# Use specific RDS instance ID if available, otherwise search by project name
if [ ! -z "$RDS_INSTANCE_ID" ]; then
    aws rds delete-db-instance \
      --db-instance-identifier $RDS_INSTANCE_ID \
      --skip-final-snapshot \
      --delete-automated-backups
else
    # Search for RDS instances by project name
    RDS_INSTANCES=$(aws rds describe-db-instances \
      --query "DBInstances[?contains(DBInstanceIdentifier, \`${PROJECT_NAME}\`)].DBInstanceIdentifier" --output text)
    
    for DB_ID in $RDS_INSTANCES; do
        if [ ! -z "$DB_ID" ]; then
            aws rds delete-db-instance \
              --db-instance-identifier $DB_ID \
              --skip-final-snapshot \
              --delete-automated-backups
        fi
    done
fi

# Delete DB subnet groups
DB_SUBNET_GROUPS=$(aws rds describe-db-subnet-groups \
  --query "DBSubnetGroups[?contains(DBSubnetGroupName, \`${PROJECT_NAME}\`)].DBSubnetGroupName" --output text)

for SG_NAME in $DB_SUBNET_GROUPS; do
    if [ ! -z "$SG_NAME" ]; then
        aws rds delete-db-subnet-group --db-subnet-group-name $SG_NAME
    fi
done
```

#### **Step 6: NAT Gateway & Elastic IPs**
```bash
# Get NAT Gateways
NAT_GATEWAYS=$(aws ec2 describe-nat-gateways \
  --filter "Name=tag:Name,Values=*uber*" "Name=state,Values=available" \
  --query 'NatGateways[].NatGatewayId' --output text)

for NAT_ID in $NAT_GATEWAYS; do
    if [ ! -z "$NAT_ID" ]; then
        aws ec2 delete-nat-gateway --nat-gateway-id $NAT_ID
    fi
done

# Wait for NAT Gateway deletion before releasing EIPs
if [ ! -z "$NAT_GATEWAYS" ]; then
    sleep 60
fi

# Release Elastic IPs
EIP_ALLOCS=$(aws ec2 describe-addresses \
  --filters "Name=tag:Name,Values=*uber*" \
  --query 'Addresses[].AllocationId' --output text)

for ALLOC_ID in $EIP_ALLOCS; do
    if [ ! -z "$ALLOC_ID" ]; then
        aws ec2 release-address --allocation-id $ALLOC_ID
    fi
done
```

#### **Step 7: Internet Gateway & Route Tables**
```bash
# Get VPC ID
VPC_ID=$(aws ec2 describe-vpcs \
  --filters "Name=tag:Name,Values=*uber*" \
  --query 'Vpcs[0].VpcId' --output text)

if [ "$VPC_ID" != "None" ] && [ ! -z "$VPC_ID" ]; then
    # Detach and delete Internet Gateway
    IGW_ID=$(aws ec2 describe-internet-gateways \
      --filters "Name=attachment.vpc-id,Values=$VPC_ID" \
      --query 'InternetGateways[0].InternetGatewayId' --output text)
    
    if [ "$IGW_ID" != "None" ] && [ ! -z "$IGW_ID" ]; then
        aws ec2 detach-internet-gateway --internet-gateway-id $IGW_ID --vpc-id $VPC_ID
        aws ec2 delete-internet-gateway --internet-gateway-id $IGW_ID
    fi
    
    # Delete custom route tables (keep main route table)
    ROUTE_TABLES=$(aws ec2 describe-route-tables \
      --filters "Name=vpc-id,Values=$VPC_ID" \
      --query 'RouteTables[?Associations[0].Main!=`true`].RouteTableId' --output text)
    
    for RT_ID in $ROUTE_TABLES; do
        if [ ! -z "$RT_ID" ]; then
            aws ec2 delete-route-table --route-table-id $RT_ID
        fi
    done
fi
```

#### **Step 8: Subnets & Security Groups**
```bash
if [ "$VPC_ID" != "None" ] && [ ! -z "$VPC_ID" ]; then
    # Delete subnets
    SUBNETS=$(aws ec2 describe-subnets \
      --filters "Name=vpc-id,Values=$VPC_ID" \
      --query 'Subnets[].SubnetId' --output text)
    
    for SUBNET_ID in $SUBNETS; do
        if [ ! -z "$SUBNET_ID" ]; then
            aws ec2 delete-subnet --subnet-id $SUBNET_ID
        fi
    done
    
    # Delete custom security groups (keep default)
    SECURITY_GROUPS=$(aws ec2 describe-security-groups \
      --filters "Name=vpc-id,Values=$VPC_ID" \
      --query 'SecurityGroups[?GroupName!=`default`].GroupId' --output text)
    
    for SG_ID in $SECURITY_GROUPS; do
        if [ ! -z "$SG_ID" ]; then
            aws ec2 delete-security-group --group-id $SG_ID
        fi
    done
fi
```

#### **Step 9: VPC & Key Pair**
```bash
# Delete VPC
if [ "$VPC_ID" != "None" ] && [ ! -z "$VPC_ID" ]; then
    aws ec2 delete-vpc --vpc-id $VPC_ID
fi

# Delete IAM Resources
aws iam remove-role-from-instance-profile \
  --instance-profile-name "${PROJECT_NAME}-ec2-profile" \
  --role-name "${PROJECT_NAME}-ec2-role" 2>/dev/null || true

aws iam delete-instance-profile \
  --instance-profile-name "${PROJECT_NAME}-ec2-profile" 2>/dev/null || true

# Get policy ARN and detach
POLICY_ARN=$(aws iam list-policies \
  --query "Policies[?PolicyName=='${PROJECT_NAME}-ec2-policy'].Arn" --output text 2>/dev/null)

if [ ! -z "$POLICY_ARN" ] && [ "$POLICY_ARN" != "None" ]; then
    aws iam detach-role-policy \
      --role-name "${PROJECT_NAME}-ec2-role" \
      --policy-arn $POLICY_ARN 2>/dev/null || true
    aws iam delete-policy --policy-arn $POLICY_ARN 2>/dev/null || true
fi

aws iam delete-role --role-name "${PROJECT_NAME}-ec2-role" 2>/dev/null || true

# Delete Key Pair
if [ ! -z "$KEY_NAME" ]; then
    aws ec2 delete-key-pair --key-name $KEY_NAME
    rm -f ${KEY_NAME}.pem
else
    aws ec2 delete-key-pair --key-name "${PROJECT_NAME}-key" 2>/dev/null || true
    rm -f ${PROJECT_NAME}-key.pem 2>/dev/null || true
fi
```

#### **Step 10: CloudWatch Log Groups**
```bash
# Delete CloudWatch log groups
LOG_GROUPS=$(aws logs describe-log-groups \
  --log-group-name-prefix "/aws/uber" \
  --query 'logGroups[].logGroupName' --output text)

for LOG_GROUP in $LOG_GROUPS; do
    if [ ! -z "$LOG_GROUP" ]; then
        aws logs delete-log-group --log-group-name "$LOG_GROUP"
    fi
done
```

---

### **Phase 3: Verification & Cost Check (2 mins)**

#### **Step 1: Verify All Resources Deleted**
```bash
# Verification Report
INSTANCES=$(aws ec2 describe-instances \
  --filters "Name=instance-state-name,Values=running,stopped" \
  --query 'Reservations[].Instances[].InstanceId' --output text | wc -w)

RDS_COUNT=$(aws rds describe-db-instances \
  --query 'DBInstances[].DBInstanceIdentifier' --output text | wc -w)

CACHE_COUNT=$(aws elasticache describe-cache-clusters \
  --query 'CacheClusters[].CacheClusterId' --output text | wc -w)

MSK_COUNT=$(aws kafka list-clusters \
  --query 'ClusterInfoList[].ClusterName' --output text | wc -w)

NAT_COUNT=$(aws ec2 describe-nat-gateways \
  --filter "Name=state,Values=available" \
  --query 'NatGateways[].NatGatewayId' --output text | wc -w)

# Summary
if [ $INSTANCES -eq 0 ] && [ $RDS_COUNT -eq 0 ] && [ $CACHE_COUNT -eq 0 ] && [ $NAT_COUNT -eq 0 ]; then
    echo "✅ ALL RESOURCES DELETED SUCCESSFULLY!"
else
    echo "⚠️ Some resources may still exist - check AWS Console"
    echo "EC2: $INSTANCES, RDS: $RDS_COUNT, Cache: $CACHE_COUNT, NAT: $NAT_COUNT"
fi
```

---

## 🎯 Final Checklist

**Your deployment is successful when:**

- ✅ All 7 services registered in Eureka
- ✅ JWT authentication working (Auth Service)
- ✅ Booking creation flows through 3 services with logs visible
- ✅ Redis GEORADIUS queries working (Location Service)
- ✅ Kafka events flowing (Socket → Booking)
- ✅ WebSocket connections working (Driver clients)
- ✅ Reviews CRUD working (Review Service)
- ✅ Backend services NOT accessible from internet (private subnet) ✅
- ✅ CloudWatch logs showing for all services

---

**You've built something 99.99% of developers never will. Now go showcase it!** 🚀