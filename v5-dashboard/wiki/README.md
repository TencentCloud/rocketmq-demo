# Tencent RocketMQ Dashboard — Wiki

腾讯云 RocketMQ 可视化管理控制台，基于 Vue 3 + Spring Boot 3 构建。

代码位于本仓库 [`v5-dashboard/`](../) 目录。

---

## 界面预览

**仪表盘首页**

![仪表盘首页](images/SCR-20260309-lswv.png)

**主题管理**

![主题管理](images/SCR-20260309-magl.png)

**消费者组详情**

![消费者组详情](images/SCR-20260309-lvty.png)

**消息详情**

![消息详情](images/SCR-20260309-makm.png)

---

## 目录

- [快速开始](#快速开始)
- [功能](#功能)
- [技术栈](#技术栈)
- [项目结构](#项目结构)
- [配置参数](#配置参数)
- [Docker 部署](#docker-部署)
- [文档](#文档)

---

## 快速开始

### 环境要求

- Java 17+
- Maven 3.6+
- Node.js 16+（仅开发时需要）

### 配置腾讯云凭证

三种方式任选其一：

**方式一：application.yml（适合本地开发）**

编辑 `backend/rocketmq-dashboard-api/src/main/resources/application.yml`：

```yaml
tencent:
  cloud:
    secret-id: AKIDxxxxxxxx
    secret-key: xxxxxxxxxxxxxxxx
    region: ap-guangzhou
```

**方式二：环境变量（推荐生产环境）**

```bash
export TENCENT_CLOUD_SECRET_ID=AKIDxxxxxxxx
export TENCENT_CLOUD_SECRET_KEY=xxxxxxxxxxxxxxxx
export TENCENT_CLOUD_REGION=ap-guangzhou
```

**方式三：JVM -D 参数**

```bash
java -Dtencent.cloud.secret-id=AKIDxxxxxxxx \
     -Dtencent.cloud.secret-key=xxxxxxxxxxxxxxxx \
     -Dtencent.cloud.region=ap-guangzhou \
     -jar rocketmq-dashboard-api-1.0.0-SNAPSHOT.jar
```

### 一键构建并运行

```bash
# 在 v5-tencent/ 目录下执行
chmod +x scripts/package.sh
./scripts/package.sh

# 运行
java -jar backend/rocketmq-dashboard-api/target/rocketmq-dashboard-api-1.0.0-SNAPSHOT.jar
```

访问 http://localhost:8080

### 开发模式

**前端（Terminal 1）：**

```bash
cd frontend
npm install
npm run dev
# http://localhost:3000
```

**后端（Terminal 2）：**

```bash
cd backend
mvn spring-boot:run -pl rocketmq-dashboard-api
# http://localhost:8080
```

---

## 功能

- **集群管理** — 查看和管理 RocketMQ 集群
- **Topic 管理** — 创建、修改、删除 Topic
- **消费组管理** — 管理消费组及消费进度
- **消息查询** — 按条件查询和追踪消息
- **角色管理** — 权限和角色配置
- **Dashboard** — 集群概览和实时监控

---

## 技术栈

| 层级 | 技术 |
|------|------|
| 前端 | Vue 3 + TypeScript + TDesign + Vite |
| 后端 | Spring Boot 3 + Java 17 + Maven |
| SDK  | 腾讯云 RocketMQ SDK |

---

## 项目结构

```
v5-tencent/
├── frontend/                  # Vue 3 前端
│   └── src/
│       ├── views/             # 页面组件
│       ├── components/        # 公共组件
│       ├── api/               # API 请求
│       └── router/            # 路由配置
├── backend/                   # Spring Boot 后端
│   └── rocketmq-dashboard-api/
│       └── src/main/
│           ├── java/          # Java 源码
│           └── resources/
│               ├── application.yml
│               └── static/    # 前端构建产物
├── scripts/                   # 构建部署脚本
├── docs/                      # API / 部署 / 测试文档
└── wiki/                      # 本 Wiki
```

---

## 配置参数

| 参数 | 环境变量 | 默认值 | 说明 |
|------|---------|--------|------|
| `tencent.cloud.secret-id` | `TENCENT_CLOUD_SECRET_ID` | — | 腾讯云 SecretId |
| `tencent.cloud.secret-key` | `TENCENT_CLOUD_SECRET_KEY` | — | 腾讯云 SecretKey |
| `tencent.cloud.region` | `TENCENT_CLOUD_REGION` | `ap-guangzhou` | 地域 |
| `tencent.cloud.endpoint` | `TENCENT_CLOUD_ENDPOINT` | `trocket.tencentcloudapi.com` | API 端点 |
| `server.port` | — | `8080` | 服务端口 |

---

## Docker 部署

```bash
docker build -t rocketmq-dashboard .
docker run -d -p 8080:8080 \
  -e TENCENT_CLOUD_SECRET_ID=AKIDxxxxxxxx \
  -e TENCENT_CLOUD_SECRET_KEY=xxxxxxxxxxxxxxxx \
  -e TENCENT_CLOUD_REGION=ap-guangzhou \
  rocketmq-dashboard
```

---

## 文档

- [API 文档](../docs/API.md)
- [部署指南](../docs/DEPLOYMENT.md)
- [测试指南](../docs/TESTING.md)
- [快速启动](../QUICKSTART.md)

---

## License

MIT
