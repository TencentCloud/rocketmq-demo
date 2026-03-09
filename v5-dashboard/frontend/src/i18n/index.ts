import { createI18n } from 'vue-i18n'
import zh from './locales/zh'
import en from './locales/en'

// 从 localStorage 获取保存的语言设置，默认为中文
const getDefaultLocale = () => {
  const savedLocale = localStorage.getItem('locale')
  if (savedLocale && ['zh', 'en'].includes(savedLocale)) {
    return savedLocale
  }
  // 尝试从浏览器语言获取
  const browserLang = navigator.language.toLowerCase()
  if (browserLang.startsWith('zh')) {
    return 'zh'
  }
  return 'en'
}

const i18n = createI18n({
  legacy: false, // 使用 Composition API 模式
  locale: getDefaultLocale(),
  fallbackLocale: 'en',
  messages: {
    zh,
    en
  }
})

export default i18n
