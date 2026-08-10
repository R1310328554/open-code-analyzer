import type { ConfigPropertyRepresentation } from "./configPropertyRepresentation.js";

/**
 * 组件类型元数据：描述某 SPI Provider 工厂可配置的属性 schema。
 * https://www.keycloak.org/docs-api/11.0/rest-api/index.html#_componenttyperepresentation
 */
export default interface ComponentTypeRepresentation {
  /** Provider 工厂 ID（与 ComponentRepresentation.providerId 对应） */
  id: string;
  /** 组件类型说明文本，用于管理控制台展示 */
  helpText: string;
  /** Realm 级组件可配置属性列表 */
  properties: ConfigPropertyRepresentation[];
  /** Client 级组件可配置属性列表 */
  clientProperties: ConfigPropertyRepresentation[];
  /** 扩展元数据键值对（如 UI 提示、能力标记等） */
  metadata: { [index: string]: any };
}
