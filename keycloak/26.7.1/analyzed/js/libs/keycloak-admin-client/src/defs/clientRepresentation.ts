/**
 * OAuth/OIDC 客户端完整配置：Admin REST API 创建/更新客户端时的核心数据结构。
 * https://www.keycloak.org/docs-api/11.0/rest-api/index.html#_clientrepresentation
 */
import type ResourceServerRepresentation from "./resourceServerRepresentation.js";
import type ProtocolMapperRepresentation from "./protocolMapperRepresentation.js";

export default interface ClientRepresentation {
  /** 当前用户对客户端的管理权限映射 */
  access?: Record<string, boolean>;
  /** 客户端管理回调 URL（Admin API 通知等） */
  adminUrl?: string;
  /** 扩展属性（如 pkce.code.challenge.method、saml 相关开关） */
  attributes?: Record<string, any>;
  /** 认证流程绑定覆盖（browser、direct grant 等 flow alias） */
  authenticationFlowBindingOverrides?: Record<string, any>;
  /** 是否启用细粒度授权服务（UMA/Resource Server） */
  authorizationServicesEnabled?: boolean;
  /** 授权服务（Resource Server）详细设置 */
  authorizationSettings?: ResourceServerRepresentation;
  /** 应用相对路径前缀 */
  baseUrl?: string;
  /** 纯 Bearer 客户端（无浏览器重定向，仅校验 Access Token） */
  bearerOnly?: boolean;
  /** 客户端认证方式（client-secret、client-jwt 等） */
  clientAuthenticatorType?: string;
  /** 客户端公开标识符（OAuth client_id） */
  clientId?: string;
  /** 是否要求用户显式同意授权 */
  consentRequired?: boolean;
  /** 默认分配的 Client Scope 名称列表 */
  defaultClientScopes?: string[];
  /** 客户端默认角色（已弃用字段，兼容旧 API） */
  defaultRoles?: string[];
  /** 客户端描述 */
  description?: string;
  /** 是否允许 Resource Owner Password Credentials 授权 */
  directAccessGrantsEnabled?: boolean;
  /** 客户端是否启用 */
  enabled?: boolean;
  /** 是否在管理控制台客户端列表中始终显示 */
  alwaysDisplayInConsole?: boolean;
  /** 是否启用前端通道登出（iframe/postMessage） */
  frontchannelLogout?: boolean;
  /** 是否允许访问全部 Realm 角色与 Scope（full scope） */
  fullScopeAllowed?: boolean;
  /** 客户端内部 UUID */
  id?: string;
  /** 是否启用隐式授权流程（Implicit Flow） */
  implicitFlowEnabled?: boolean;
  /** 客户端显示名称 */
  name?: string;
  /** 集群节点再注册超时（秒），用于适配器心跳 */
  nodeReRegistrationTimeout?: number;
  /** 令牌生效起始时间（not-before 策略，Unix 秒） */
  notBefore?: number;
  /** 可选 Client Scope 名称列表（需显式请求才附带） */
  optionalClientScopes?: string[];
  /** 客户端来源标识（导入/联邦场景） */
  origin?: string;
  /** 协议类型（openid-connect、saml 等） */
  protocol?: string;
  /** 客户端级协议映射器（Claim/Attribute 映射规则） */
  protocolMappers?: ProtocolMapperRepresentation[];
  /** 是否为公开客户端（无 client secret） */
  publicClient?: boolean;
  /** 合法重定向 URI 白名单 */
  redirectUris?: string[];
  /** 已注册适配器节点及其最后心跳信息 */
  registeredNodes?: Record<string, any>;
  /** 动态客户端注册访问令牌 */
  registrationAccessToken?: string;
  /** 应用根 URL */
  rootUrl?: string;
  /** 客户端密钥（机密客户端） */
  secret?: string;
  /** 是否启用 Service Account（客户端凭证授权） */
  serviceAccountsEnabled?: boolean;
  /** 是否启用标准授权码流程（Authorization Code） */
  standardFlowEnabled?: boolean;
  /** 是否需要代理认证（surrogate auth） */
  surrogateAuthRequired?: boolean;
  /** CORS 允许的 Web Origin 列表 */
  webOrigins?: string[];
}
