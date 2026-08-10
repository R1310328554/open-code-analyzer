// config.ts — i18next 初始化、懒加载语言包与切换/持久化逻辑。

import { LanguageAbbreviation } from '@/constants/common';
import storage from '@/utils/authorization-util';
import dayjs from 'dayjs';
import i18n from 'i18next';
import LanguageDetector from 'i18next-browser-languagedetector';
import { upperFirst } from 'lodash';
import { initReactI18next } from 'react-i18next';
import translation_en from './en';

// 界面语言取自客户端 localStorage；库内语言用于服务端 Agent 模板资源。
// 跨设备登录时，登录页语言由 VITE_DEFAULT_LANGUAGE_CODE 决定。

/** 各语言代码 → 动态 import 工厂，用于按需加载 translation 包。 */
const languageImports: Record<string, () => Promise<{ default: any }>> = {
  [LanguageAbbreviation.En]: () => import('./en'),
  [LanguageAbbreviation.Zh]: () => import('./zh'),
  [LanguageAbbreviation.ZhTraditional]: () => import('./zh-traditional'),
  [LanguageAbbreviation.Id]: () => import('./id'),
  [LanguageAbbreviation.Ja]: () => import('./ja'),
  [LanguageAbbreviation.Es]: () => import('./es'),
  [LanguageAbbreviation.Vi]: () => import('./vi'),
  [LanguageAbbreviation.Ru]: () => import('./ru'),
  [LanguageAbbreviation.PtBr]: () => import('./pt-br'),
  [LanguageAbbreviation.De]: () => import('./de'),
  [LanguageAbbreviation.Fr]: () => import('./fr'),
  [LanguageAbbreviation.It]: () => import('./it'),
  [LanguageAbbreviation.Bg]: () => import('./bg'),
  [LanguageAbbreviation.Ar]: () => import('./ar'),
  [LanguageAbbreviation.Tr]: () => import('./tr'),
  [LanguageAbbreviation.Ko]: () => import('./ko'),
};

const supportedLanguageCodes: Intl.UnicodeBCP47LocaleIdentifier[] =
  Object.keys(languageImports);

/** 支持语言列表：code、Intl.Locale 与本地化 displayName。 */
export const supportedLanguages = supportedLanguageCodes.map((code) => {
  const locale = new Intl.Locale(code);

  return {
    code,
    locale,
    displayName: upperFirst(
      new Intl.DisplayNames(locale, { type: 'language' }).of(code)!,
    ),
  };
});

/** 默认语言：环境变量 VITE_DEFAULT_LANGUAGE_CODE 或英语。 */
export const DEFAULT_LANGUAGE_CODE =
  import.meta.env.VITE_DEFAULT_LANGUAGE_CODE || LanguageAbbreviation.En;

const resources = {
  [LanguageAbbreviation.En]: translation_en,
};

/** 同步 html lang/dir 与 dayjs 区域设置。 */
const updateDocumentLocale = (lng: string) => {
  document.documentElement.lang = lng;
  document.documentElement.dir = 'ltr';
  dayjs.locale(lng === 'zh' ? 'zh-cn' : lng);
};

i18n
  .use(initReactI18next)
  .use(LanguageDetector)
  .init({
    detection: {
      lookupLocalStorage: 'lng',
      order: ['localStorage'],
      caches: [],
    },
    supportedLngs: supportedLanguageCodes,
    resources,
    fallbackLng: DEFAULT_LANGUAGE_CODE,
    interpolation: {
      escapeValue: false,
    },
  });

/** 懒加载指定语言包并注册到 i18n translation 命名空间。 */
export const loadLanguageAsync = async (lng: string): Promise<void> => {
  const normalizedLng = lng;

  if (i18n.hasResourceBundle(normalizedLng, 'translation')) {
    return;
  }

  const importFn = languageImports[normalizedLng];
  if (!importFn) {
    console.warn(`Language ${lng} is not supported for lazy loading`);
    return;
  }

  try {
    const module = await importFn();
    const translationData = module.default?.translation || module.default;
    i18n.addResourceBundle(normalizedLng, 'translation', translationData);
  } catch (error) {
    console.error(`Failed to load language ${lng}:`, error);
  }
};

/** 切换语言：必要时先 load，写入 storage 并更新 document/dayjs。 */
export const changeLanguageAsync = async (lng: string): Promise<void> => {
  const normalizedLng = lng;

  if (
    normalizedLng !== LanguageAbbreviation.En &&
    !i18n.hasResourceBundle(normalizedLng, 'translation')
  ) {
    await loadLanguageAsync(normalizedLng);
  }

  storage.setLanguage(lng);

  updateDocumentLocale(lng);

  await i18n.changeLanguage(normalizedLng);
};

/** 应用启动：从 storage 或默认语言初始化 i18n。 */
export const initLanguage = async (): Promise<void> => {
  const currentLng = storage.getLanguage() || DEFAULT_LANGUAGE_CODE;

  await changeLanguageAsync(currentLng);
};

export default i18n;
