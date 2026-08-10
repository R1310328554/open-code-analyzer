/**
 * 身份提供者（IdP）配置：用于社交登录、SAML/OIDC Broker 等联邦认证场景。
 * https://www.keycloak.org/docs-api/11.0/rest-api/index.html#_identityproviderrepresentation
 */

/** IdP 在令牌交换、客户端断言等场景中的用途分类 */
export enum IdentityProviderType {
  /** 任意用途（未限定） */
  ANY = "ANY",
  /** 终端用户认证（社交/企业 SSO 登录） */
  USER_AUTHENTICATION = "USER_AUTHENTICATION",
  /** 客户端断言验证（JWT client authentication） */
  CLIENT_ASSERTION = "CLIENT_ASSERTION",
  /** 信任材料来源（如 JWKS、证书） */
  TRUST_MATERIAL = "TRUST_MATERIAL",
  /** 外部令牌交换（Token Exchange 目标 IdP） */
  EXCHANGE_EXTERNAL_TOKEN = "EXCHANGE_EXTERNAL_TOKEN",
  /** JWT 授权许可（JWT Authorization Grant） */
  JWT_AUTHORIZATION_GRANT = "JWT_AUTHORIZATION_GRANT",
}

export default interface IdentityProviderRepresentation {
  /** 首次通过 Broker 创建用户时，是否将 IdP access token 中的角色写入本地用户 */
  addReadTokenRoleOnCreate?: boolean;
  /** IdP 唯一别名（登录按钮与联邦关联引用） */
  alias?: string;
  /** Provider 特定配置项（端点、Client ID/Secret 等） */
  config?: Record<string, any>;
  /** 登录页显示名称 */
  displayName?: string;
  /** 是否启用该 IdP */
  enabled?: boolean;
  /** 首次 Broker 登录认证流别名 */
  firstBrokerLoginFlowAlias?: string;
  /** IdP 内部 UUID */
  internalId?: string;
  /** 仅允许账户关联，不允许直接登录 */
  linkOnly?: boolean;
  /** 在登录页隐藏 IdP 按钮 */
  hideOnLogin?: boolean;
  /** Broker 登录后执行的认证流别名 */
  postBrokerLoginFlowAlias?: string;
  /** Provider 工厂 ID（如 oidc、google、saml） */
  providerId?: string;
  /** 是否持久化 IdP 颁发的令牌 */
  storeToken?: boolean;
  /** 是否信任 IdP 提供的邮箱（跳过本地验证） */
  trustEmail?: boolean;
  /** 关联的组织 ID（Organizations 特性） */
  organizationId?: string;
  /** IdP 支持的用途类型列表 */
  types?: string[];
}
