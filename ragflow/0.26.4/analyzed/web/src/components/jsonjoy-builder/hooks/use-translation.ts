// use-translation.ts — jsonjoy-builder 国际化：Context 读取与模板占位符替换。

import { useContext } from 'react';
import { en } from '../i18n/locales/en';
import { TranslationContext } from '../i18n/translation-context';

/** 从 TranslationContext 取当前语言包，缺省回退 en。 */
export function useTranslation() {
  const translation = useContext(TranslationContext);
  return translation ?? en;
}

/** 将 {key} 占位符替换为 values 中对应字符串/数字。 */
export function formatTranslation(
  template: string,
  values: Record<string, string | number>,
) {
  return template.replace(/\{(\w+)\}/g, (_, key) => {
    const value = values[key];
    return value !== undefined ? String(value) : `{${key}}`;
  });
}
