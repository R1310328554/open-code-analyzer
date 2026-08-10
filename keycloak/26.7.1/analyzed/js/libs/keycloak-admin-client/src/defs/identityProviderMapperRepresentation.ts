/**
 * 身份提供者映射器实例：将 IdP 断言中的属性同步到本地用户/角色。
 * https://www.keycloak.org/docs-api/11.0/rest-api/index.html#_identityprovidermapperrepresentation
 */

export default interface IdentityProviderMapperRepresentation {
  /** 映射器配置项（键值对，结构由 mapper 类型决定） */
  config?: any;
  /** 映射器实例 ID */
  id?: string;
  /** 所属身份提供者别名 */
  identityProviderAlias?: string;
  /** 映射器 Provider 工厂 ID */
  identityProviderMapper?: string;
  /** 映射器显示名称 */
  name?: string;
}
