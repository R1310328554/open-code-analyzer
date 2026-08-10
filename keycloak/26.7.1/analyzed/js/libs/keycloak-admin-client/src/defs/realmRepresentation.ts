import type ClientRepresentation from "./clientRepresentation.js";
import type ComponentExportRepresentation from "./componentExportRepresentation.js";
import type UserRepresentation from "./userRepresentation.js";
import type GroupRepresentation from "./groupRepresentation.js";
import type IdentityProviderRepresentation from "./identityProviderRepresentation.js";
import type RequiredActionProviderRepresentation from "./requiredActionProviderRepresentation.js";
import type RolesRepresentation from "./rolesRepresentation.js";
import type ClientProfilesRepresentation from "./clientProfilesRepresentation.js";
import type ClientPoliciesRepresentation from "./clientPoliciesRepresentation.js";
import type RoleRepresentation from "./roleRepresentation.js";

/**
 * Realm 完整表示：租户级身份与访问管理配置，涵盖令牌生命周期、认证流、角色、联邦与主题等。
 * https://www.keycloak.org/docs-api/11.0/rest-api/index.html#_realmrepresentation
 */

export default interface RealmRepresentation {
  /** 授权码有效期（秒） */
  accessCodeLifespan?: number;
  /** 登录流程中授权码有效期（秒） */
  accessCodeLifespanLogin?: number;
  /** 用户操作触发的授权码有效期（秒） */
  accessCodeLifespanUserAction?: number;
  /** Access Token 有效期（秒） */
  accessTokenLifespan?: number;
  /** 隐式流 Access Token 有效期（秒） */
  accessTokenLifespanForImplicitFlow?: number;
  /** Account Console 主题名称 */
  accountTheme?: string;
  /** 管理员生成的 Action Token 有效期（秒） */
  actionTokenGeneratedByAdminLifespan?: number;
  /** 用户触发的 Action Token 有效期（秒） */
  actionTokenGeneratedByUserLifespan?: number;
  /** Admin 事件是否包含请求/响应详情 */
  adminEventsDetailsEnabled?: boolean;
  /** 是否记录 Admin REST API 操作事件 */
  adminEventsEnabled?: boolean;
  /** Admin 权限管理专用客户端 */
  adminPermissionsClient?: ClientRepresentation;
  /** 是否启用细粒度 Admin 权限 */
  adminPermissionsEnabled?: boolean;
  /** Admin Console 主题名称 */
  adminTheme?: string;
  /** Realm 级自定义属性 */
  attributes?: Record<string, any>;
  // AuthenticationFlowRepresentation
  /** 认证流定义列表 */
  authenticationFlows?: any[];
  // AuthenticatorConfigRepresentation
  /** 认证器配置列表 */
  authenticatorConfig?: any[];
  /** 浏览器 SSO 认证流别名 */
  browserFlow?: string;
  /** 浏览器安全响应头配置 */
  browserSecurityHeaders?: Record<string, any>;
  /** 是否启用暴力破解防护 */
  bruteForceProtected?: boolean;
  /** 客户端认证流别名 */
  clientAuthenticationFlow?: string;
  /** 客户端到 Client Scope 的映射 */
  clientScopeMappings?: Record<string, any>;
  // ClientScopeRepresentation
  /** Client Scope 定义列表 */
  clientScopes?: any[];
  /** Realm 内 OAuth/OIDC 客户端列表 */
  clients?: ClientRepresentation[];
  /** 客户端策略（Client Policies）配置 */
  clientPolicies?: ClientPoliciesRepresentation;
  /** 客户端配置 Profile 定义 */
  clientProfiles?: ClientProfilesRepresentation;
  /** 可导出组件（如密钥提供者、用户联邦） */
  components?: { [index: string]: ComponentExportRepresentation };
  /** 新客户端默认绑定的 Client Scope 名称 */
  defaultDefaultClientScopes?: string[];
  /** 新用户默认加入的组名称 */
  defaultGroups?: string[];
  /** 默认语言区域 */
  defaultLocale?: string;
  /** 新客户端默认可选的 Client Scope 名称 */
  defaultOptionalClientScopes?: string[];
  /** 默认 Realm 角色名称列表（遗留字段） */
  defaultRoles?: string[];
  /** 默认 Realm 角色对象 */
  defaultRole?: RoleRepresentation;
  /** 默认 Token 签名算法 */
  defaultSignatureAlgorithm?: string;
  /** Direct Grant（资源所有者密码）认证流别名 */
  directGrantFlow?: string;
  /** Realm 显示名称 */
  displayName?: string;
  /** Realm 显示名称（HTML 格式） */
  displayNameHtml?: string;
  /** Docker 认证流别名 */
  dockerAuthenticationFlow?: string;
  /** 是否允许重复邮箱注册 */
  duplicateEmailsAllowed?: boolean;
  /** 是否允许用户自行修改用户名 */
  editUsernameAllowed?: boolean;
  /** 邮件主题名称 */
  emailTheme?: string;
  /** Realm 是否启用 */
  enabled?: boolean;
  /** 启用的用户事件类型名称列表 */
  enabledEventTypes?: string[];
  /** 是否启用用户事件记录 */
  eventsEnabled?: boolean;
  /** 用户事件保留时长（秒） */
  eventsExpiration?: number;
  /** 事件监听器 SPI ID 列表 */
  eventsListeners?: string[];
  /** 暴力破解：触发临时锁定的连续失败次数阈值 */
  failureFactor?: number;
  /** 二级认证连续失败次数上限 */
  maxSecondaryAuthFailures?: number;
  /** 联邦身份用户列表（导入/导出场景） */
  federatedUsers?: UserRepresentation[];
  /** Realm 用户组列表 */
  groups?: GroupRepresentation[];
  /** Realm UUID */
  id?: string;
  // IdentityProviderMapperRepresentation
  /** 身份提供者属性映射器列表 */
  identityProviderMappers?: any[];
  /** 身份联邦提供者配置列表 */
  identityProviders?: IdentityProviderRepresentation[];
  /** 是否启用国际化（多语言登录页等） */
  internationalizationEnabled?: boolean;
  /** 创建/导出时的 Keycloak 版本号 */
  keycloakVersion?: string;
  /** 登录页主题名称 */
  loginTheme?: string;
  /** 是否允许使用邮箱登录 */
  loginWithEmailAllowed?: boolean;
  /** 暴力破解：失败计数重置窗口（秒） */
  maxDeltaTimeSeconds?: number;
  /** 暴力破解：最长等待时间（秒） */
  maxFailureWaitSeconds?: number;
  /** 暴力破解：最大临时锁定次数 */
  maxTemporaryLockouts?: number;
  /** 暴力破解策略（多次递增或线性等待） */
  bruteForceStrategy?: "MULTIPLE" | "LINEAR";
  /** 暴力破解：快速连续登录检测窗口（秒） */
  minimumQuickLoginWaitSeconds?: number;
  /** 全局「生效不早于」时间戳（秒），用于强制登出 */
  notBefore?: number;
  /** OAuth 2.0 设备授权码有效期（秒） */
  oauth2DeviceCodeLifespan?: number;
  /** 设备授权轮询间隔（秒） */
  oauth2DevicePollingInterval?: number;
  /** 离线 Session 空闲超时（秒） */
  offlineSessionIdleTimeout?: number;
  /** 离线 Session 最大生命周期（秒） */
  offlineSessionMaxLifespan?: number;
  /** 是否启用离线 Session 最大生命周期限制 */
  offlineSessionMaxLifespanEnabled?: boolean;
  /** 是否启用组织（Organizations）功能 */
  organizationsEnabled?: boolean;
  /** 是否启用可验证凭证（Verifiable Credentials） */
  verifiableCredentialsEnabled?: boolean;
  /** OTP 哈希算法 */
  otpPolicyAlgorithm?: string;
  /** OTP 位数 */
  otpPolicyDigits?: number;
  /** HOTP 初始计数器值 */
  otpPolicyInitialCounter?: number;
  /** OTP 时间窗口容差 */
  otpPolicyLookAheadWindow?: number;
  /** TOTP 时间步长（秒） */
  otpPolicyPeriod?: number;
  /** OTP 类型（totp 或 hotp） */
  otpPolicyType?: string;
  /** 支持的 OTP 应用列表 */
  otpSupportedApplications?: string[];
  /** OTP 码是否可重复使用 */
  otpPolicyCodeReusable?: boolean;
  /** 密码策略规则字符串（如 length(8)） */
  passwordPolicy?: string;
  /** 暴力破解：是否永久锁定（而非临时） */
  permanentLockout?: boolean;
  // ProtocolMapperRepresentation
  /** Realm 级协议映射器列表 */
  protocolMappers?: any[];
  /** 快速连续登录检测间隔（毫秒） */
  quickLoginCheckMilliSeconds?: number;
  /** Realm 名称（唯一标识） */
  realm?: string;
  /** Refresh Token 最大重用次数 */
  refreshTokenMaxReuse?: number;
  /** 是否允许自助注册 */
  registrationAllowed?: boolean;
  /** 注册时是否以邮箱作为用户名 */
  registrationEmailAsUsername?: boolean;
  /** 注册认证流别名 */
  registrationFlow?: string;
  /** 是否启用「记住我」 */
  rememberMe?: boolean;
  /** Required Action 提供者配置列表 */
  requiredActions?: RequiredActionProviderRepresentation[];
  /** 重置凭证认证流别名 */
  resetCredentialsFlow?: string;
  /** 是否允许用户自助重置密码 */
  resetPasswordAllowed?: boolean;
  /** 是否在 Refresh 时撤销旧 Refresh Token */
  revokeRefreshToken?: boolean;
  /** Realm/Client 角色集合 */
  roles?: RolesRepresentation;
  // ScopeMappingRepresentation
  /** Client Scope 到角色的映射 */
  scopeMappings?: any[];
  /** SMTP 邮件服务器配置 */
  smtpServer?: Record<string, any>;
  /** SSL 要求级别（external、all、none） */
  sslRequired?: string;
  /** SSO Session 空闲超时（秒） */
  ssoSessionIdleTimeout?: number;
  /** 「记住我」场景下 SSO Session 空闲超时（秒） */
  ssoSessionIdleTimeoutRememberMe?: number;
  /** SSO Session 最大生命周期（秒） */
  ssoSessionMaxLifespan?: number;
  /** 「记住我」场景下 SSO Session 最大生命周期（秒） */
  ssoSessionMaxLifespanRememberMe?: number;
  /** 客户端 Session 空闲超时（秒） */
  clientSessionIdleTimeout?: number;
  /** 客户端 Session 最大生命周期（秒） */
  clientSessionMaxLifespan?: number;
  /** 客户端离线 Session 空闲超时（秒） */
  clientOfflineSessionIdleTimeout?: number;
  /** 客户端离线 Session 最大生命周期（秒） */
  clientOfflineSessionMaxLifespan?: number;
  /** 支持的语言区域列表 */
  supportedLocales?: string[];
  // UserFederationMapperRepresentation
  /** 用户联邦属性映射器列表 */
  userFederationMappers?: any[];
  // UserFederationProviderRepresentation
  /** 用户联邦提供者配置列表（如 LDAP） */
  userFederationProviders?: any[];
  /** 是否允许用户自行管理资源访问（UMA） */
  userManagedAccessAllowed?: boolean;
  /** Realm 用户列表（导入/导出场景） */
  users?: UserRepresentation[];
  /** 注册后是否要求验证邮箱 */
  verifyEmail?: boolean;
  /** 暴力破解：每次失败后等待增量（秒） */
  waitIncrementSeconds?: number;
  /** WebAuthn 无密码 Passkey 策略是否启用 */
  webAuthnPolicyPasswordlessPasskeysEnabled?: boolean;
  /** 是否启用 SCIM API */
  scimApiEnabled?: boolean;
}

/** 部分导入 Realm 请求：附带资源冲突处理策略 */
export type PartialImportRealmRepresentation = RealmRepresentation & {
  /** 导入时遇到已存在资源的处理方式 */
  ifResourceExists: "FAIL" | "SKIP" | "OVERWRITE";
};

/** 部分导入响应：汇总覆盖/新增/跳过数量及逐条结果 */
export type PartialImportResponse = {
  /** 被覆盖的资源数量 */
  overwritten: number;
  /** 新增的资源数量 */
  added: number;
  /** 跳过的资源数量 */
  skipped: number;
  /** 逐条导入结果 */
  results: PartialImportResult[];
};

/** 单条部分导入结果 */
export type PartialImportResult = {
  /** 执行的操作（如 CREATED、OVERWRITTEN） */
  action: string;
  /** 资源类型（如 CLIENT、USER） */
  resourceType: string;
  /** 资源名称 */
  resourceName: string;
  /** 资源 ID */
  id: string;
};
