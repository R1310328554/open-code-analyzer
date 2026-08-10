/**
 * 汇总领域可用语言列表的 Hook。
 * 合并 realm 默认 locale 与 supportedLocales，去重后供语言选择器等组件使用。
 */
import { useMemo } from "react";
import { useRealm } from "../context/realm-context/RealmContext";
import { DEFAULT_LOCALE } from "../i18n/constants";

export default function useLocale() {
  const { realmRepresentation: realm } = useRealm();

  // 领域支持的语言；未配置时回退到 Admin UI 默认 locale
  const defaultSupportedLocales = useMemo(() => {
    return realm.supportedLocales?.length
      ? realm.supportedLocales
      : [DEFAULT_LOCALE];
  }, [realm]);

  // 领域显式设置的 defaultLocale（若有）
  const defaultLocales = useMemo(() => {
    return realm.defaultLocale?.length ? [realm.defaultLocale] : [];
  }, [realm]);

  // 默认 locale 排在前面，再并入支持列表并去重
  const combinedLocales = useMemo(() => {
    return Array.from(new Set([...defaultLocales, ...defaultSupportedLocales]));
  }, [defaultLocales, defaultSupportedLocales]);

  return combinedLocales;
}
