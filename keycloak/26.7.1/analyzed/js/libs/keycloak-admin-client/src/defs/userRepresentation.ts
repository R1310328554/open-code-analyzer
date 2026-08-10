import type CredentialRepresentation from "./credentialRepresentation.js";
import type FederatedIdentityRepresentation from "./federatedIdentityRepresentation.js";
import type { RequiredActionAlias } from "./requiredActionProviderRepresentation.js";
import type UserConsentRepresentation from "./userConsentRepresentation.js";
import type { UserProfileMetadata } from "./userProfileMetadata.js";

/**
 * Realm 用户表示：Admin/Users API 创建、更新与查询用户时的核心数据结构。
 * 涵盖本地/联邦身份、凭证、角色、属性及 User Profile 元数据。
 */
export default interface UserRepresentation {
  /** 用户 UUID */
  id?: string;
  /** 账户创建时间（Unix 毫秒时间戳） */
  createdTimestamp?: number;
  /** 登录用户名（Realm 内唯一） */
  username?: string;
  /** 账户是否启用 */
  enabled?: boolean;
  /** 是否已配置 TOTP（双因素） */
  totp?: boolean;
  /** 邮箱是否已验证 */
  emailVerified?: boolean;
  /** 当前不可禁用的凭证类型 ID 列表 */
  disableableCredentialTypes?: string[];
  /** 待完成的 Required Action 别名列表 */
  requiredActions?: (RequiredActionAlias | string)[];
  /** 令牌生效起始时间（not-before，Unix 秒） */
  notBefore?: number;
  /** 细粒度管理权限映射（如 manageGroupMembership） */
  access?: Record<string, boolean>;

  // optional from response — 以下字段多见于 GET 响应，创建/更新时可选
  /** 自定义用户属性（键为多值字符串数组） */
  attributes?: Record<string, any>;
  /** 用户对各客户端的 OAuth 同意记录 */
  clientConsents?: UserConsentRepresentation[];
  /** 按客户端 ID 分组的客户端角色映射 */
  clientRoles?: Record<string, any>;
  /** 用户凭证列表（密码、OTP、WebAuthn 等） */
  credentials?: CredentialRepresentation[];
  /** 电子邮箱 */
  email?: string;
  /** 关联的外部身份提供者链接（社交登录/联邦 IdP） */
  federatedIdentities?: FederatedIdentityRepresentation[];
  /** 联邦 User Storage 组件链接 ID */
  federationLink?: string;
  /** 名 */
  firstName?: string;
  /** 用户所属组路径或名称列表 */
  groups?: string[];
  /** 姓 */
  lastName?: string;
  /** Realm 级角色名称列表 */
  realmRoles?: string[];
  /** 该用户资源的 HAL/REST 自链接 */
  self?: string;
  /** 若为 Service Account，对应客户端 ID */
  serviceAccountClientId?: string;
  /** User Profile 属性与分组的运行时元数据 */
  userProfileMetadata?: UserProfileMetadata;
}
