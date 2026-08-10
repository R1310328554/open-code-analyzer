/**
 * 基于当前管理员语言环境格式化日期的 Hook。
 * 从 WhoAmI 上下文读取 locale，使日期/时间展示与管理员界面语言一致。
 */
import { useWhoAmI } from "../context/whoami/WhoAmI";

/** 长日期 + 短时间样式的默认 Intl 格式化选项。 */
export const FORMAT_DATE_AND_TIME: Intl.DateTimeFormatOptions = {
  dateStyle: "long",
  timeStyle: "short",
};

/**
 * 返回按管理员 locale 格式化 Date 的函数。
 * 可选传入 Intl.DateTimeFormatOptions 覆盖默认样式。
 */
export default function useFormatDate() {
  const { whoAmI } = useWhoAmI();

  return function formatDate(date: Date, options?: Intl.DateTimeFormatOptions) {
    // 使用 whoAmI.locale 而非浏览器默认语言，保证与 Keycloak 管理端一致
    return date.toLocaleString(whoAmI.locale, options);
  };
}
