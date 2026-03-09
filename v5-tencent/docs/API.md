# RocketMQ Dashboard API Documentation

## Base URL

```
http://localhost:8080/api
```

## Response Format

All API responses follow this standard format:

```json
{
  "code": 200,
  "message": "Success message",
  "data": { ... },
  "timestamp": 1769761144093
}
```

### Status Codes

| Code | Description |
|------|-------------|
| 200  | Success |
| 400  | Bad Request - Invalid parameters or validation error |
| 401  | Unauthorized - Missing or invalid credentials |
| 404  | Not Found - Resource does not exist |
| 500  | Internal Server Error |

---

## Health & Config APIs

### GET /health

Check application health status.

**Response:**
```json
{
  "status": "UP",
  "application": "RocketMQ Dashboard",
  "version": "1.0.0"
}
```

### GET /v1/config/credentials

Get Tencent Cloud credentials configuration.

**Response:**
```json
{
  "code": 200,
  "data": {
    "secretId": "AKIDxxxx",
    "secretKey": "****",
    "configured": true
  }
}
```

### POST /v1/config/credentials

Update Tencent Cloud credentials.

**Request Body:**
```json
{
  "secretId": "AKID...",
  "secretKey": "..."
}
```

**Response:**
```json
{
  "code": 200,
  "message": "Credentials updated successfully"
}
```

### GET /v1/config/regions

Get available Tencent Cloud regions.

**Response:**
```json
{
  "code": 200,
  "data": [
    {
      "regionId": "ap-guangzhou",
      "regionName": "Guangzhou"
    },
    {
      "regionId": "ap-shanghai",
      "regionName": "Shanghai"
    }
  ]
}
```

---

## Cluster Management APIs

### GET /v1/clusters

List all RocketMQ clusters.

**Query Parameters:**
- `page` (optional): Page number, default 1
- `pageSize` (optional): Page size, default 20

**Response:**
```json
{
  "code": 200,
  "data": {
    "items": [
      {
        "clusterId": "rmq-cn-xxxxx",
        "clusterName": "production-cluster",
        "region": "ap-guangzhou",
        "status": "running",
        "createTime": "2024-01-01T00:00:00Z"
      }
    ],
    "total": 1,
    "page": 1,
    "pageSize": 20
  }
}
```

### GET /v1/clusters/{clusterId}

Get cluster details.

**Path Parameters:**
- `clusterId`: Cluster ID

**Response:**
```json
{
  "code": 200,
  "data": {
    "clusterId": "rmq-cn-xxxxx",
    "clusterName": "production-cluster",
    "region": "ap-guangzhou",
    "status": "running",
    "vpcId": "vpc-xxxxx",
    "subnetId": "subnet-xxxxx",
    "createTime": "2024-01-01T00:00:00Z",
    "maxTps": 5000,
    "maxRetention": 72
  }
}
```

### POST /v1/clusters

Create a new cluster.

**Request Body:**
```json
{
  "name": "my-cluster",
  "region": "ap-guangzhou",
  "remark": "Production cluster",
  "vpcId": "vpc-xxxxx",
  "subnetId": "subnet-xxxxx",
  "maxTps": 5000,
  "maxRetention": 72
}
```

**Response:**
```json
{
  "code": 200,
  "message": "Cluster created successfully",
  "data": {
    "clusterId": "rmq-cn-xxxxx"
  }
}
```

### PUT /v1/clusters/{clusterId}

Update cluster configuration.

**Path Parameters:**
- `clusterId`: Cluster ID

**Request Body:**
```json
{
  "name": "updated-cluster-name",
  "remark": "Updated description"
}
```

**Response:**
```json
{
  "code": 200,
  "message": "Cluster updated successfully"
}
```

### DELETE /v1/clusters/{clusterId}

Delete a cluster.

**Path Parameters:**
- `clusterId`: Cluster ID

**Response:**
```json
{
  "code": 200,
  "message": "Cluster deleted successfully"
}
```

---

## Topic Management APIs

### GET /v1/topics

List topics in a cluster.

**Query Parameters:**
- `clusterId` (required): Cluster ID
- `page` (optional): Page number, default 1
- `pageSize` (optional): Page size, default 20

**Response:**
```json
{
  "code": 200,
  "data": {
    "items": [
      {
        "topicName": "my-topic",
        "topicType": "Normal",
        "queueNum": 8,
        "remark": "Test topic",
        "createTime": "2024-01-01T00:00:00Z"
      }
    ],
    "total": 1
  }
}
```

### GET /v1/topics/{topicName}

Get topic details.

**Query Parameters:**
- `clusterId` (required): Cluster ID

**Path Parameters:**
- `topicName`: Topic name

**Response:**
```json
{
  "code": 200,
  "data": {
    "topicName": "my-topic",
    "topicType": "Normal",
    "queueNum": 8,
    "remark": "Test topic",
    "createTime": "2024-01-01T00:00:00Z",
    "messageRetention": 72
  }
}
```

### POST /v1/topics

Create a topic.

**Request Body:**
```json
{
  "clusterId": "rmq-cn-xxxxx",
  "topicName": "my-topic",
  "topicType": "Normal",
  "queueNum": 8,
  "remark": "My topic"
}
```

**Response:**
```json
{
  "code": 200,
  "message": "Topic created successfully"
}
```

### PUT /v1/topics/{topicName}

Update topic configuration.

**Query Parameters:**
- `clusterId` (required): Cluster ID

**Path Parameters:**
- `topicName`: Topic name

**Request Body:**
```json
{
  "queueNum": 16,
  "remark": "Updated topic"
}
```

**Response:**
```json
{
  "code": 200,
  "message": "Topic updated successfully"
}
```

### DELETE /v1/topics/{topicName}

Delete a topic.

**Query Parameters:**
- `clusterId` (required): Cluster ID

**Path Parameters:**
- `topicName`: Topic name

**Response:**
```json
{
  "code": 200,
  "message": "Topic deleted successfully"
}
```

---

## Consumer Group APIs

### GET /v1/groups

List consumer groups in a cluster.

**Query Parameters:**
- `clusterId` (required): Cluster ID
- `page` (optional): Page number, default 1
- `pageSize` (optional): Page size, default 20

**Response:**
```json
{
  "code": 200,
  "data": {
    "items": [
      {
        "groupName": "my-consumer-group",
        "consumeType": "Pull",
        "retryMaxTimes": 16,
        "consumeEnable": true,
        "createTime": "2024-01-01T00:00:00Z"
      }
    ],
    "total": 1
  }
}
```

### POST /v1/groups

Create a consumer group.

**Request Body:**
```json
{
  "clusterId": "rmq-cn-xxxxx",
  "groupName": "my-consumer-group",
  "consumeType": "Pull",
  "retryMaxTimes": 16,
  "remark": "My consumer group"
}
```

**Response:**
```json
{
  "code": 200,
  "message": "Consumer group created successfully"
}
```

### DELETE /v1/groups/{groupName}

Delete a consumer group.

**Query Parameters:**
- `clusterId` (required): Cluster ID

**Path Parameters:**
- `groupName`: Group name

**Response:**
```json
{
  "code": 200,
  "message": "Consumer group deleted successfully"
}
```

---

## Message APIs

### GET /v1/messages

Query messages by topic.

**Query Parameters:**
- `clusterId` (required): Cluster ID
- `topicName` (required): Topic name
- `startTime` (optional): Start timestamp
- `endTime` (optional): End timestamp
- `page` (optional): Page number, default 1
- `pageSize` (optional): Page size, default 20

**Response:**
```json
{
  "code": 200,
  "data": {
    "items": [
      {
        "messageId": "01000000000000000000000000000001",
        "topic": "my-topic",
        "tags": "tag1",
        "keys": "key1",
        "body": "message body",
        "bornTime": "2024-01-01T00:00:00Z",
        "storeTime": "2024-01-01T00:00:01Z"
      }
    ],
    "total": 1
  }
}
```

### GET /v1/messages/{messageId}

Get message by ID.

**Query Parameters:**
- `clusterId` (required): Cluster ID
- `topicName` (required): Topic name

**Path Parameters:**
- `messageId`: Message ID

**Response:**
```json
{
  "code": 200,
  "data": {
    "messageId": "01000000000000000000000000000001",
    "topic": "my-topic",
    "tags": "tag1",
    "keys": "key1",
    "body": "message body",
    "properties": {
      "UNIQ_KEY": "...",
      "CLUSTER": "..."
    },
    "bornTime": "2024-01-01T00:00:00Z",
    "storeTime": "2024-01-01T00:00:01Z"
  }
}
```

### POST /v1/messages/send

Send a message.

**Request Body:**
```json
{
  "clusterId": "rmq-cn-xxxxx",
  "topicName": "my-topic",
  "tags": "tag1",
  "keys": "key1",
  "body": "message content"
}
```

**Response:**
```json
{
  "code": 200,
  "message": "Message sent successfully",
  "data": {
    "messageId": "01000000000000000000000000000001"
  }
}
```

### GET /v1/messages/{messageId}/trace

Get message trace.

**Query Parameters:**
- `clusterId` (required): Cluster ID
- `topicName` (required): Topic name

**Path Parameters:**
- `messageId`: Message ID

**Response:**
```json
{
  "code": 200,
  "data": {
    "messageId": "01000000000000000000000000000001",
    "traces": [
      {
        "stage": "produce",
        "timestamp": "2024-01-01T00:00:00Z",
        "broker": "broker-1",
        "success": true
      },
      {
        "stage": "consume",
        "timestamp": "2024-01-01T00:00:01Z",
        "consumerGroup": "my-group",
        "success": true
      }
    ]
  }
}
```

---

## Dashboard APIs

### GET /v1/dashboard/overview

Get dashboard overview data.

**Query Parameters:**
- `clusterId` (optional): Filter by cluster ID

**Response:**
```json
{
  "code": 200,
  "data": {
    "totalClusters": 5,
    "runningClusters": 4,
    "totalTopics": 100,
    "totalGroups": 50,
    "totalMessages": 1000000,
    "todayMessages": 50000
  }
}
```

### GET /v1/dashboard/trends

Get message trends.

**Query Parameters:**
- `clusterId` (required): Cluster ID
- `period` (optional): Time period (1h, 24h, 7d, 30d), default 24h

**Response:**
```json
{
  "code": 200,
  "data": {
    "period": "24h",
    "dataPoints": [
      {
        "timestamp": "2024-01-01T00:00:00Z",
        "produceCount": 1000,
        "consumeCount": 950
      }
    ]
  }
}
```

### GET /v1/dashboard/lag-groups

Get consumer groups with message lag.

**Query Parameters:**
- `clusterId` (required): Cluster ID

**Response:**
```json
{
  "code": 200,
  "data": {
    "items": [
      {
        "groupName": "my-group",
        "topicName": "my-topic",
        "totalLag": 1000,
        "consumerCount": 5
      }
    ]
  }
}
```

---

## Role Management APIs

### GET /v1/roles

List roles.

**Query Parameters:**
- `clusterId` (required): Cluster ID
- `page` (optional): Page number, default 1
- `pageSize` (optional): Page size, default 20

**Response:**
```json
{
  "code": 200,
  "data": {
    "items": [
      {
        "roleName": "admin",
        "description": "Administrator role",
        "permissions": ["read", "write", "admin"],
        "enabled": true,
        "createTime": "2024-01-01T00:00:00Z"
      }
    ],
    "total": 1
  }
}
```

### POST /v1/roles

Create a role.

**Request Body:**
```json
{
  "clusterId": "rmq-cn-xxxxx",
  "roleName": "admin",
  "description": "Administrator role",
  "permissions": ["read", "write", "admin"],
  "ipWhitelist": ["192.168.1.0/24"],
  "enabled": true
}
```

**Response:**
```json
{
  "code": 200,
  "message": "Role created successfully"
}
```

### PUT /v1/roles/{roleName}

Update a role.

**Query Parameters:**
- `clusterId` (required): Cluster ID

**Path Parameters:**
- `roleName`: Role name

**Request Body:**
```json
{
  "description": "Updated description",
  "permissions": ["read", "write"],
  "enabled": false
}
```

**Response:**
```json
{
  "code": 200,
  "message": "Role updated successfully"
}
```

### DELETE /v1/roles/{roleName}

Delete a role.

**Query Parameters:**
- `clusterId` (required): Cluster ID

**Path Parameters:**
- `roleName`: Role name

**Response:**
```json
{
  "code": 200,
  "message": "Role deleted successfully"
}
```

---

## Error Handling

All errors follow this format:

```json
{
  "code": 400,
  "message": "Error description",
  "data": null,
  "timestamp": 1769761144093
}
```

### Common Error Codes

| Code | Message | Solution |
|------|---------|----------|
| 400 | Missing required parameter: clusterId | Provide clusterId parameter |
| 400 | Validation error: invalid topic name | Use valid topic name format |
| 401 | Credentials not configured | Configure credentials via /v1/config/credentials |
| 404 | Cluster not found | Check cluster ID is correct |
| 500 | Tencent Cloud API Error | Check credentials and permissions |

---

## Authentication

Currently, the API uses configuration-based authentication with Tencent Cloud SecretId/SecretKey stored in the application configuration.

Future versions may add:
- JWT-based authentication
- Role-based access control (RBAC)
- API key authentication

---

## Rate Limiting

No rate limiting is currently enforced. This may be added in future versions.

---

## Examples

### Using curl

```bash
# Get health status
curl http://localhost:8080/api/health

# List clusters
curl http://localhost:8080/api/v1/clusters

# Create a topic
curl -X POST http://localhost:8080/api/v1/topics \
  -H "Content-Type: application/json" \
  -d '{
    "clusterId": "rmq-cn-xxxxx",
    "topicName": "my-topic",
    "queueNum": 8
  }'
```

### Using JavaScript (fetch)

```javascript
// Get clusters
const response = await fetch('http://localhost:8080/api/v1/clusters');
const result = await response.json();
console.log(result.data.items);

// Create topic
const response = await fetch('http://localhost:8080/api/v1/topics', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    clusterId: 'rmq-cn-xxxxx',
    topicName: 'my-topic',
    queueNum: 8
  })
});
```

### Using Python (requests)

```python
import requests

# Get clusters
response = requests.get('http://localhost:8080/api/v1/clusters')
clusters = response.json()['data']['items']

# Create topic
response = requests.post('http://localhost:8080/api/v1/topics', json={
    'clusterId': 'rmq-cn-xxxxx',
    'topicName': 'my-topic',
    'queueNum': 8
})
```
