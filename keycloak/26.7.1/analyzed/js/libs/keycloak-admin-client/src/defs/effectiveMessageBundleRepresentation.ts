/**
 * 有效消息条目：合并主题与 Realm 覆盖后，某 locale 下实际生效的 i18n 键值对。
 */
export default interface EffectiveMessageBundleRepresentation {
  /** 消息键（如 loginTitle、error.invalidUser） */
  key: string;
  /** 解析后的最终文案 */
  value: string;
  /** 文案来源：THEME 表示主题包，REALM 表示 Realm 级自定义覆盖 */
  source: "THEME" | "REALM";
}
