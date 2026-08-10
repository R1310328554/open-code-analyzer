/**
 * Account Console 国际化（i18next）初始化与配置。
 * 从 Keycloak 服务端加载 realm 级翻译资源，并监听语言切换事件。
 */
import { LanguageDetectorModule, createInstance } from "i18next";
import FetchBackend from "i18next-fetch-backend";
import { initReactI18next } from "react-i18next";

import { environment } from "./environment";
import { joinPath } from "./utils/joinPath";

/** 无匹配翻译时的回退语言。 */
const DEFAULT_LOCALE = "en";

/** 服务端返回的键值对翻译条目。 */
type KeyValue = { key: string; value: string };

/** 从注入的 environment.locale 检测当前用户语言。 */
export const keycloakLanguageDetector: LanguageDetectorModule = {
  type: "languageDetector",

  detect() {
    return environment.locale;
  },
};

// 监听全局 languageChanged 事件，同步切换 i18next 语言
window.addEventListener("languageChanged", (event: Event) => {
  const customEvent = event as CustomEvent<{ language: string }>;
  void (async () => {
    await i18n.changeLanguage(customEvent.detail.language, (error) => {
      if (error) {
        console.warn(
          "Error(s) loading locale",
          customEvent.detail.language,
          error,
        );
      }
    });
  })();
});

/** i18next 单例：配置后端加载路径与 JSON 解析方式。 */
export const i18n = createInstance({
  fallbackLng: DEFAULT_LOCALE,
  nsSeparator: false,
  interpolation: {
    escapeValue: false,
  },
  backend: {
    // 翻译文件路径：/resources/{realm}/account/{lng}
    loadPath: joinPath(
      environment.serverBaseUrl,
      `resources/${environment.realm}/account/{{lng}}`,
    ),
    // 将 [{key, value}, ...] 数组转为 { key: value } 对象
    parse(data: string) {
      const messages: KeyValue[] = JSON.parse(data);

      return Object.fromEntries(messages.map(({ key, value }) => [key, value]));
    },
  },
});

i18n.use(FetchBackend);
i18n.use(keycloakLanguageDetector);
i18n.use(initReactI18next);
