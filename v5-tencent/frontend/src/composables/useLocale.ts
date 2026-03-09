import { useI18n } from 'vue-i18n'

export function useLocale() {
  const { locale, t } = useI18n()

  const changeLocale = (lang: 'zh' | 'en') => {
    locale.value = lang
    localStorage.setItem('locale', lang)
  }

  const getCurrentLocale = () => {
    return locale.value
  }

  return {
    locale,
    t,
    changeLocale,
    getCurrentLocale
  }
}
