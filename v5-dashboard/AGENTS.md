# v5-dashboard AI 开发约束文档

本文档为 AI 辅助编码工具（Cursor / GitHub Copilot / OpenCode 等）提供项目上下文和规范约束。

---

## 项目概述

腾讯云 RocketMQ 5.x 可视化管理控制台。前端 Vue 3 + 后端 Spring Boot 3，后端封装腾讯云 `trocket` SDK。前端构建产物嵌入后端 JAR，单 Artifact 部署。

## 目录结构

```
v5-dashboard/
├── frontend/                          # Vue 3 前端
│   └── src/
│       ├── api/                       # API 请求层（axios 封装）
│       │   ├── request.ts             # axios 实例、拦截器、重试
│       │   ├── types.ts               # 所有 TS 接口定义
│       │   ├── config.ts              # configApi
│       │   ├── cluster.ts             # clusterApi
│       │   ├── topic.ts               # topicApi
│       │   ├── group.ts               # groupApi
│       │   ├── message.ts             # messageApi
│       │   ├── role.ts                # roleApi
│       │   ├── dashboard.ts           # dashboardApi
│       │   └── index.ts              # barrel export
│       ├── components/
│       │   ├── common/                # 通用组件 (SearchBar, PageHeader, EmptyState, LoadingOverlay, LanguageSwitcher)
│       │   └── layout/                # 布局组件 (Sidebar, Header)
│       ├── composables/               # 组合函数 (useLocale)
│       ├── i18n/                      # 国际化 (zh/en)
│       ├── layouts/                   # 页面布局 (MainLayout)
│       ├── router/                    # 路由配置（扁平路由，无嵌套）
│       ├── stores/                    # Pinia 状态管理
│       ├── utils/                     # 工具函数 (format, validators)
│       └── views/                     # 页面组件
│           ├── Dashboard.vue
│           ├── clusters/ (Index.vue, Detail.vue)
│           ├── topics/   (Index.vue, Detail.vue)
│           ├── groups/   (Index.vue, Detail.vue)
│           ├── messages/ (Index.vue, Detail.vue)
│           └── roles/    (Index.vue)
├── backend/                           # Spring Boot 3 后端
│   └── rocketmq-dashboard-api/
│       └── src/main/java/com/rocketmq/dashboard/
│           ├── DashboardApplication.java
│           ├── common/                # Result, ResponseCode
│           ├── config/                # TencentCloudConfig, ConfigManager, WebConfig, OpenApiConfig
│           ├── controller/            # REST controllers (/api/v1/*)
│           ├── service/               # 业务逻辑（封装 SDK 调用）
│           ├── dto/
│           │   ├── request/           # 请求 DTO
│           │   └── response/          # 响应 DTO
│           ├── exception/             # BusinessException, GlobalExceptionHandler
│           └── util/                  # ResponseUtil
└── scripts/
    └── package.sh                     # 一键构建脚本
```

---

## 技术栈 & 版本

| 项 | 版本 |
|---|---|
| Java | 17 |
| Spring Boot | 3.2.1 |
| Maven | 3.6+ |
| 腾讯云 SDK | `tencentcloud-sdk-java-trocket:3.1.1292` |
| Node.js | 16+ |
| Vue | 3.5 |
| TypeScript | 5.9 |
| Vite | 7.2 |
| Pinia | 3.0 |
| vue-router | 5.0 |
| vue-i18n | 11.2 |
| UI 库 | TDesign Vue Next 1.18 |
| HTTP 客户端 | axios 1.13 |
| 日期 | dayjs |

---

## 前端规范

### 代码风格

- **不加分号**（semi: false）
- **单引号**（singleQuote: true）
- **行宽 100**（printWidth: 100）
- **不加尾逗号**（trailingComma: none）
- **箭头函数省略括号**（arrowParens: avoid）
- **缩进 2 空格**
- Prettier + ESLint 统一格式

### 命名约定

| 类别 | 规则 | 示例 |
|------|------|------|
| Vue 组件文件 | PascalCase | `PageHeader.vue`, `SearchBar.vue` |
| TS 文件 | camelCase | `request.ts`, `format.ts` |
| 列表页 | `views/{module}/Index.vue` | `views/clusters/Index.vue` |
| 详情页 | `views/{module}/Detail.vue` | `views/clusters/Detail.vue` |
| API 对象 | camelCase + `Api` 后缀 | `clusterApi`, `topicApi` |
| TS 类型 | PascalCase | `ClusterInfo`, `ApiResponse` |
| 请求类型 | `Create/Update{Entity}Request` | `CreateTopicRequest` |
| 响应类型 | `{Entity}Info` | `TopicInfo` |
| 变量/函数 | camelCase | `selectedClusterId`, `loadData` |
| 路由 path | 小写复数 + kebab-case | `/clusters`, `/clusters/:clusterId` |

### 导入路径

- 使用 `@/` 别名指向 `src/`
- 类型导入使用 `import type { ... }` 语法
- TDesign 组件类型：`import type { FormInstanceFunctions, PrimaryTableCol } from 'tdesign-vue-next'`

### 组件编写模式

所有页面组件遵循此结构：

```vue
<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { MessagePlugin } from 'tdesign-vue-next'
import type { PrimaryTableCol } from 'tdesign-vue-next'
import { useI18n } from 'vue-i18n'
import { someApi } from '@/api'
import type { SomeInfo } from '@/api/types'
import PageHeader from '@/components/common/PageHeader.vue'
import LoadingOverlay from '@/components/common/LoadingOverlay.vue'
import EmptyState from '@/components/common/EmptyState.vue'

const { t } = useI18n()
const router = useRouter()

const loading = ref(true)
const data = ref<SomeInfo[]>([])

const columns: PrimaryTableCol[] = [/* ... */]

const loadData = async () => {
  try {
    const res = await someApi.list()
    data.value = res.data
  } catch (e) {
    console.error(e)
  }
}

onMounted(async () => {
  loading.value = true
  await loadData()
  loading.value = false
})
</script>

<template>
  <div class="page-container">
    <PageHeader :title="t('xxx.pageTitle')" :description="t('xxx.pageDescription')">
      <template #actions><!-- 操作按钮 --></template>
    </PageHeader>
    <LoadingOverlay :visible="loading" />
    <t-card v-if="data.length > 0">
      <t-table :data="data" :columns="columns" row-key="id" hover />
    </t-card>
    <EmptyState v-else />
  </div>
</template>

<style scoped>
.page-container {
  padding: var(--gap-lg);
}
</style>
```

### API 层模式

每个模块一个文件，导出一个对象：

```typescript
// src/api/example.ts
import request from './request'
import type { ApiResponse, ExampleInfo } from './types'

export const exampleApi = {
  list(params?: { clusterId?: string }): Promise<ApiResponse<ExampleInfo[]>> {
    return request.get('/v1/examples', { params }).then(res => res.data)
  },
  get(id: string, params?: { clusterId?: string }): Promise<ApiResponse<ExampleInfo>> {
    return request.get(`/v1/examples/${id}`, { params }).then(res => res.data)
  },
  create(data: CreateExampleRequest): Promise<ApiResponse<ExampleInfo>> {
    return request.post('/v1/examples', data).then(res => res.data)
  }
}
```

### 状态管理

- Pinia **Composition API** 风格（`defineStore('name', () => { ... })`）
- 全局状态仅放必要项（loading, selectedClusterId）
- 页面级状态用 `ref()` 管理在组件内

### 样式方案

- **纯 CSS**（无 SCSS、无 CSS Modules）
- 使用 CSS 自定义属性（design tokens），定义在 `src/style.css` 的 `:root` 中
- 组件内用 `<style scoped>`
- 核心 tokens:
  - 颜色：`--color-primary: #0052d9`, `--color-success/warning/danger`, `--color-text-*`, `--color-bg-*`
  - 间距：`--gap-xs(4px)/sm(8px)/md(16px)/lg(24px)/xl(32px)`
  - 圆角：`--radius-sm(4px)/md(6px)/lg(8px)/xl(12px)`
  - 字体：`--font-sans`(Inter), `--font-mono`(JetBrains Mono)

### 国际化

- `vue-i18n` Composition API 模式（`legacy: false`）
- 翻译 key 结构：`{module}.{key}`，如 `cluster.pageTitle`
- 使用：`const { t } = useI18n()` → `t('cluster.pageTitle')`
- 所有用户可见文本必须走 i18n，不允许硬编码中文/英文
- 语言文件：`src/i18n/locales/zh.ts` 和 `en.ts`

### 路由

- `createWebHistory()` HTML5 history 模式
- **扁平路由**，不嵌套
- 所有页面 **懒加载**：`component: () => import('@/views/xxx.vue')`
- `meta` 包含 `title` 和 `icon`（用于侧边栏和 document.title）

### 环境变量

- `VITE_APP_TITLE` — 应用标题
- `VITE_API_BASE_URL` — API 前缀（默认 `/api`）
- `VITE_APP_ENV` — 环境标识

### 构建输出

前端 `npm run build` 产物输出到 `backend/.../resources/static/`，由后端作为静态资源提供。

---

## 后端规范

### 代码风格

- **缩进 4 空格**
- Java 标准命名：PascalCase 类名，camelCase 字段/方法
- Lombok 注解简化代码

### 包结构

| 包 | 职责 |
|---|---|
| `common` | 通用返回体 `Result<T>`、响应码枚举 |
| `config` | Spring 配置类、SDK 客户端初始化 |
| `controller` | REST 控制器，`/api/v1/{resource}` |
| `service` | 业务逻辑，封装 SDK 调用 |
| `dto.request` | 请求 DTO |
| `dto.response` | 响应 DTO |
| `exception` | 自定义异常、全局异常处理器 |
| `util` | 工具类 |

### Controller 模式

```java
@Slf4j
@RestController
@RequestMapping("/api/v1/examples")
@Tag(name = "Example Management")
public class ExampleController {

    private final ExampleService exampleService;

    // 构造器注入，不用 @Autowired
    public ExampleController(ExampleService exampleService) {
        this.exampleService = exampleService;
    }

    @GetMapping
    @Operation(summary = "List examples")
    public Result<List<ExampleInfo>> list(
            @Parameter(description = "Cluster ID") @RequestParam String clusterId) {
        try {
            return Result.success(exampleService.list(clusterId));
        } catch (Exception e) {
            log.error("Failed to list examples", e);
            throw new RuntimeException("Failed to list examples", e);
        }
    }
}
```

### DTO 模式

**请求 DTO：**
```java
@Data
@Schema(description = "Create example request")
public class CreateExampleRequest {
    @NotBlank(message = "Name is required")
    @Schema(description = "Example name", required = true)
    private String name;
}
```

**响应 DTO：**
```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Example info")
public class ExampleInfo {
    @Schema(description = "Example ID")
    private String id;
    @Schema(description = "Example name")
    private String name;
}
```

### API 响应格式

统一返回 `Result<T>`：

```json
{
  "code": 200,
  "message": "Success",
  "data": { ... },
  "timestamp": 1704038400000
}
```

- `code=200` 表示成功
- 业务异常和 SDK 异常返回 HTTP 200，错误信息在 body 中
- 参数校验异常返回 HTTP 400

### Service 层 & SDK 调用模式

- 构造器注入 `TrocketClient`（单例 Bean）
- SDK 调用：创建 Request 对象 → 设置参数 → 调用 client 方法 → 映射为 DTO
- SDK 异常 `TencentCloudSDKException` 由 `GlobalExceptionHandler` 统一处理
- DTO 转换使用 `.builder()` 模式

```java
@Slf4j
@Service
public class ExampleService {
    private final TrocketClient trocketClient;

    public ExampleService(TrocketClient trocketClient) {
        this.trocketClient = trocketClient;
    }

    public List<ExampleInfo> list(String clusterId) throws TencentCloudSDKException {
        DescribeExampleRequest req = new DescribeExampleRequest();
        req.setInstanceId(clusterId);
        DescribeExampleResponse resp = trocketClient.DescribeExample(req);
        return Arrays.stream(resp.getData())
            .map(this::mapToInfo)
            .collect(Collectors.toList());
    }

    private ExampleInfo mapToInfo(ExampleItem item) {
        return ExampleInfo.builder()
            .id(item.getId())
            .name(item.getName())
            .build();
    }
}
```

### 异常处理

| 异常类型 | HTTP 状态码 | 响应 code |
|---------|-----------|----------|
| `BusinessException` | 200 | 自定义 |
| `TencentCloudSDKException` | 200 | 1002 |
| `MethodArgumentNotValidException` | 400 | 1003 |
| `UnsupportedOperationException` | 403 | — |
| 其他 `Exception` | 500 | 500 |

### 配置参数

| 参数 | 环境变量 | 默认值 |
|-----|---------|-------|
| `tencent.cloud.secret-id` | `TENCENT_CLOUD_SECRET_ID` | — |
| `tencent.cloud.secret-key` | `TENCENT_CLOUD_SECRET_KEY` | — |
| `tencent.cloud.region` | `TENCENT_CLOUD_REGION` | `ap-guangzhou` |
| `tencent.cloud.endpoint` | `TENCENT_CLOUD_ENDPOINT` | `trocket.tencentcloudapi.com` |
| `server.port` | — | `8080` |

---

## API 端点汇总

所有端点前缀 `/api/v1/`：

| 模块 | 方法 | 路径 | 说明 |
|-----|------|------|------|
| Config | GET | `/v1/config/regions` | 地域列表 |
| Cluster | GET | `/v1/clusters` | 集群列表 |
| Cluster | GET | `/v1/clusters/:id` | 集群详情 |
| Topic | GET | `/v1/topics?clusterId=` | Topic 列表 |
| Topic | GET | `/v1/topics/:name?clusterId=` | Topic 详情 |
| Topic | POST | `/v1/topics` | 创建 Topic |
| Topic | PUT | `/v1/topics/:name` | 修改 Topic |
| Topic | DELETE | `/v1/topics/:name` | 删除 Topic |
| Group | GET | `/v1/groups?clusterId=` | 消费组列表 |
| Group | GET | `/v1/groups/:name?clusterId=` | 消费组详情 |
| Group | POST | `/v1/groups` | 创建消费组 |
| Group | PUT | `/v1/groups/:name` | 修改消费组 |
| Group | DELETE | `/v1/groups/:name` | 删除消费组 |
| Message | GET | `/v1/messages` | 消息查询 |
| Message | GET | `/v1/messages/:id` | 消息详情 |
| Message | GET | `/v1/messages/:id/trace` | 消息轨迹 |
| Role | GET | `/v1/roles?clusterId=` | 角色列表 |
| Dashboard | GET | `/v1/dashboard/overview` | 概览数据 |
| Dashboard | GET | `/v1/dashboard/trends` | 趋势数据 |
| Dashboard | GET | `/v1/dashboard/top-lag` | 堆积排行 |

---

## 当前已知限制

1. **单地域**：`TrocketClient` 启动时绑定单个 region，不支持运行时切换
2. **集群管理**：创建/修改/删除集群抛 `UnsupportedOperationException`，需通过腾讯云控制台操作
3. **部分功能 Mock**：消息发送/重发、Dashboard 概览/趋势/堆积排行使用模拟数据
4. **Topic producers**：返回空列表（SDK 不支持）

---

## 开发流程

### 本地开发

```bash
# Terminal 1 — 前端（热更新）
cd frontend && npm install && npm run dev    # http://localhost:3000

# Terminal 2 — 后端
cd backend && mvn spring-boot:run -pl rocketmq-dashboard-api   # http://localhost:8080
```

前端 dev server 通过 Vite proxy 将 `/api` 转发到 `http://localhost:8080`。

### 构建部署

```bash
./scripts/package.sh
java -jar backend/rocketmq-dashboard-api/target/rocketmq-dashboard-api-1.0.0-SNAPSHOT.jar
```

### 类型检查

```bash
cd frontend && npm run build:check   # vue-tsc + vite build
```
