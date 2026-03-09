# Topic 和 Group 搜索功能说明

## 功能概述

为 Topic 管理页面和 Consumer Group 管理页面添加了搜索功能，支持根据名称快速查询列表。

## 实现细节

### 1. API 接口更新

#### Topic API (`src/api/topic.ts`)

```typescript
listTopics(clusterId: string, topicName?: string): Promise<ApiResponse<TopicInfo[]>>
```

- 新增可选参数 `topicName`，用于按主题名称搜索
- 当 `topicName` 为空或 undefined 时，返回全量数据
- 当 `topicName` 有值时，后端会根据该参数过滤返回结果

#### Group API (`src/api/group.ts`)

```typescript
listGroups(clusterId: string, groupName?: string): Promise<ApiResponse<GroupInfo[]>>
```

- 新增可选参数 `groupName`，用于按消费者组名称搜索
- 当 `groupName` 为空或 undefined 时，返回全量数据
- 当 `groupName` 有值时，后端会根据该参数过滤返回结果

### 2. 页面功能

#### Topic 页面 (`src/views/topics/Index.vue`)

**新增功能：**

- 搜索输入框：位于页面顶部操作栏
- 支持按 Enter 键触发搜索
- 支持点击搜索图标触发搜索
- 支持清空按钮，点击后清空搜索条件并重新加载全量数据

**使用方法：**

1. 在搜索框中输入 Topic 名称（支持部分匹配）
2. 按 Enter 键或点击搜索图标
3. 列表会根据搜索条件刷新
4. 点击清空按钮可恢复显示全量数据

#### Group 页面 (`src/views/groups/Index.vue`)

**新增功能：**

- 搜索输入框：位于页面顶部操作栏
- 支持按 Enter 键触发搜索
- 支持点击搜索图标触发搜索
- 支持清空按钮，点击后清空搜索条件并重新加载全量数据

**使用方法：**

1. 在搜索框中输入 Consumer Group 名称（支持部分匹配）
2. 按 Enter 键或点击搜索图标
3. 列表会根据搜索条件刷新
4. 点击清空按钮可恢复显示全量数据

### 3. 国际化支持

已添加中英文翻译：

**中文：**

- Topic 搜索框：`按主题名称搜索`
- Group 搜索框：`按消费者组名称搜索`

**英文：**

- Topic 搜索框：`Search by topic name`
- Group 搜索框：`Search by group name`

## 技术实现

### 状态管理

```typescript
const searchKeyword = ref('') // 搜索关键词
```

### 搜索方法

```typescript
const handleSearch = () => {
  loadTopics() // 或 loadGroups()
}

const handleClearSearch = () => {
  searchKeyword.value = ''
  loadTopics() // 或 loadGroups()
}
```

### API 调用

```typescript
const response = await topicApi.listTopics(
  selectedClusterId.value,
  searchKeyword.value || undefined
)
```

## 后端要求

后端接口需要支持以下参数：

### Topic 接口

```
GET /v1/topics?clusterId={clusterId}&topicName={topicName}
```

- `clusterId`: 必填，集群ID
- `topicName`: 可选，主题名称（支持模糊匹配）

### Group 接口

```
GET /v1/groups?clusterId={clusterId}&groupName={groupName}
```

- `clusterId`: 必填，集群ID
- `groupName`: 可选，消费者组名称（支持模糊匹配）

## 用户体验优化

1. **实时搜索**：输入后按 Enter 或点击图标即可搜索
2. **清空功能**：一键清空搜索条件并恢复全量数据
3. **视觉反馈**：搜索图标可点击，提供良好的交互反馈
4. **国际化**：完整支持中英文切换
5. **响应式**：搜索框宽度适配不同屏幕尺寸

## 注意事项

1. 搜索功能依赖后端接口支持，请确保后端已实现相应的过滤逻辑
2. 搜索是基于后端过滤，不是前端过滤，可以处理大量数据
3. 搜索条件会随着集群切换而保持，如需清空请点击清空按钮
4. 搜索支持部分匹配，具体匹配规则由后端决定
