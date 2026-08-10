/**
 * 组件导出结构：Realm 导入/导出时序列化 SPI 组件（如 User Storage、LDAP 等）的层级快照。
 * https://www.keycloak.org/docs-api/11.0/rest-api/index.html#_componentexportrepresentation
 */

export default interface ComponentExportRepresentation {
  /** 组件实例 ID */
  id?: string;
  /** 组件显示名称 */
  name?: string;
  /** Provider 工厂 ID（如 ldap、kerberos 等） */
  providerId?: string;
  /** 组件子类型（Provider 内部细分） */
  subType?: string;
  /** 嵌套子组件，键为 providerType，值为子组件数组 */
  subComponents?: { [index: string]: ComponentExportRepresentation };
  /** 组件配置键值对 */
  config?: { [index: string]: string };
}
