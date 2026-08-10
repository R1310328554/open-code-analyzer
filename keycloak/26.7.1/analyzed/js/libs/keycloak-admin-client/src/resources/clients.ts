import type { KeycloakAdminClient } from "../client.js";
import { NetworkError } from "../utils/fetchWithError.js";
import type CertificateRepresentation from "../defs/certificateRepresentation.js";
import type ClientRepresentation from "../defs/clientRepresentation.js";
import type ClientScopeRepresentation from "../defs/clientScopeRepresentation.js";
import type CredentialRepresentation from "../defs/credentialRepresentation.js";
import type GlobalRequestResult from "../defs/globalRequestResult.js";
import type KeyStoreConfig from "../defs/keystoreConfig.js";
import type { ManagementPermissionReference } from "../defs/managementPermissionReference.js";
import type MappingsRepresentation from "../defs/mappingsRepresentation.js";
import type PolicyEvaluationResponse from "../defs/policyEvaluationResponse.js";
import type PolicyProviderRepresentation from "../defs/policyProviderRepresentation.js";
import type PolicyRepresentation from "../defs/policyRepresentation.js";
import type ProtocolMapperRepresentation from "../defs/protocolMapperRepresentation.js";
import type ResourceEvaluation from "../defs/resourceEvaluation.js";
import type ResourceRepresentation from "../defs/resourceRepresentation.js";
import type ResourceServerRepresentation from "../defs/resourceServerRepresentation.js";
import type RoleRepresentation from "../defs/roleRepresentation.js";
import type ScopeRepresentation from "../defs/scopeRepresentation.js";
import type UserRepresentation from "../defs/userRepresentation.js";
import type UserSessionRepresentation from "../defs/userSessionRepresentation.js";
import Resource from "./resource.js";
import { ClientsV2 } from "./clientsV2.js";

/** 分页查询参数 */
export interface PaginatedQuery {
  first?: number;
  max?: number;
}

/** 客户端列表查询参数 */
export interface ClientQuery extends PaginatedQuery {
  clientId?: string;
  viewableOnly?: boolean;
  search?: boolean;
  q?: string;
}

/** 授权资源查询参数 */
export interface ResourceQuery extends PaginatedQuery {
  id?: string;
  name?: string;
  type?: string;
  owner?: string;
  uri?: string;
  deep?: boolean;
}

/** 授权策略查询参数 */
export interface PolicyQuery extends PaginatedQuery {
  id?: string;
  name?: string;
  type?: string;
  resource?: string;
  scope?: string;
  permission?: string;
  owner?: string;
  fields?: string;
}

/** 客户端 Admin 资源：OAuth/OIDC/SAML 客户端 CRUD、角色、Scope、会话、授权服务（UMA）及证书管理。 */
export class Clients extends Resource<{ realm?: string }> {
  /**
   * Clients v2 API - New versioned API with OpenAPI-generated client.
   */
  #v2: ClientsV2;
  #client: KeycloakAdminClient;

  /** 查询列表 */
  public find = this.makeRequest<ClientQuery, ClientRepresentation[]>({
    method: "GET",
  });

  /** 创建 */
  public create = this.makeRequest<ClientRepresentation, { id: string }>({
    method: "POST",
    returnResourceIdInLocationHeader: { field: "id" },
  });

  /**
   * 单个客户端
   * Single client
   */

  public findOne = this.makeRequest<
    { id: string },
    ClientRepresentation | undefined
  >({
    method: "GET",
    path: "/{id}",
    urlParamKeys: ["id"],
    catchNotFound: true,
  });

  /** 更新 */
  public update = this.makeUpdateRequest<
    { id: string },
    ClientRepresentation,
    void
  >({
    method: "PUT",
    path: "/{id}",
    urlParamKeys: ["id"],
  });

  /** 删除 */
  public del = this.makeRequest<{ id: string }, void>({
    method: "DELETE",
    path: "/{id}",
    urlParamKeys: ["id"],
  });

  /**
   * 客户端角色
   * Client roles
   */

  public createRole = this.makeRequest<
    RoleRepresentation,
    { roleName: string }
  >({
    method: "POST",
    path: "/{id}/roles",
    urlParamKeys: ["id"],
    returnResourceIdInLocationHeader: { field: "roleName" },
  });

  /** 列出客户端角色 */
  public listRoles = this.makeRequest<{ id: string }, RoleRepresentation[]>({
    method: "GET",
    path: "/{id}/roles",
    urlParamKeys: ["id"],
  });

  /** 按名称获取客户端角色 */
  public findRole = this.makeRequest<
    { id: string; roleName: string },
    RoleRepresentation | null
  >({
    method: "GET",
    path: "/{id}/roles/{roleName}",
    urlParamKeys: ["id", "roleName"],
    catchNotFound: true,
  });

  /** 更新客户端角色 */
  public updateRole = this.makeUpdateRequest<
    { id: string; roleName: string },
    RoleRepresentation,
    void
  >({
    method: "PUT",
    path: "/{id}/roles/{roleName}",
    urlParamKeys: ["id", "roleName"],
  });

  /** 删除客户端角色 */
  public delRole = this.makeRequest<{ id: string; roleName: string }, void>({
    method: "DELETE",
    path: "/{id}/roles/{roleName}",
    urlParamKeys: ["id", "roleName"],
  });

  /** 列出拥有指定角色的用户 */
  public findUsersWithRole = this.makeRequest<
    {
      id: string;
      roleName: string;
      briefRepresentation?: boolean;
      first?: number;
      max?: number;
    },
    UserRepresentation[]
  >({
    method: "GET",
    path: "/{id}/roles/{roleName}/users",
    urlParamKeys: ["id", "roleName"],
  });

  /**
   * 服务账户用户
   * Service account user
   */

  public getServiceAccountUser = this.makeRequest<
    { id: string },
    UserRepresentation
  >({
    method: "GET",
    path: "/{id}/service-account-user",
    urlParamKeys: ["id"],
  });

  /**
   * 客户端密钥
   * Client secret
   */

  public generateNewClientSecret = this.makeRequest<
    { id: string },
    CredentialRepresentation
  >({
    method: "POST",
    path: "/{id}/client-secret",
    urlParamKeys: ["id"],
  });

  /** 使已轮换的客户端密钥失效 */
  public invalidateSecret = this.makeRequest<{ id: string }, void>({
    method: "DELETE",
    path: "/{id}/client-secret/rotated",
    urlParamKeys: ["id"],
  });

  /** 生成客户端注册访问令牌 */
  public generateRegistrationAccessToken = this.makeRequest<
    { id: string },
    { registrationAccessToken: string }
  >({
    method: "POST",
    path: "/{id}/registration-access-token",
    urlParamKeys: ["id"],
  });

  /** 获取客户端密钥 */
  public getClientSecret = this.makeRequest<
    { id: string },
    CredentialRepresentation
  >({
    method: "GET",
    path: "/{id}/client-secret",
    urlParamKeys: ["id"],
  });

  /**
   * 客户端 Scope 关联
   * Client Scopes
   */
  public listDefaultClientScopes = this.makeRequest<
    { id: string },
    ClientScopeRepresentation[]
  >({
    method: "GET",
    path: "/{id}/default-client-scopes",
    urlParamKeys: ["id"],
  });

  /** 添加默认客户端 Scope */
  public addDefaultClientScope = this.makeRequest<
    { id: string; clientScopeId: string },
    void
  >({
    method: "PUT",
    path: "/{id}/default-client-scopes/{clientScopeId}",
    urlParamKeys: ["id", "clientScopeId"],
  });

  /** 移除默认客户端 Scope */
  public delDefaultClientScope = this.makeRequest<
    { id: string; clientScopeId: string },
    void
  >({
    method: "DELETE",
    path: "/{id}/default-client-scopes/{clientScopeId}",
    urlParamKeys: ["id", "clientScopeId"],
  });

  /** 列出可选客户端 Scope */
  public listOptionalClientScopes = this.makeRequest<
    { id: string },
    ClientScopeRepresentation[]
  >({
    method: "GET",
    path: "/{id}/optional-client-scopes",
    urlParamKeys: ["id"],
  });

  /** 添加可选客户端 Scope */
  public addOptionalClientScope = this.makeRequest<
    { id: string; clientScopeId: string },
    void
  >({
    method: "PUT",
    path: "/{id}/optional-client-scopes/{clientScopeId}",
    urlParamKeys: ["id", "clientScopeId"],
  });

  /** 移除可选客户端 Scope */
  public delOptionalClientScope = this.makeRequest<
    { id: string; clientScopeId: string },
    void
  >({
    method: "DELETE",
    path: "/{id}/optional-client-scopes/{clientScopeId}",
    urlParamKeys: ["id", "clientScopeId"],
  });

  /**
   * 协议映射器
   * Protocol Mappers
   */

  public addMultipleProtocolMappers = this.makeUpdateRequest<
    { id: string },
    ProtocolMapperRepresentation[],
    void
  >({
    method: "POST",
    path: "/{id}/protocol-mappers/add-models",
    urlParamKeys: ["id"],
  });

  /** 添加协议映射器 */
  public addProtocolMapper = this.makeUpdateRequest<
    { id: string },
    ProtocolMapperRepresentation,
    void
  >({
    method: "POST",
    path: "/{id}/protocol-mappers/models",
    urlParamKeys: ["id"],
  });

  /** 列出协议映射器 */
  public listProtocolMappers = this.makeRequest<
    { id: string },
    ProtocolMapperRepresentation[]
  >({
    method: "GET",
    path: "/{id}/protocol-mappers/models",
    urlParamKeys: ["id"],
  });

  /** 按 ID 获取协议映射器 */
  public findProtocolMapperById = this.makeRequest<
    { id: string; mapperId: string },
    ProtocolMapperRepresentation
  >({
    method: "GET",
    path: "/{id}/protocol-mappers/models/{mapperId}",
    urlParamKeys: ["id", "mapperId"],
    catchNotFound: true,
  });

  /** 按协议类型列出协议映射器 */
  public findProtocolMappersByProtocol = this.makeRequest<
    { id: string; protocol: string },
    ProtocolMapperRepresentation[]
  >({
    method: "GET",
    path: "/{id}/protocol-mappers/protocol/{protocol}",
    urlParamKeys: ["id", "protocol"],
    catchNotFound: true,
  });

  /** 更新协议映射器 */
  public updateProtocolMapper = this.makeUpdateRequest<
    { id: string; mapperId: string },
    ProtocolMapperRepresentation,
    void
  >({
    method: "PUT",
    path: "/{id}/protocol-mappers/models/{mapperId}",
    urlParamKeys: ["id", "mapperId"],
  });

  /** 删除协议映射器 */
  public delProtocolMapper = this.makeRequest<
    { id: string; mapperId: string },
    void
  >({
    method: "DELETE",
    path: "/{id}/protocol-mappers/models/{mapperId}",
    urlParamKeys: ["id", "mapperId"],
  });

  /**
   * Scope 角色映射
   * Scope Mappings
   */
  public listScopeMappings = this.makeRequest<
    { id: string },
    MappingsRepresentation
  >({
    method: "GET",
    path: "/{id}/scope-mappings",
    urlParamKeys: ["id"],
  });

  /** 添加客户端 Scope 角色映射 */
  public addClientScopeMappings = this.makeUpdateRequest<
    { id: string; client: string },
    RoleRepresentation[],
    void
  >({
    method: "POST",
    path: "/{id}/scope-mappings/clients/{client}",
    urlParamKeys: ["id", "client"],
  });

  /** 列出客户端 Scope 角色映射 */
  public listClientScopeMappings = this.makeRequest<
    { id: string; client: string },
    RoleRepresentation[]
  >({
    method: "GET",
    path: "/{id}/scope-mappings/clients/{client}",
    urlParamKeys: ["id", "client"],
  });

  /** 列出可分配的客户端 Scope 角色映射 */
  public listAvailableClientScopeMappings = this.makeRequest<
    { id: string; client: string },
    RoleRepresentation[]
  >({
    method: "GET",
    path: "/{id}/scope-mappings/clients/{client}/available",
    urlParamKeys: ["id", "client"],
  });

  /** 列出客户端 Scope 复合角色映射 */
  public listCompositeClientScopeMappings = this.makeRequest<
    { id: string; client: string },
    RoleRepresentation[]
  >({
    method: "GET",
    path: "/{id}/scope-mappings/clients/{client}/composite",
    urlParamKeys: ["id", "client"],
  });

  /** 删除客户端 Scope 角色映射 */
  public delClientScopeMappings = this.makeUpdateRequest<
    { id: string; client: string },
    RoleRepresentation[],
    void
  >({
    method: "DELETE",
    path: "/{id}/scope-mappings/clients/{client}",
    urlParamKeys: ["id", "client"],
  });

  /** 评估 Scope 权限映射 */
  public evaluatePermission = this.makeRequest<
    {
      id: string;
      roleContainer: string;
      type: "granted" | "not-granted";
      scope: string;
    },
    RoleRepresentation[]
  >({
    method: "GET",
    path: "/{id}/evaluate-scopes/scope-mappings/{roleContainer}/{type}",
    urlParamKeys: ["id", "roleContainer", "type"],
    queryParamKeys: ["scope"],
  });

  /** 评估 Scope 下的协议映射器 */
  public evaluateListProtocolMapper = this.makeRequest<
    {
      id: string;
      scope: string;
    },
    ProtocolMapperRepresentation[]
  >({
    method: "GET",
    path: "/{id}/evaluate-scopes/protocol-mappers",
    urlParamKeys: ["id"],
    queryParamKeys: ["scope"],
  });

  /** 生成示例 SAML 响应 */
  public evaluateGenerateSamlResponse = this.makeRequest<
    { id: string; scope: string; userId: string },
    Record<string, unknown>
  >({
    method: "GET",
    path: "/{id}/evaluate-scopes/generate-example-saml-response",
    urlParamKeys: ["id"],
    queryParamKeys: ["scope", "userId"],
  });

  /** 生成示例 Access Token */
  public evaluateGenerateAccessToken = this.makeRequest<
    { id: string; scope: string; userId: string; audience: string },
    Record<string, unknown>
  >({
    method: "GET",
    path: "/{id}/evaluate-scopes/generate-example-access-token",
    urlParamKeys: ["id"],
    queryParamKeys: ["scope", "userId", "audience"],
  });

  /** 生成示例 UserInfo */
  public evaluateGenerateUserInfo = this.makeRequest<
    { id: string; scope: string; userId: string },
    Record<string, unknown>
  >({
    method: "GET",
    path: "/{id}/evaluate-scopes/generate-example-userinfo",
    urlParamKeys: ["id"],
    queryParamKeys: ["scope", "userId"],
  });

  /** 生成示例 ID Token */
  public evaluateGenerateIdToken = this.makeRequest<
    { id: string; scope: string; userId: string },
    Record<string, unknown>
  >({
    method: "GET",
    path: "/{id}/evaluate-scopes/generate-example-id-token",
    urlParamKeys: ["id"],
    queryParamKeys: ["scope", "userId"],
  });

  /** 添加 Realm Scope 角色映射 */
  public addRealmScopeMappings = this.makeUpdateRequest<
    { id: string },
    RoleRepresentation[],
    void
  >({
    method: "POST",
    path: "/{id}/scope-mappings/realm",
    urlParamKeys: ["id", "client"],
  });

  /** 列出 Realm Scope 角色映射 */
  public listRealmScopeMappings = this.makeRequest<
    { id: string },
    RoleRepresentation[]
  >({
    method: "GET",
    path: "/{id}/scope-mappings/realm",
    urlParamKeys: ["id"],
  });

  /** 列出可分配的 Realm Scope 角色映射 */
  public listAvailableRealmScopeMappings = this.makeRequest<
    { id: string },
    RoleRepresentation[]
  >({
    method: "GET",
    path: "/{id}/scope-mappings/realm/available",
    urlParamKeys: ["id"],
  });

  /** 列出 Realm Scope 复合角色映射 */
  public listCompositeRealmScopeMappings = this.makeRequest<
    { id: string },
    RoleRepresentation[]
  >({
    method: "GET",
    path: "/{id}/scope-mappings/realm/composite",
    urlParamKeys: ["id"],
  });

  /** 删除 Realm Scope 角色映射 */
  public delRealmScopeMappings = this.makeUpdateRequest<
    { id: string },
    RoleRepresentation[],
    void
  >({
    method: "DELETE",
    path: "/{id}/scope-mappings/realm",
    urlParamKeys: ["id"],
  });

  /**
   * 会话管理
   * Sessions
   */
  public listSessions = this.makeRequest<
    { id: string; first?: number; max?: number },
    UserSessionRepresentation[]
  >({
    method: "GET",
    path: "/{id}/user-sessions",
    urlParamKeys: ["id"],
  });

  /** 列出离线会话 */
  public listOfflineSessions = this.makeRequest<
    { id: string; first?: number; max?: number },
    UserSessionRepresentation[]
  >({
    method: "GET",
    path: "/{id}/offline-sessions",
    urlParamKeys: ["id"],
  });

  /** 获取在线会话数量 */
  public getSessionCount = this.makeRequest<{ id: string }, { count: number }>({
    method: "GET",
    path: "/{id}/session-count",
    urlParamKeys: ["id"],
  });

  /**
   * 授权资源服务器（UMA）
   * Resource
   */

  public getResourceServer = this.makeRequest<
    { id: string },
    ResourceServerRepresentation
  >({
    method: "GET",
    path: "{id}/authz/resource-server",
    urlParamKeys: ["id"],
  });

  /** 更新授权资源服务器配置 */
  public updateResourceServer = this.makeUpdateRequest<
    { id: string },
    ResourceServerRepresentation,
    void
  >({
    method: "PUT",
    path: "{id}/authz/resource-server",
    urlParamKeys: ["id"],
  });

  /** 列出授权资源 */
  public listResources = this.makeRequest<
    ResourceQuery,
    ResourceRepresentation[]
  >({
    method: "GET",
    path: "{id}/authz/resource-server/resource",
    urlParamKeys: ["id"],
  });

  /** 创建授权资源 */
  public createResource = this.makeUpdateRequest<
    { id: string },
    ResourceRepresentation,
    ResourceRepresentation
  >({
    method: "POST",
    path: "{id}/authz/resource-server/resource",
    urlParamKeys: ["id"],
  });

  /** 获取授权资源 */
  public getResource = this.makeRequest<
    { id: string; resourceId: string },
    ResourceRepresentation
  >({
    method: "GET",
    path: "{id}/authz/resource-server/resource/{resourceId}",
    urlParamKeys: ["id", "resourceId"],
  });

  /** 更新授权资源 */
  public updateResource = this.makeUpdateRequest<
    { id: string; resourceId: string },
    ResourceRepresentation,
    void
  >({
    method: "PUT",
    path: "/{id}/authz/resource-server/resource/{resourceId}",
    urlParamKeys: ["id", "resourceId"],
  });

  /** 删除授权资源 */
  public delResource = this.makeRequest<
    { id: string; resourceId: string },
    void
  >({
    method: "DELETE",
    path: "/{id}/authz/resource-server/resource/{resourceId}",
    urlParamKeys: ["id", "resourceId"],
  });

  /** 导入授权资源配置 */
  public importResource = this.makeUpdateRequest<
    { id: string },
    ResourceServerRepresentation
  >({
    method: "POST",
    path: "/{id}/authz/resource-server/import",
    urlParamKeys: ["id"],
  });

  /** 导出授权资源配置 */
  public exportResource = this.makeRequest<
    { id: string },
    ResourceServerRepresentation
  >({
    method: "GET",
    path: "/{id}/authz/resource-server/settings",
    urlParamKeys: ["id"],
  });

  /** 评估授权策略 */
  public evaluateResource = this.makeUpdateRequest<
    { id: string },
    ResourceEvaluation,
    PolicyEvaluationResponse
  >({
    method: "POST",
    path: "{id}/authz/resource-server/policy/evaluate",
    urlParamKeys: ["id"],
  });

  /**
   * 授权策略
   * Policy
   */
  public listPolicies = this.makeRequest<
    PolicyQuery,
    PolicyRepresentation[] | ""
  >({
    method: "GET",
    path: "{id}/authz/resource-server/policy",
    urlParamKeys: ["id"],
  });

  /** 按名称查找授权策略 */
  public findPolicyByName = this.makeRequest<
    { id: string; name: string },
    PolicyRepresentation
  >({
    method: "GET",
    path: "{id}/authz/resource-server/policy/search",
    urlParamKeys: ["id"],
  });

  /** 更新策略 */
  public updatePolicy = this.makeUpdateRequest<
    { id: string; type: string; policyId: string },
    PolicyRepresentation,
    void
  >({
    method: "PUT",
    path: "/{id}/authz/resource-server/policy/{type}/{policyId}",
    urlParamKeys: ["id", "type", "policyId"],
  });

  /** 创建授权策略 */
  public createPolicy = this.makeUpdateRequest<
    { id: string; type: string },
    PolicyRepresentation,
    PolicyRepresentation
  >({
    method: "POST",
    path: "/{id}/authz/resource-server/policy/{type}",
    urlParamKeys: ["id", "type"],
  });

  /** 按类型和 ID 获取授权策略 */
  public findOnePolicyWithType = this.makeRequest<
    { id: string; type: string; policyId: string },
    void
  >({
    method: "GET",
    path: "/{id}/authz/resource-server/policy/{type}/{policyId}",
    urlParamKeys: ["id", "type", "policyId"],
    catchNotFound: true,
  });

  /** 按 ID 获取授权策略 */
  public findOnePolicy = this.makeRequest<
    { id: string; policyId: string },
    void
  >({
    method: "GET",
    path: "/{id}/authz/resource-server/policy/{policyId}",
    urlParamKeys: ["id", "policyId"],
    catchNotFound: true,
  });

  /** 列出依赖策略 */
  public listDependentPolicies = this.makeRequest<
    { id: string; policyId: string },
    PolicyRepresentation[]
  >({
    method: "GET",
    path: "/{id}/authz/resource-server/policy/{policyId}/dependentPolicies",
    urlParamKeys: ["id", "policyId"],
  });

  /** 删除授权策略 */
  public delPolicy = this.makeRequest<{ id: string; policyId: string }, void>({
    method: "DELETE",
    path: "{id}/authz/resource-server/policy/{policyId}",
    urlParamKeys: ["id", "policyId"],
  });

  /** 列出策略提供者类型 */
  public listPolicyProviders = this.makeRequest<
    { id: string },
    PolicyProviderRepresentation[]
  >({
    method: "GET",
    path: "/{id}/authz/resource-server/policy/providers",
    urlParamKeys: ["id"],
  });

  public async createOrUpdatePolicy(payload: {
    id: string;
    policyName: string;
    policy: PolicyRepresentation;
  }): Promise<PolicyRepresentation> {
    try {
      const policyFound = await this.findPolicyByName({
        id: payload.id,
        name: payload.policyName,
      });
      await this.updatePolicy(
        {
          id: payload.id,
          policyId: policyFound.id!,
          type: payload.policy.type!,
        },
        payload.policy,
      );
      return await this.findPolicyByName({
        id: payload.id,
        name: payload.policyName,
      });
    } catch (error) {
      if (error instanceof NetworkError && error.response.status === 404) {
        return this.createPolicy(
          { id: payload.id, type: payload.policy.type! },
          payload.policy,
        );
      }
      throw error;
    }
  }

  /**
   * 授权 Scope
   * Scopes
   */
  public listAllScopes = this.makeRequest<
    { id: string; name?: string; deep?: boolean } & PaginatedQuery,
    ScopeRepresentation[]
  >({
    method: "GET",
    path: "/{id}/authz/resource-server/scope",
    urlParamKeys: ["id"],
  });

  /** 列出 Scope 关联资源 */
  public listAllResourcesByScope = this.makeRequest<
    { id: string; scopeId: string },
    ResourceRepresentation[]
  >({
    method: "GET",
    path: "/{id}/authz/resource-server/scope/{scopeId}/resources",
    urlParamKeys: ["id", "scopeId"],
  });

  /** 列出 Scope 关联权限 */
  public listAllPermissionsByScope = this.makeRequest<
    { id: string; scopeId: string },
    PolicyRepresentation[]
  >({
    method: "GET",
    path: "/{id}/authz/resource-server/scope/{scopeId}/permissions",
    urlParamKeys: ["id", "scopeId"],
  });

  /** 列出资源关联权限 */
  public listPermissionsByResource = this.makeRequest<
    { id: string; resourceId: string },
    PolicyRepresentation[]
  >({
    method: "GET",
    path: "/{id}/authz/resource-server/resource/{resourceId}/permissions",
    urlParamKeys: ["id", "resourceId"],
  });

  /** 列出资源关联 Scope */
  public listScopesByResource = this.makeRequest<
    { id: string; resourceName: string },
    { id: string; name: string }[]
  >({
    method: "GET",
    path: "/{id}/authz/resource-server/resource/{resourceName}/scopes",
    urlParamKeys: ["id", "resourceName"],
  });

  /** 列出 Scope 权限 */
  public listPermissionScope = this.makeRequest<
    {
      id: string;
      policyId?: string;
      name?: string;
      resource?: string;
    } & PaginatedQuery,
    PolicyRepresentation[]
  >({
    method: "GET",
    path: "/{id}/authz/resource-server/permission/scope",
    urlParamKeys: ["id"],
  });

  /** 创建授权 Scope */
  public createAuthorizationScope = this.makeUpdateRequest<
    { id: string },
    ScopeRepresentation
  >({
    method: "POST",
    path: "{id}/authz/resource-server/scope",
    urlParamKeys: ["id"],
  });

  /** 更新授权 Scope */
  public updateAuthorizationScope = this.makeUpdateRequest<
    { id: string; scopeId: string },
    ScopeRepresentation
  >({
    method: "PUT",
    path: "/{id}/authz/resource-server/scope/{scopeId}",
    urlParamKeys: ["id", "scopeId"],
  });

  /** 获取授权 Scope */
  public getAuthorizationScope = this.makeRequest<
    { id: string; scopeId: string },
    ScopeRepresentation
  >({
    method: "GET",
    path: "/{id}/authz/resource-server/scope/{scopeId}",
    urlParamKeys: ["id", "scopeId"],
  });

  /** 删除授权 Scope */
  public delAuthorizationScope = this.makeRequest<
    { id: string; scopeId: string },
    void
  >({
    method: "DELETE",
    path: "/{id}/authz/resource-server/scope/{scopeId}",
    urlParamKeys: ["id", "scopeId"],
  });

  /**
   * 授权权限
   * Permissions
   */
  public findPermissions = this.makeRequest<
    {
      id: string;
      name?: string;
      resource?: string;
      scope?: string;
    } & PaginatedQuery,
    PolicyRepresentation[]
  >({
    method: "GET",
    path: "{id}/authz/resource-server/permission",
    urlParamKeys: ["id"],
  });

  /** 创建权限 */
  public createPermission = this.makeUpdateRequest<
    { id: string; type: string },
    PolicyRepresentation,
    PolicyRepresentation
  >({
    method: "POST",
    path: "/{id}/authz/resource-server/permission/{type}",
    urlParamKeys: ["id", "type"],
  });

  /** 更新权限 */
  public updatePermission = this.makeUpdateRequest<
    { id: string; type: string; permissionId: string },
    PolicyRepresentation,
    void
  >({
    method: "PUT",
    path: "/{id}/authz/resource-server/permission/{type}/{permissionId}",
    urlParamKeys: ["id", "type", "permissionId"],
  });

  /** 删除权限 */
  public delPermission = this.makeRequest<
    { id: string; type: string; permissionId: string },
    void
  >({
    method: "DELETE",
    path: "/{id}/authz/resource-server/permission/{type}/{permissionId}",
    urlParamKeys: ["id", "type", "permissionId"],
  });

  /** 按 ID 获取权限 */
  public findOnePermission = this.makeRequest<
    { id: string; type: string; permissionId: string },
    PolicyRepresentation | undefined
  >({
    method: "GET",
    path: "/{id}/authz/resource-server/permission/{type}/{permissionId}",
    urlParamKeys: ["id", "type", "permissionId"],
  });

  /** 获取权限关联 Scope */
  public getAssociatedScopes = this.makeRequest<
    { id: string; permissionId: string },
    { id: string; name: string }[]
  >({
    method: "GET",
    path: "/{id}/authz/resource-server/policy/{permissionId}/scopes",
    urlParamKeys: ["id", "permissionId"],
  });

  /** 获取权限关联资源 */
  public getAssociatedResources = this.makeRequest<
    { id: string; permissionId: string },
    { _id: string; name: string }[]
  >({
    method: "GET",
    path: "/{id}/authz/resource-server/policy/{permissionId}/resources",
    urlParamKeys: ["id", "permissionId"],
  });

  /** 获取权限关联策略 */
  public getAssociatedPolicies = this.makeRequest<
    { id: string; permissionId: string },
    PolicyRepresentation[]
  >({
    method: "GET",
    path: "/{id}/authz/resource-server/policy/{permissionId}/associatedPolicies",
    urlParamKeys: ["id", "permissionId"],
  });

  /** 获取离线会话数量 */
  public getOfflineSessionCount = this.makeRequest<
    { id: string },
    { count: number }
  >({
    method: "GET",
    path: "/{id}/offline-session-count",
    urlParamKeys: ["id"],
  });

  /** 获取客户端安装配置 */
  public getInstallationProviders = this.makeRequest<
    { id: string; providerId: string },
    string
  >({
    method: "GET",
    path: "/{id}/installation/providers/{providerId}",
    urlParamKeys: ["id", "providerId"],
  });

  /** 推送令牌吊销 */
  public pushRevocation = this.makeRequest<{ id: string }, GlobalRequestResult>(
    {
      method: "POST",
      path: "/{id}/push-revocation",
      urlParamKeys: ["id"],
    },
  );

  /** 注册集群节点 */
  public addClusterNode = this.makeRequest<{ id: string; node: string }, void>({
    method: "POST",
    path: "/{id}/nodes",
    urlParamKeys: ["id"],
  });

  /** 删除集群节点 */
  public deleteClusterNode = this.makeRequest<
    { id: string; node: string },
    void
  >({
    method: "DELETE",
    path: "/{id}/nodes/{node}",
    urlParamKeys: ["id", "node"],
  });

  /** 测试集群节点可用性 */
  public testNodesAvailable = this.makeRequest<
    { id: string },
    GlobalRequestResult
  >({
    method: "GET",
    path: "/{id}/test-nodes-available",
    urlParamKeys: ["id"],
  });

  /** 获取证书/密钥信息 */
  public getKeyInfo = this.makeRequest<
    { id: string; attr: string },
    CertificateRepresentation
  >({
    method: "GET",
    path: "/{id}/certificates/{attr}",
    urlParamKeys: ["id", "attr"],
  });

  /** 生成证书/密钥 */
  public generateKey = this.makeRequest<
    { id: string; attr: string },
    CertificateRepresentation
  >({
    method: "POST",
    path: "/{id}/certificates/{attr}/generate",
    urlParamKeys: ["id", "attr"],
  });

  /** 下载密钥库 */
  public downloadKey = this.makeUpdateRequest<
    { id: string; attr: string },
    KeyStoreConfig,
    ArrayBuffer
  >({
    method: "POST",
    path: "/{id}/certificates/{attr}/download",
    urlParamKeys: ["id", "attr"],
    headers: {
      accept: "application/octet-stream",
    },
  });

  /** 生成并下载密钥库 */
  public generateAndDownloadKey = this.makeUpdateRequest<
    { id: string; attr: string },
    KeyStoreConfig,
    ArrayBuffer
  >({
    method: "POST",
    path: "/{id}/certificates/{attr}/generate-and-download",
    urlParamKeys: ["id", "attr"],
    headers: {
      accept: "application/octet-stream",
    },
  });

  /** 上传密钥库 */
  public uploadKey = this.makeUpdateRequest<
    { id: string; attr: string },
    FormData
  >({
    method: "POST",
    path: "/{id}/certificates/{attr}/upload",
    urlParamKeys: ["id", "attr"],
  });

  /** 上传证书 */
  public uploadCertificate = this.makeUpdateRequest<
    { id: string; attr: string },
    FormData
  >({
    method: "POST",
    path: "/{id}/certificates/{attr}/upload-certificate",
    urlParamKeys: ["id", "attr"],
  });

  /** 更新细粒度管理权限 */
  public updateFineGrainPermission = this.makeUpdateRequest<
    { id: string },
    ManagementPermissionReference,
    ManagementPermissionReference
  >({
    method: "PUT",
    path: "/{id}/management/permissions",
    urlParamKeys: ["id"],
  });

  /** 获取细粒度管理权限 */
  public listFineGrainPermissions = this.makeRequest<
    { id: string },
    ManagementPermissionReference
  >({
    method: "GET",
    path: "/{id}/management/permissions",
    urlParamKeys: ["id"],
  });

  constructor(client: KeycloakAdminClient) {
    super(client, {
      path: "/admin/realms/{realm}/clients",
      getUrlParams: () => ({
        realm: client.realmName,
      }),
      getBaseUrl: () => client.baseUrl,
    });

    this.#client = client;
    // 初始化 Clients v2 API
    this.#v2 = new ClientsV2(client);
  }

  /**
   * 获取当前 Realm 的 Clients v2 API 端点（实验性，需 enableExperimentalApis）。
   * Get the clients v2 API endpoint for the currently configured realm.
   * Returns a fluent API builder for client operations using the new versioned API.
   *
   * Note: This API is experimental and must be explicitly enabled by setting
   * `enableExperimentalApis: true` in the client configuration.
   *
   * @example
   * ```typescript
   * // Enable experimental APIs in client configuration
   * const kcAdminClient = new KeycloakAdminClient({
   *   baseUrl: "http://localhost:8080",
   *   enableExperimentalApis: true,
   * });
   *
   * // List all clients
   * const clients = await kcAdminClient.clients.v2().get();
   *
   * // Get a single client by clientId
   * const client = await kcAdminClient.clients.v2().byId("my-client").get();
   *
   * // Create a new client
   * await kcAdminClient.clients.v2().post({
   *   clientId: "my-client",
   *   protocol: "openid-connect",
   *   enabled: true,
   * });
   *
   * // Update a client
   * await kcAdminClient.clients.v2().byId("my-client").put({
   *   clientId: "my-client",
   *   protocol: "openid-connect",
   *   description: "Updated description",
   * });
   *
   * // Delete a client
   * await kcAdminClient.clients.v2().byId("my-client").delete();
   * ```
   *
   * @returns A promise that resolves to the clients v2 endpoint
   * @throws Error if experimental APIs are not enabled
   */
  v2() {
    if (!this.#client.enableExperimentalApis) {
      throw new Error(
        "The v2 API is experimental and not enabled. " +
          "To use it, set `enableExperimentalApis: true` in the KeycloakAdminClient configuration.",
      );
    }
    return this.#v2.api();
  }

  /**
   * 按名称查找协议映射器
   * Find single protocol mapper by name.
   */
  public async findProtocolMapperByName(payload: {
    realm?: string;
    id: string;
    name: string;
  }): Promise<ProtocolMapperRepresentation | undefined> {
    const allProtocolMappers = await this.listProtocolMappers({
      id: payload.id,
      ...(payload.realm ? { realm: payload.realm } : {}),
    });
    return allProtocolMappers.find((mapper) => mapper.name === payload.name);
  }
}
