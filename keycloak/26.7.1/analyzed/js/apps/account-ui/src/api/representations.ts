// Generated using typescript-generator version 2.37.1128 on 2022-09-16 15:57:05.
/**
 * Account Console API 数据模型（由 Java 服务端类型自动生成）。
 * 定义账户、凭据、会话、权限等资源的前端 TypeScript 接口。
 */

/** 账户关联 URI：含 nonce 与 hash 用于安全绑定流程。 */
export interface AccountLinkUriRepresentation {
  accountLinkUri: string;
  nonce: string;
  hash: string;
}

/** OAuth/OIDC 客户端（应用）在账户控制台中的展示信息。 */
export interface ClientRepresentation {
  clientId: string;
  clientName: string;
  description: string;
  userConsentRequired: boolean;
  inUse: boolean;
  offlineAccess: boolean;
  rootUrl: string;
  baseUrl: string;
  effectiveUrl: string;
  consent?: ConsentRepresentation;
  logoUri: string;
  policyUri: string;
  tosUri: string;
}

/** 用户对客户端的授权同意记录。 */
export interface ConsentRepresentation {
  grantedScopes: ConsentScopeRepresentation[];
  createdDate: number;
  lastUpdatedDate: number;
}

/** 授权同意中的单个作用域。 */
export interface ConsentScopeRepresentation {
  id: string;
  name: string;
  displayText: string;
}

/** 凭据元数据中的国际化消息（键 + 参数占位符）。 */
export interface CredentialMetadataRepresentationMessage {
  key: string;
  parameters?: string[];
}

/** 单条凭据的展示元数据（图标、提示、警告等）。 */
export interface CredentialMetadataRepresentation {
  infoMessage?: CredentialMetadataRepresentationMessage;
  infoProperties?: CredentialMetadataRepresentationMessage[];
  warningMessageTitle?: CredentialMetadataRepresentationMessage;
  warningMessageDescription?: CredentialMetadataRepresentationMessage;
  credential: CredentialRepresentation;
  iconLight?: string;
  iconDark?: string;
}

/** 用户登录设备及其关联会话的汇总信息。 */
export interface DeviceRepresentation {
  id: string;
  ipAddress: string;
  os: string;
  osVersion: string;
  browser: string;
  device: string;
  lastAccess: number;
  current: boolean;
  sessions: SessionRepresentation[];
  mobile: boolean;
}

/** 与外部身份提供者关联的账户信息。 */
export interface LinkedAccountRepresentation {
  connected: boolean;
  providerAlias: string;
  providerName: string;
  displayName: string;
  linkedUsername: string;
  social: boolean;
}

/** 单条用户会话详情。 */
export interface SessionRepresentation {
  id: string;
  ipAddress: string;
  started: number;
  lastAccess: number;
  expires: number;
  clients: ClientRepresentation[];
  browser: string;
  current: boolean;
}

/** 用户档案中单个属性的元数据（校验规则、只读等）。 */
export interface UserProfileAttributeMetadata {
  name: string;
  displayName: string;
  required: boolean;
  readOnly: boolean;
  annotations?: { [index: string]: any };
  validators: { [index: string]: { [index: string]: any } };
  multivalued: boolean;
  defaultValue: string;
}

/** 用户档案字段定义集合。 */
export interface UserProfileMetadata {
  attributes: UserProfileAttributeMetadata[];
}

/** 用户表示：基础用户属性与用户档案元数据的交叉类型。 */
export type UserRepresentation = any & {
  userProfileMetadata: UserProfileMetadata;
};

/** 单条认证凭据（密码、WebAuthn、OTP 等）的存储表示。 */
export interface CredentialRepresentation {
  id: string;
  type: string;
  userLabel: string;
  createdDate: number;
  secretData: string;
  credentialData: string;
  priority: number;
  value: string;
  temporary: boolean;
  /**
   * @deprecated
   */
  device: string;
  /**
   * @deprecated
   */
  hashedSaltedValue: string;
  /**
   * @deprecated
   */
  salt: string;
  /**
   * @deprecated
   */
  hashIterations: number;
  /**
   * @deprecated
   */
  counter: number;
  /**
   * @deprecated
   */
  algorithm: string;
  /**
   * @deprecated
   */
  digits: number;
  /**
   * @deprecated
   */
  period: number;
  /**
   * @deprecated
   */
  config: { [index: string]: string[] };
}

/** 凭据类型的 UI 元数据（分类、操作链接、图标等）。 */
export interface CredentialTypeMetadata {
  type: string;
  displayName: string;
  helpText: string;
  iconCssClass: string;
  createAction: string;
  updateAction: string;
  removeable: boolean;
  category: "basic-authentication" | "two-factor" | "passwordless";
}

/** 按类型分组的凭据容器，含该类型下所有凭据实例。 */
export interface CredentialContainer {
  type: string;
  category: string;
  displayName: string;
  helptext: string;
  iconCssClass: string;
  createAction: string;
  updateAction: string;
  removeable: boolean;
  userCredentialMetadatas: CredentialMetadataRepresentation[];
  metadata: CredentialTypeMetadata;
}

/** 资源所属客户端的简要信息。 */
export interface Client {
  baseUrl: string;
  clientId: string;
  name?: string;
}

/** 授权作用域。 */
export interface Scope {
  name: string;
  displayName?: string;
}

/** 用户拥有的可共享资源。 */
export interface Resource {
  _id: string;
  name: string;
  client: Client;
  scopes: Scope[];
  uris: string[];
  shareRequests?: Permission[];
}

/** 资源上的权限授予或申请记录。 */
export interface Permission {
  email?: string;
  firstName?: string;
  lastName?: string;
  scopes: Scope[] | string[]; // this should be Scope[] - fix API
  username: string;
}

/** 权限列表响应（含可选行号）。 */
export interface Permissions {
  permissions: Permission[];
  row?: number;
}

/** 用户所属组。 */
export interface Group {
  id?: string;
  name: string;
  path: string;
}

export type { default as UserVerifiableCredentialRepresentation } from "@keycloak/keycloak-admin-client/lib/defs/userVerifiableCredentialRepresentation";
export type { default as IssuedUserVerifiableCredentialRepresentation } from "@keycloak/keycloak-admin-client/lib/defs/issuedUserVerifiableCredentialRepresentation";
