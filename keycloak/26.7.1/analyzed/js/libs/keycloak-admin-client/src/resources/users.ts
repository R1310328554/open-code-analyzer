import type { KeycloakAdminClient } from "../client.js";
import type CredentialRepresentation from "../defs/credentialRepresentation.js";
import type FederatedIdentityRepresentation from "../defs/federatedIdentityRepresentation.js";
import type GroupRepresentation from "../defs/groupRepresentation.js";
import type MappingsRepresentation from "../defs/mappingsRepresentation.js";
import type { RequiredActionAlias } from "../defs/requiredActionProviderRepresentation.js";
import type RoleRepresentation from "../defs/roleRepresentation.js";
import type { RoleMappingPayload } from "../defs/roleRepresentation.js";
import type UserConsentRepresentation from "../defs/userConsentRepresentation.js";
import type {
  UserProfileConfig,
  UserProfileMetadata,
} from "../defs/userProfileMetadata.js";
import type UserRepresentation from "../defs/userRepresentation.js";
import type UserSessionRepresentation from "../defs/userSessionRepresentation.js";
import type UserVerifiableCredentialRepresentation from "../defs/userVerifiableCredentialRepresentation.js";
import type IssuedUserVerifiableCredentialRepresentation from "../defs/issuedUserVerifiableCredentialRepresentation.js";
import type VerifiableCredentialOfferActionConfigRepresentation from "../defs/verifiableCredentialOfferActionConfigRepresentation.js";
import Resource from "./resource.js";

/** 搜索查询参数 */
export interface SearchQuery {
  search?: string;
}

/** 分页参数 */
export interface PaginationQuery {
  first?: number;
  max?: number;
}

interface UserBaseQuery {
  email?: string;
  firstName?: string;
  lastName?: string;
  username?: string;
  q?: string;
}

/** 用户列表查询参数 */
export interface UserQuery extends PaginationQuery, SearchQuery, UserBaseQuery {
  exact?: boolean;
  [key: string]: string | number | undefined | boolean;
}

/** 用户 Admin 资源：用户 CRUD、角色/组映射、凭据、联邦身份、会话、可验证凭据及 Profile。 */
export class Users extends Resource<{ realm?: string }> {
  /** 查询列表 */
  public find = this.makeRequest<UserQuery, UserRepresentation[]>({
    method: "GET",
  });

  /** 创建 */
  public create = this.makeRequest<UserRepresentation, { id: string }>({
    method: "POST",
    returnResourceIdInLocationHeader: { field: "id" },
  });

  /**
   * 单个用户/组
   * Single user
   */

  public findOne = this.makeRequest<
    { id: string; userProfileMetadata?: boolean },
    UserRepresentation | undefined
  >({
    method: "GET",
    path: "/{id}",
    urlParamKeys: ["id"],
    catchNotFound: true,
  });

  /** 更新 */
  public update = this.makeUpdateRequest<
    { id: string },
    UserRepresentation,
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

  /** 统计数量 */
  public count = this.makeRequest<UserBaseQuery & SearchQuery, number>({
    method: "GET",
    path: "/count",
  });

  /** 获取用户 Profile 配置 */
  public getProfile = this.makeRequest<{}, UserProfileConfig>({
    method: "GET",
    path: "/profile",
  });

  /** 更新用户 Profile 配置 */
  public updateProfile = this.makeRequest<UserProfileConfig, UserProfileConfig>(
    {
      method: "PUT",
      path: "/profile",
    },
  );

  /** 获取用户 Profile 元数据 */
  public getProfileMetadata = this.makeRequest<{}, UserProfileMetadata>({
    method: "GET",
    path: "/profile/metadata",
  });

  /**
   * role mappings
   */

  public listRoleMappings = this.makeRequest<
    { id: string },
    MappingsRepresentation
  >({
    method: "GET",
    path: "/{id}/role-mappings",
    urlParamKeys: ["id"],
  });

  /** 添加 Realm 角色映射 */
  public addRealmRoleMappings = this.makeRequest<
    { id: string; roles: RoleMappingPayload[] },
    void
  >({
    method: "POST",
    path: "/{id}/role-mappings/realm",
    urlParamKeys: ["id"],
    payloadKey: "roles",
  });

  /** 列出 Realm 角色映射 */
  public listRealmRoleMappings = this.makeRequest<
    { id: string },
    RoleRepresentation[]
  >({
    method: "GET",
    path: "/{id}/role-mappings/realm",
    urlParamKeys: ["id"],
  });

  /** 删除 Realm 角色映射 */
  public delRealmRoleMappings = this.makeRequest<
    { id: string; roles: RoleMappingPayload[] },
    void
  >({
    method: "DELETE",
    path: "/{id}/role-mappings/realm",
    urlParamKeys: ["id"],
    payloadKey: "roles",
  });

  /** 列出可分配 Realm 角色映射 */
  public listAvailableRealmRoleMappings = this.makeRequest<
    { id: string },
    RoleRepresentation[]
  >({
    method: "GET",
    path: "/{id}/role-mappings/realm/available",
    urlParamKeys: ["id"],
  });

  // 获取有效 Realm 级角色映射（递归展开复合角色）
  /** 列出 Realm 复合角色映射 */
  public listCompositeRealmRoleMappings = this.makeRequest<
    { id: string },
    RoleRepresentation[]
  >({
    method: "GET",
    path: "/{id}/role-mappings/realm/composite",
    urlParamKeys: ["id"],
  });

  /**
   * 客户端角色映射
   * Client role mappings
   * https://www.keycloak.org/docs-api/11.0/rest-api/#_client_role_mappings_resource
   */

  public listClientRoleMappings = this.makeRequest<
    { id: string; clientUniqueId: string },
    RoleRepresentation[]
  >({
    method: "GET",
    path: "/{id}/role-mappings/clients/{clientUniqueId}",
    urlParamKeys: ["id", "clientUniqueId"],
  });

  /** 添加客户端角色映射 */
  public addClientRoleMappings = this.makeRequest<
    { id: string; clientUniqueId: string; roles: RoleMappingPayload[] },
    void
  >({
    method: "POST",
    path: "/{id}/role-mappings/clients/{clientUniqueId}",
    urlParamKeys: ["id", "clientUniqueId"],
    payloadKey: "roles",
  });

  /** 删除客户端角色映射 */
  public delClientRoleMappings = this.makeRequest<
    { id: string; clientUniqueId: string; roles: RoleMappingPayload[] },
    void
  >({
    method: "DELETE",
    path: "/{id}/role-mappings/clients/{clientUniqueId}",
    urlParamKeys: ["id", "clientUniqueId"],
    payloadKey: "roles",
  });

  /** 列出可分配客户端角色映射 */
  public listAvailableClientRoleMappings = this.makeRequest<
    { id: string; clientUniqueId: string },
    RoleRepresentation[]
  >({
    method: "GET",
    path: "/{id}/role-mappings/clients/{clientUniqueId}/available",
    urlParamKeys: ["id", "clientUniqueId"],
  });

  /** 列出客户端复合角色映射 */
  public listCompositeClientRoleMappings = this.makeRequest<
    { id: string; clientUniqueId: string },
    RoleRepresentation[]
  >({
    method: "GET",
    path: "/{id}/role-mappings/clients/{clientUniqueId}/composite",
    urlParamKeys: ["id", "clientUniqueId"],
  });

  /**
   * Send a update account email to the user
   * an email contains a link the user can click to perform a set of required actions.
   */

  public executeActionsEmail = this.makeRequest<
    {
      id: string;
      clientId?: string;
      lifespan?: number;
      redirectUri?: string;
      actions?: (RequiredActionAlias | string)[];
    },
    void
  >({
    method: "PUT",
    path: "/{id}/execute-actions-email",
    urlParamKeys: ["id"],
    payloadKey: "actions",
    queryParamKeys: ["lifespan", "redirectUri", "clientId"],
    keyTransform: {
      clientId: "client_id",
      redirectUri: "redirect_uri",
    },
  });

  /**
   * 用户组关联
   * Group
   */

  public listGroups = this.makeRequest<
    { id: string; briefRepresentation?: boolean } & PaginationQuery &
      SearchQuery,
    GroupRepresentation[]
  >({
    method: "GET",
    path: "/{id}/groups",
    urlParamKeys: ["id"],
  });

  /** 将用户加入组 */
  public addToGroup = this.makeRequest<{ id: string; groupId: string }, string>(
    {
      method: "PUT",
      path: "/{id}/groups/{groupId}",
      urlParamKeys: ["id", "groupId"],
    },
  );

  /** 将用户从组移除 */
  public delFromGroup = this.makeRequest<
    { id: string; groupId: string },
    string
  >({
    method: "DELETE",
    path: "/{id}/groups/{groupId}",
    urlParamKeys: ["id", "groupId"],
  });

  /** 统计用户所属组数量 */
  public countGroups = this.makeRequest<
    { id: string; search?: string },
    { count: number }
  >({
    method: "GET",
    path: "/{id}/groups/count",
    urlParamKeys: ["id"],
  });

  /**
   * 联邦身份
   * Federated Identity
   */

  public listFederatedIdentities = this.makeRequest<
    { id: string },
    FederatedIdentityRepresentation[]
  >({
    method: "GET",
    path: "/{id}/federated-identity",
    urlParamKeys: ["id"],
  });

  /** 添加联邦身份关联 */
  public addToFederatedIdentity = this.makeRequest<
    {
      id: string;
      federatedIdentityId: string;
      federatedIdentity: FederatedIdentityRepresentation;
    },
    void
  >({
    method: "POST",
    path: "/{id}/federated-identity/{federatedIdentityId}",
    urlParamKeys: ["id", "federatedIdentityId"],
    payloadKey: "federatedIdentity",
  });

  /** 删除联邦身份关联 */
  public delFromFederatedIdentity = this.makeRequest<
    { id: string; federatedIdentityId: string },
    void
  >({
    method: "DELETE",
    path: "/{id}/federated-identity/{federatedIdentityId}",
    urlParamKeys: ["id", "federatedIdentityId"],
  });

  /**
   * remove totp
   */
  public removeTotp = this.makeRequest<{ id: string }, void>({
    method: "PUT",
    path: "/{id}/remove-totp",
    urlParamKeys: ["id"],
  });

  /**
   * reset password
   */
  public resetPassword = this.makeRequest<
    { id: string; credential: CredentialRepresentation },
    void
  >({
    method: "PUT",
    path: "/{id}/reset-password",
    urlParamKeys: ["id"],
    payloadKey: "credential",
  });

  /** 获取用户存储支持的凭据类型 */
  public getUserStorageCredentialTypes = this.makeRequest<
    { id: string },
    string[]
  >({
    method: "GET",
    path: "/{id}/configured-user-storage-credential-types",
    urlParamKeys: ["id"],
  });

  /**
   * get user credentials
   */
  public getCredentials = this.makeRequest<
    { id: string },
    CredentialRepresentation[]
  >({
    method: "GET",
    path: "/{id}/credentials",
    urlParamKeys: ["id"],
  });

  /**
   * delete user credentials
   */
  public deleteCredential = this.makeRequest<
    { id: string; credentialId: string },
    void
  >({
    method: "DELETE",
    path: "/{id}/credentials/{credentialId}",
    urlParamKeys: ["id", "credentialId"],
  });

  /**
   * update a credential label for a user
   */
  public updateCredentialLabel = this.makeUpdateRequest<
    { id: string; credentialId: string },
    string,
    void
  >({
    method: "PUT",
    path: "/{id}/credentials/{credentialId}/userLabel",
    urlParamKeys: ["id", "credentialId"],
    headers: { "content-type": "text/plain" },
  });

  // 将凭据移动到另一凭据之后
  /** 将凭据下移至指定凭据之后 */
  public moveCredentialPositionDown = this.makeRequest<
    {
      id: string;
      credentialId: string;
      newPreviousCredentialId: string;
    },
    void
  >({
    method: "POST",
    path: "/{id}/credentials/{credentialId}/moveAfter/{newPreviousCredentialId}",
    urlParamKeys: ["id", "credentialId", "newPreviousCredentialId"],
  });

  // 将凭据移至用户凭据列表首位
  /** 将凭据移至列表首位 */
  public moveCredentialPositionUp = this.makeRequest<
    {
      id: string;
      credentialId: string;
    },
    void
  >({
    method: "POST",
    path: "/{id}/credentials/{credentialId}/moveToFirst",
    urlParamKeys: ["id", "credentialId"],
  });

  /**
   * send verify email
   */
  public sendVerifyEmail = this.makeRequest<
    { id: string; clientId?: string; redirectUri?: string },
    void
  >({
    method: "PUT",
    path: "/{id}/send-verify-email",
    urlParamKeys: ["id"],
    queryParamKeys: ["clientId", "redirectUri"],
    keyTransform: {
      clientId: "client_id",
      redirectUri: "redirect_uri",
    },
  });

  /**
   * list user sessions
   */
  public listSessions = this.makeRequest<
    { id: string },
    UserSessionRepresentation[]
  >({
    method: "GET",
    path: "/{id}/sessions",
    urlParamKeys: ["id"],
  });

  /**
   * list offline sessions associated with the user and client
   */
  public listOfflineSessions = this.makeRequest<
    { id: string; clientId: string },
    UserSessionRepresentation[]
  >({
    method: "GET",
    path: "/{id}/offline-sessions/{clientId}",
    urlParamKeys: ["id", "clientId"],
  });

  /**
   * logout user from all sessions
   */
  public logout = this.makeRequest<{ id: string }, void>({
    method: "POST",
    path: "/{id}/logout",
    urlParamKeys: ["id"],
  });

  /**
   * list consents granted by the user
   */
  public listConsents = this.makeRequest<
    { id: string },
    UserConsentRepresentation[]
  >({
    method: "GET",
    path: "/{id}/consents",
    urlParamKeys: ["id"],
  });

  /** 模拟登录指定用户 */
  public impersonation = this.makeUpdateRequest<
    { id: string },
    { user: string; realm: string },
    Record<string, any>
  >({
    method: "POST",
    path: "/{id}/impersonation",
    urlParamKeys: ["id"],
  });

  /**
   * revoke consent and offline tokens for particular client from user
   */
  public revokeConsent = this.makeRequest<
    { id: string; clientId: string },
    void
  >({
    method: "DELETE",
    path: "/{id}/consents/{clientId}",
    urlParamKeys: ["id", "clientId"],
  });

  /**
   * list verifiable credentials for a user
   */
  public listVerifiableCredentials = this.makeRequest<
    { id: string },
    UserVerifiableCredentialRepresentation[]
  >({
    method: "GET",
    path: "/{id}/vc/credentials",
    urlParamKeys: ["id"],
  });

  /**
   * create a verifiable credential for a user
   */
  public createVerifiableCredential = this.makeUpdateRequest<
    { id: string },
    UserVerifiableCredentialRepresentation,
    UserVerifiableCredentialRepresentation
  >({
    method: "POST",
    path: "/{id}/vc/credentials",
    urlParamKeys: ["id"],
  });

  /**
   * revoke a verifiable credential from a user
   */
  public revokeVerifiableCredential = this.makeRequest<
    { id: string; credentialScopeName: string },
    void
  >({
    method: "DELETE",
    path: "/{id}/vc/credentials/{credentialScopeName}",
    urlParamKeys: ["id", "credentialScopeName"],
  });

  /**
   * update a verifiable credential for a user (refreshes user attributes snapshot and increments revision)
   */
  public updateVerifiableCredential = this.makeRequest<
    { id: string; credentialScopeName: string },
    UserVerifiableCredentialRepresentation
  >({
    method: "PUT",
    path: "/{id}/vc/credentials/{credentialScopeName}",
    urlParamKeys: ["id", "credentialScopeName"],
  });

  /**
   * Send credential offer of specified verifiable credential to this user by email.
   * An email contains a link the user can click to see the page with credential offer, from which he can obtain verifiable credential to his wallet.
   */

  public sendVerifiableCredentialOffer = this.makeUpdateRequest<
    {
      id: string;
      clientId?: string;
      lifespan?: number;
      redirectUri?: string;
    },
    VerifiableCredentialOfferActionConfigRepresentation,
    void
  >({
    method: "PUT",
    path: "/{id}/vc/credentials/send-credential-offer",
    urlParamKeys: ["id"],
    queryParamKeys: ["lifespan", "redirectUri", "clientId"],
    keyTransform: {
      clientId: "client_id",
      redirectUri: "redirect_uri",
    },
  });

  /**
   * list issued verifiable credentials for a user
   */
  public listIssuedVerifiableCredentials = this.makeRequest<
    { id: string },
    IssuedUserVerifiableCredentialRepresentation[]
  >({
    method: "GET",
    path: "/{id}/vc/issued-credentials",
    urlParamKeys: ["id"],
  });

  /**
   * revoke an issued verifiable credential
   */
  public revokeIssuedVerifiableCredential = this.makeRequest<
    { id: string; credentialId: string },
    void
  >({
    method: "DELETE",
    path: "/{id}/vc/issued-credentials/{credentialId}",
    urlParamKeys: ["id", "credentialId"],
  });

  /** 获取用户未托管属性 */
  public getUnmanagedAttributes = this.makeRequest<
    { id: string },
    Record<string, string[]>
  >({
    method: "GET",
    path: "/{id}/unmanagedAttributes",
    urlParamKeys: ["id"],
  });

  constructor(client: KeycloakAdminClient) {
    super(client, {
      path: "/admin/realms/{realm}/users",
      getUrlParams: () => ({
        realm: client.realmName,
      }),
      getBaseUrl: () => client.baseUrl,
    });
  }
}
