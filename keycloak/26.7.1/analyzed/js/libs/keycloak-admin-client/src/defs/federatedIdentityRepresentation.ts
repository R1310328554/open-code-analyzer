/**
 * 联邦身份关联：本地用户与外部 IdP 账户之间的绑定关系。
 * https://www.keycloak.org/docs-api/11.0/rest-api/index.html#_federatedidentityrepresentation
 */

export default interface FederatedIdentityRepresentation {
  /** 身份提供者别名（IdentityProviderRepresentation.alias） */
  identityProvider?: string;
  /** 外部 IdP 侧的用户唯一标识 */
  userId?: string;
  /** 外部 IdP 侧的用户名/登录名 */
  userName?: string;
}
