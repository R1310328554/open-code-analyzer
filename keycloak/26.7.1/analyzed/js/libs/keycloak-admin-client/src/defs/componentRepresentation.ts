/**
 * Realm 组件实例：SPI 扩展点的运行时配置（用户存储、密钥提供者、协议映射器等）。
 * https://www.keycloak.org/docs-api/11.0/rest-api/index.html#_componentrepresentation
 */

export default interface ComponentRepresentation {
  /** 组件实例 ID */
  id?: string;
  /** 组件显示名称 */
  name?: string;
  /** Provider 工厂 ID */
  providerId?: string;
  /** Provider 接口类型（如 org.keycloak.storage.UserStorageProvider） */
  providerType?: string;
  /** 父组件 ID（用于嵌套组件树） */
  parentId?: string;
  /** 组件子类型 */
  subType?: string;
  /** 配置项：值为字符串或字符串数组（多值属性） */
  config?: { [index: string]: string | string[] };
}
