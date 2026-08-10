import type AccessTokenAccess from "./AccessTokenAccess.js";
import type AccessTokenCertConf from "./accessTokenCertConf.js";
import type AddressClaimSet from "./addressClaimSet.js";
import type { Category } from "./resourceServerRepresentation.js";

/** JWT/OIDC 访问令牌的完整声明映射，对应 Keycloak AccessToken 序列化结构。 */
export default interface AccessTokenRepresentation {
  /** Authentication Context Class Reference */
  acr?: string;
  /** 结构化地址声明 */
  address?: AddressClaimSet;
  /** CORS 允许的源列表 */
  "allowed-origins"?: string[];
  /** Access Token Hash（OIDC） */
  at_hash?: string;
  /** 用户认证时间（Unix 秒） */
  auth_time?: number;
  /** 嵌套授权决策令牌（UMA） */
  authorization?: AccessTokenRepresentation;
  /** Authorized Party（发起授权的客户端） */
  azp?: string;
  /** 出生日期 */
  birthdate?: string;
  /** Code Hash（OIDC） */
  c_hash?: string;
  /** 资源类别（UMA） */
  category?: Category;
  /** 用户声明可用语言 */
  claims_locales?: string;
  /** 证书确认声明（mTLS 等） */
  cnf?: AccessTokenCertConf;
  /** 电子邮箱 */
  email?: string;
  /** 邮箱是否已验证 */
  email_verified?: boolean;
  /** 过期时间（Unix 秒） */
  exp?: number;
  /** 姓 */
  family_name?: string;
  /** 性别 */
  gender: string;
  /** 名 */
  given_name?: string;
  /** 签发时间（Unix 秒） */
  iat?: number;
  /** 签发者 */
  iss?: string;
  /** JWT ID */
  jti?: string;
  /** 区域设置 */
  locale?: string;
  /** 中间名 */
  middle_name?: string;
  /** 全名 */
  name?: string;
  /** Not Before（Unix 秒） */
  nbf?: number;
  /** 昵称 */
  nickname?: string;
  /** OIDC nonce */
  nonce?: string;
  /** 未映射到标准字段的自定义声明 */
  otherClaims?: { [index: string]: string };
  /** 电话号码 */
  phone_number?: string;
  /** 电话是否已验证 */
  phone_number_verified?: boolean;
  /** 头像 URL */
  picture?: string;
  /** 首选用户名 */
  preferred_username?: string;
  /** 个人资料页 URL */
  profile?: string;
  /** Realm 级角色访问 */
  realm_access?: AccessTokenAccess;
  /** State Hash（OIDC） */
  s_hash?: string;
  /** OAuth scope 字符串 */
  scope?: string;
  /** 会话状态标识 */
  session_state?: string;
  /** Subject（用户 ID） */
  sub?: string;
  /** 受信任证书列表 */
  "trusted-certs"?: string[];
  /** 令牌类型 */
  typ?: string;
  /** 用户信息最后更新时间（Unix 秒） */
  updated_at?: number;
  /** 个人网站 */
  website?: string;
  /** 时区 */
  zoneinfo?: string;
}
