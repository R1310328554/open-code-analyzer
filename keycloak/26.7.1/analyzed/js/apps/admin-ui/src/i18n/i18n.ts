/**
 * Admin UI 国际化（i18n）实例配置。
 * 通过 i18next + react-i18next 从服务端按 realm/语言加载消息包，
 * 开发模式下可回退到构建时内联的 message-bundle。
 */
import { createInstance } from "i18next";
import type { i18n as i18nType } from "i18next";
import FetchBackend from "i18next-fetch-backend";
import { initReactI18next } from "react-i18next";

import { environment } from "../environment";
import { joinPath } from "../utils/joinPath";
import { DEFAULT_LOCALE } from "./constants";

// @ts-ignore imported by rollup plugin
import code from "message-bundle";

/** 服务端消息 JSON 中单条键值对的结构。 */
type KeyValue = { key: string; value: string };

/** i18next 翻译键的层级分隔符（如 `common.save`）。 */
export const KEY_SEPARATOR = ".";

/** 全局 i18n 单例：命名空间与当前 realm 对齐，便于按领域隔离文案。 */
export const i18n: i18nType = createInstance({
  fallbackLng: DEFAULT_LOCALE,
  keySeparator: KEY_SEPARATOR,
  nsSeparator: false,
  interpolation: {
    // React 已对输出做 XSS 转义，此处关闭 i18next 二次转义
    escapeValue: false,
  },
  defaultNS: [environment.realm],
  ns: [environment.realm],
  backend: {
    // 从 Admin 静态资源路径按 ns（realm）与 lng 拉取消息
    loadPath: joinPath(
      environment.adminBaseUrl,
      `resources/{{ns}}/admin/{{lng}}`,
    ),
    parse: (data: string) => {
      // 本地开发且无 realm 覆盖时，直接使用打包内联的 message-bundle
      if (
        process.env.NODE_ENV === "development" &&
        import.meta.env.VITE_REALM_OVERRIDES === undefined
      ) {
        return code;
      }
      const messages: KeyValue[] = JSON.parse(data);
      return Object.fromEntries(messages.map(({ key, value }) => [key, value]));
    },
  },
});

i18n.use(FetchBackend);
i18n.use(initReactI18next);
