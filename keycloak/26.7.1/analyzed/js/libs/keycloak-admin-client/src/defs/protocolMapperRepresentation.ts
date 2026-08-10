/**
 * 协议映射器（Protocol Mapper）表示：将用户/会话属性写入 Token 或 SAML 断言。
 * https://www.keycloak.org/docs-api/11.0/rest-api/index.html#_protocolmapperrepresentation
 */

export default interface ProtocolMapperRepresentation {
  /** 映射器配置项（键值对，因协议/映射器类型而异） */
  config?: Record<string, any>;
  /** 映射器 UUID */
  id?: string;
  /** 映射器显示名称 */
  name?: string;
  /** 所属协议（如 openid-connect、saml） */
  protocol?: string;
  /** 映射器实现 ID（内置或 SPI 扩展） */
  protocolMapper?: string;
}
