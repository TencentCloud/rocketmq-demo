# 快速启动指南

## 启动后端服务

```bash
cd backend
mvn spring-boot:run -pl rocketmq-dashboard-api
```

服务将在 http://localhost:8080 启动

## 访问 API 文档

打开浏览器访问 Swagger UI:
```
http://localhost:8080/swagger-ui/index.html
```

## 测试 API

### 1. 配置腾讯云凭证

```bash
curl -X POST http://localhost:8080/api/v1/config/credentials \
  -H "Content-Type: application/json" \
  -d '{
    "secretId": "AKIDxxxxxxxxxxxxxxxxxxxxx",
    "secretKey": "xxxxxxxxxxxxxxxxxxxxxxxx",
    "region": "ap-guangzhou"
  }'
```

### 2. 获取凭证信息（脱敏）

```bash
curl http://localhost:8080/api/v1/config/credentials
```

### 3. 获取可用地域列表

```bash
curl http://localhost:8080/api/v1/config/regions
```

### 4. 获取集群列表

```bash
curl http://localhost:8080/api/v1/clusters
```

### 5. 获取集群详情

```bash
curl http://localhost:8080/api/v1/clusters/rmq-cn-demo001
```

### 6. 创建集群

```bash
curl -X POST http://localhost:8080/api/v1/clusters \
  -H "Content-Type: application/json" \
  -d '{
    "clusterName": "my-test-cluster",
    "description": "My test cluster",
    "region": "ap-guangzhou",
    "clusterType": "5.x",
    "maxTps": 10000,
    "maxBandwidth": 100,
    "storageCapacity": 500
  }'
```

### 7. 更新集群

```bash
curl -X PUT http://localhost:8080/api/v1/clusters/rmq-cn-demo001 \
  -H "Content-Type: application/json" \
  -d '{
    "description": "Updated description",
    "maxTps": 20000
  }'
```

### 8. 删除集群

```bash
curl -X DELETE http://localhost:8080/api/v1/clusters/rmq-cn-demo001
```

## 检查服务健康状态

```bash
curl http://localhost:8080/api/health
```

预期响应:
```json
{
  "status": "UP",
  "application": "RocketMQ Dashboard",
  "version": "1.0.0"
}
```

## 使用 Swagger UI 测试

1. 打开 http://localhost:8080/swagger-ui/index.html
2. 选择一个 Controller (例如 "Configuration Management")
3. 展开一个 endpoint (例如 "POST /api/v1/config/credentials")
4. 点击 "Try it out" 按钮
5. 填写请求参数
6. 点击 "Execute" 按钮
7. 查看响应结果

## 响应格式

所有 API 响应都使用统一的格式:

### 成功响应
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    // 返回的数据对象
  },
  "timestamp": 1704038400000
}
```

### 错误响应
```json
{
  "code": 1003,
  "message": "Validation Error: Secret ID is required",
  "data": null,
  "timestamp": 1704038400000
}
```

## 常见错误码

| Code | Message | 说明 |
|------|---------|------|
| 200 | Success | 操作成功 |
| 400 | Bad Request | 请求参数错误 |
| 404 | Not Found | 资源不存在 |
| 500 | Internal Server Error | 服务器内部错误 |
| 1001 | Configuration Error | 配置错误 |
| 1002 | Tencent Cloud API Error | 腾讯云 API 调用错误 |
| 1003 | Validation Error | 参数校验错误 |
| 1004 | Business Error | 业务逻辑错误 |

## 下一步

- 查看完整的 API 文档: `TASK3_COMPLETION_REPORT.md`
- 查看实现细节: `IMPLEMENTATION_SUMMARY.md`
- 继续实现剩余的 Controllers (Topic, Group, Message, Role, Dashboard)
