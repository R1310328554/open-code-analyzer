/**
 * LDAP 连接测试请求/响应表示：用于在保存 User Federation 组件前验证 LDAP 连通性与绑定凭据。
 * https://www.keycloak.org/docs-api/11.0/rest-api/#_testldapconnectionrepresentation
 */

export default interface TestLdapConnectionRepresentation {
  /** 测试动作类型（如 testConnection、testAuthentication） */
  action?: string;
  /** LDAP 服务器连接 URL（ldap:// 或 ldaps://） */
  connectionUrl?: string;
  /** 绑定 DN（服务账号或管理员 DN） */
  bindDn?: string;
  /** 绑定密码或凭证 */
  bindCredential?: string;
  /** 是否使用 Truststore SPI（always、never 等） */
  useTruststoreSpi?: string;
  /** 连接超时（毫秒，字符串形式） */
  connectionTimeout?: string;
  /** 关联的 User Storage 组件 ID（更新已有组件时） */
  componentId?: string;
  /** 是否启用 StartTLS（true/false 字符串） */
  startTls?: string;
  /** 认证类型（simple、none 等） */
  authType?: string;
}
