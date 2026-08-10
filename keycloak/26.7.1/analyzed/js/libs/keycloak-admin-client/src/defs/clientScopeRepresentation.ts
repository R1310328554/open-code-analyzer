/**
 * 客户端 Scope：定义可分配给客户端的 OIDC/SAML 作用域及其协议映射器。
 * https://www.keycloak.org/docs-api/11.0/rest-api/index.html#_clientscoperepresentation
 */
import type ProtocolMapperRepresentation from "./protocolMapperRepresentation.js";

export default interface ClientScopeRepresentation {
  /** 扩展属性键值对 */
  attributes?: Record<string, any>;
  /** Scope 描述 */
  description?: string;
  /** Scope 唯一 ID */
  id?: string;
  /** Scope 名称（如 email、profile） */
  name?: string;
  /** 关联协议（openid-connect、saml 等） */
  protocol?: string;
  /** 该 Scope 下的协议映射器列表 */
  protocolMappers?: ProtocolMapperRepresentation[];
}
