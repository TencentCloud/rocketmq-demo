# 国际化使用指南

## 概述

本项目已集成 vue-i18n 实现中英文双语切换功能。

## 功能特性

- ✅ 支持中文和英文切换
- ✅ 语言设置自动保存到 localStorage
- ✅ 自动检测浏览器语言
- ✅ 提供语言切换按钮组件

## 文件结构

```
src/
├── i18n/
│   ├── index.ts           # i18n 配置入口
│   └── locales/
│       ├── zh.ts          # 中文语言包
│       └── en.ts          # 英文语言包
├── components/
│   └── common/
│       └── LanguageSwitcher.vue  # 语言切换组件
└── composables/
    └── useLocale.ts       # 语言管理 Composable
```

## 使用方法

### 1. 在组件中使用翻译

```vue
<template>
  <div>
    <h1>{{ t('dashboard.title') }}</h1>
    <p>{{ t('common.loading') }}</p>
  </div>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n'

const { t } = useI18n()
</script>
```

### 2. 使用 Composable

```vue
<script setup lang="ts">
import { useLocale } from '@/composables/useLocale'

const { t, locale, changeLocale } = useLocale()

// 切换语言
const switchToEnglish = () => {
  changeLocale('en')
}

const switchToChinese = () => {
  changeLocale('zh')
}
</script>
```

### 3. 添加新的翻译

在 `src/i18n/locales/zh.ts` 和 `src/i18n/locales/en.ts` 中添加对应的翻译：

```typescript
// zh.ts
export default {
  myModule: {
    title: '我的模块',
    description: '这是描述'
  }
}

// en.ts
export default {
  myModule: {
    title: 'My Module',
    description: 'This is description'
  }
}
```

## 语言切换组件

项目已在 Header 组件中集成了语言切换按钮，用户可以通过点击按钮在中英文之间切换。

## 默认语言

- 优先使用 localStorage 中保存的语言设置
- 如果没有保存的设置，则根据浏览器语言自动选择
- 如果浏览器语言不是中文，则默认使用英文

## 已翻译的模块

- ✅ 通用文本（按钮、状态等）
- ✅ 头部导航
- ✅ 侧边栏菜单
- ✅ 仪表盘
- ✅ 集群管理
- ✅ 主题管理
- ✅ 消费者管理
- ✅ 生产者管理
- ✅ 消息管理
- ✅ 消息轨迹
- ✅ 配置页面

## 注意事项

1. 所有新增的界面文本都应该使用 `t()` 函数进行翻译
2. 翻译 key 应该使用有意义的命名，便于维护
3. 保持中英文翻译文件的结构一致
4. 语言切换后会自动保存到 localStorage，刷新页面后保持选择的语言
