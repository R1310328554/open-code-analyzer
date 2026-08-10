/**
 * 资源服务器（Resource Server）表示：OAuth 客户端的细粒度授权配置容器。
 * https://www.keycloak.org/docs-api/11.0/rest-api/index.html#_policyrepresentation
 */
import type PolicyRepresentation from "./policyRepresentation.js";
import type ResourceRepresentation from "./resourceRepresentation.js";
import type ScopeRepresentation from "./scopeRepresentation.js";

export default interface ResourceServerRepresentation {
  /** 资源服务器 UUID */
  id?: string;
  /** 关联的 OAuth 客户端 ID */
  clientId?: string;
  /** 资源服务器名称 */
  name?: string;
  /** 是否允许远程资源管理（UMA 资源所有者 API） */
  allowRemoteResourceManagement?: boolean;
  /** 授权模式 JSON Schema 定义 */
  authorizationSchema?: AuthorizationSchemaRepresentation;
  /** 策略执行模式（强制/宽松/禁用） */
  policyEnforcementMode?: PolicyEnforcementMode;
  /** 受保护资源列表 */
  resources?: ResourceRepresentation[];
  /** 授权策略列表 */
  policies?: PolicyRepresentation[];
  /** 作用域列表 */
  scopes?: ScopeRepresentation[];
  /** 多条策略组合时的决策策略 */
  decisionStrategy?: DecisionStrategy;
}

/** 授权 Schema：定义资源类型及其默认作用域 */
export interface AuthorizationSchemaRepresentation {
  /** 资源类型定义列表 */
  resourceTypes?: ResourceTypesRepresentation[];
}

/** 资源所有者标识（用户或客户端） */
export interface ResourceOwnerRepresentation {
  /** 所有者 ID */
  id?: string;
  /** 所有者名称 */
  name?: string;
}

/** 资源类型定义：类型名及其关联的作用域名称 */
export interface ResourceTypesRepresentation {
  /** 资源类型名称 */
  type?: string;
  /** 该类型资源默认关联的作用域名称列表 */
  scopes?: string[];
}

/** 授权策略的抽象基结构（共享字段） */
export interface AbstractPolicyRepresentation {
  /** 策略 UUID */
  id?: string;
  /** 策略名称 */
  name?: string;
  /** 策略描述 */
  description?: string;
  /** 策略类型（如 role、group、js） */
  type?: string;
  /** 引用的子策略 ID 列表（聚合策略） */
  policies?: string[];
  /** 关联的资源 ID 列表 */
  resources?: string[];
  /** 关联的作用域 ID 列表 */
  scopes?: string[];
  /** 条件逻辑（正向/负向） */
  logic?: Logic;
  /** 决策策略 */
  decisionStrategy?: DecisionStrategy;
  /** 策略所有者 ID */
  owner?: string;
  /** 关联资源的完整数据（展开响应） */
  resourcesData?: ResourceRepresentation[];
  /** 关联作用域的完整数据（展开响应） */
  scopesData?: ScopeRepresentation[];
}

/** 策略执行模式：控制授权服务对缺失权限的处理方式 */
export type PolicyEnforcementMode = "ENFORCING" | "PERMISSIVE" | "DISABLED";

/** 多条子策略组合时的聚合决策策略 */
export type DecisionStrategy = "AFFIRMATIVE" | "UNANIMOUS" | "CONSENSUS";

/** 策略条件逻辑：正向匹配或取反 */
export type Logic = "POSITIVE" | "NEGATIVE";

/** 策略/映射器类别（内部、访问、身份、管理、用户信息） */
export type Category = "INTERNAL" | "ACCESS" | "ID" | "ADMIN" | "USERINFO";
