import Resource from "./resource.js";
import type AdminEventRepresentation from "../defs/adminEventRepresentation.js";
import type RealmRepresentation from "../defs/realmRepresentation.js";
import type {
  PartialImportRealmRepresentation,
  PartialImportResponse,
} from "../defs/realmRepresentation.js";
import type EventRepresentation from "../defs/eventRepresentation.js";
import type EventType from "../defs/eventTypes.js";
import type KeysMetadataRepresentation from "../defs/keyMetadataRepresentation.js";
import type ClientInitialAccessPresentation from "../defs/clientInitialAccessPresentation.js";
import type TestLdapConnectionRepresentation from "../defs/testLdapConnection.js";

import type { KeycloakAdminClient } from "../client.js";
import type { RealmEventsConfigRepresentation } from "../defs/realmEventsConfigRepresentation.js";
import type GlobalRequestResult from "../defs/globalRequestResult.js";
import type GroupRepresentation from "../defs/groupRepresentation.js";
import type { ManagementPermissionReference } from "../defs/managementPermissionReference.js";
import type ComponentTypeRepresentation from "../defs/componentTypeRepresentation.js";
import type { ClientSessionStat } from "../defs/clientSessionStat.js";

/** Realm Admin 资源：Realm 生命周期、导入导出、事件、会话、密钥、LDAP/SMTP 测试及本地化。 */
export class Realms extends Resource {
  /**
   * Realm 管理
   * Realm
   * https://www.keycloak.org/docs-api/11.0/rest-api/#_realms_admin_resource
   */

  public find = this.makeRequest<
    { briefRepresentation?: boolean },
    RealmRepresentation[]
  >({
    method: "GET",
  });

  /** 创建 */
  public create = this.makeRequest<RealmRepresentation, { realmName: string }>({
    method: "POST",
    returnResourceIdInLocationHeader: { field: "realmName" },
  });

  /** 按 ID 获取单个 */
  public findOne = this.makeRequest<
    { realm: string },
    RealmRepresentation | undefined
  >({
    method: "GET",
    path: "/{realm}",
    urlParamKeys: ["realm"],
    catchNotFound: true,
  });

  /** 更新 */
  public update = this.makeUpdateRequest<
    { realm: string },
    RealmRepresentation,
    void
  >({
    method: "PUT",
    path: "/{realm}",
    urlParamKeys: ["realm"],
  });

  /** 删除 */
  public del = this.makeRequest<{ realm: string }, void>({
    method: "DELETE",
    path: "/{realm}",
    urlParamKeys: ["realm"],
  });

  /** 部分导入 Realm 配置 */
  public partialImport = this.makeRequest<
    {
      realm: string;
      rep: PartialImportRealmRepresentation;
    },
    PartialImportResponse
  >({
    method: "POST",
    path: "/{realm}/partialImport",
    urlParamKeys: ["realm"],
    payloadKey: "rep",
  });

  /** 部分导出 Realm 配置 */
  public export = this.makeRequest<
    {
      realm: string;
      exportClients?: boolean;
      exportGroupsAndRoles?: boolean;
    },
    RealmRepresentation
  >({
    method: "POST",
    path: "/{realm}/partial-export",
    urlParamKeys: ["realm"],
    queryParamKeys: ["exportClients", "exportGroupsAndRoles"],
  });

  /** 获取 Realm 默认组 */
  public getDefaultGroups = this.makeRequest<
    { realm: string },
    GroupRepresentation[]
  >({
    method: "GET",
    path: "/{realm}/default-groups",
    urlParamKeys: ["realm"],
  });

  /** 添加默认组 */
  public addDefaultGroup = this.makeRequest<{ realm: string; id: string }>({
    method: "PUT",
    path: "/{realm}/default-groups/{id}",
    urlParamKeys: ["realm", "id"],
  });

  /** 移除默认组 */
  public removeDefaultGroup = this.makeRequest<{ realm: string; id: string }>({
    method: "DELETE",
    path: "/{realm}/default-groups/{id}",
    urlParamKeys: ["realm", "id"],
  });

  /** 按路径获取组 */
  public getGroupByPath = this.makeRequest<
    { path: string; realm: string },
    GroupRepresentation
  >({
    method: "GET",
    path: "/{realm}/group-by-path/{path}",
    urlParamKeys: ["realm", "path"],
  });

  /**
   * Get events Returns all events, or filters them based on URL query parameters listed here
   */
  public findEvents = this.makeRequest<
    {
      realm: string;
      client?: string;
      dateFrom?: string;
      dateTo?: string;
      first?: number;
      ipAddress?: string;
      max?: number;
      type?: EventType | EventType[];
      user?: string;
    },
    EventRepresentation[]
  >({
    method: "GET",
    path: "/{realm}/events",
    urlParamKeys: ["realm"],
    queryParamKeys: [
      "client",
      "dateFrom",
      "dateTo",
      "first",
      "ipAddress",
      "max",
      "type",
      "user",
    ],
  });

  /** 获取事件配置 */
  public getConfigEvents = this.makeRequest<
    { realm: string },
    RealmEventsConfigRepresentation
  >({
    method: "GET",
    path: "/{realm}/events/config",
    urlParamKeys: ["realm"],
  });

  /** 更新事件配置 */
  public updateConfigEvents = this.makeUpdateRequest<
    { realm: string },
    RealmEventsConfigRepresentation,
    void
  >({
    method: "PUT",
    path: "/{realm}/events/config",
    urlParamKeys: ["realm"],
  });

  /** 清除用户事件 */
  public clearEvents = this.makeRequest<{ realm: string }, void>({
    method: "DELETE",
    path: "/{realm}/events",
    urlParamKeys: ["realm"],
  });

  /** 清除管理事件 */
  public clearAdminEvents = this.makeRequest<{ realm: string }, void>({
    method: "DELETE",
    path: "/{realm}/admin-events",
    urlParamKeys: ["realm"],
  });

  /** 列出客户端注册策略提供者 */
  public getClientRegistrationPolicyProviders = this.makeRequest<
    { realm: string },
    ComponentTypeRepresentation[]
  >({
    method: "GET",
    path: "/{realm}/client-registration-policy/providers",
    urlParamKeys: ["realm"],
  });

  /** 列出客户端初始访问配置 */
  public getClientsInitialAccess = this.makeRequest<
    { realm: string },
    ClientInitialAccessPresentation[]
  >({
    method: "GET",
    path: "/{realm}/clients-initial-access",
    urlParamKeys: ["realm"],
  });

  /** 创建客户端初始访问配置 */
  public createClientsInitialAccess = this.makeUpdateRequest<
    { realm: string },
    { count?: number; expiration?: number },
    ClientInitialAccessPresentation
  >({
    method: "POST",
    path: "/{realm}/clients-initial-access",
    urlParamKeys: ["realm"],
  });

  /** 删除客户端初始访问配置 */
  public delClientsInitialAccess = this.makeRequest<
    { realm: string; id: string },
    void
  >({
    method: "DELETE",
    path: "/{realm}/clients-initial-access/{id}",
    urlParamKeys: ["realm", "id"],
  });

  /**
   * Remove a specific user session.
   */
  public removeSession = this.makeRequest<
    { realm: string; sessionId: string },
    void
  >({
    method: "DELETE",
    path: "/{realm}/sessions/{sessionId}",
    urlParamKeys: ["realm", "sessionId"],
    catchNotFound: true,
  });

  /**
   * Get admin events Returns all admin events, or filters events based on URL query parameters listed here
   */
  public findAdminEvents = this.makeRequest<
    {
      realm: string;
      authClient?: string;
      authIpAddress?: string;
      authRealm?: string;
      authUser?: string;
      dateFrom?: Date;
      dateTo?: Date;
      first?: number;
      max?: number;
      operationTypes?: string;
      resourcePath?: string;
      resourceTypes?: string;
    },
    AdminEventRepresentation[]
  >({
    method: "GET",
    path: "/{realm}/admin-events",
    urlParamKeys: ["realm"],
    queryParamKeys: [
      "authClient",
      "authIpAddress",
      "authRealm",
      "authUser",
      "dateFrom",
      "dateTo",
      "max",
      "first",
      "operationTypes",
      "resourcePath",
      "resourceTypes",
    ],
  });

  /**
   * 用户管理细粒度权限
   * Users management permissions
   */
  public getUsersManagementPermissions = this.makeRequest<
    { realm: string },
    ManagementPermissionReference
  >({
    method: "GET",
    path: "/{realm}/users-management-permissions",
    urlParamKeys: ["realm"],
  });

  /** 更新用户管理细粒度权限 */
  public updateUsersManagementPermissions = this.makeRequest<
    { realm: string; enabled: boolean },
    ManagementPermissionReference
  >({
    method: "PUT",
    path: "/{realm}/users-management-permissions",
    urlParamKeys: ["realm"],
  });

  /**
   * 会话管理
   * Sessions
   */
  public getClientSessionStats = this.makeRequest<
    { realm: string },
    ClientSessionStat[]
  >({
    method: "GET",
    path: "/{realm}/client-session-stats",
    urlParamKeys: ["realm"],
  });

  /** 登出 Realm 内所有用户 */
  public logoutAll = this.makeRequest<{ realm: string }, void>({
    method: "POST",
    path: "/{realm}/logout-all",
    urlParamKeys: ["realm"],
  });

  /** 删除指定会话 */
  public deleteSession = this.makeRequest<
    { realm: string; session: string; isOffline: boolean },
    void
  >({
    method: "DELETE",
    path: "/{realm}/sessions/{session}",
    urlParamKeys: ["realm", "session"],
    queryParamKeys: ["isOffline"],
  });

  /** 推送令牌吊销 */
  public pushRevocation = this.makeRequest<
    { realm: string },
    GlobalRequestResult
  >({
    method: "POST",
    path: "/{realm}/push-revocation",
    urlParamKeys: ["realm"],
    ignoredKeys: ["realm"],
  });

  /** 获取 Realm 密钥元数据 */
  public getKeys = this.makeRequest<
    { realm: string },
    KeysMetadataRepresentation
  >({
    method: "GET",
    path: "/{realm}/keys",
    urlParamKeys: ["realm"],
  });

  /** 测试 LDAP 连接 */
  public testLDAPConnection = this.makeUpdateRequest<
    { realm: string },
    TestLdapConnectionRepresentation
  >({
    method: "POST",
    path: "/{realm}/testLDAPConnection",
    urlParamKeys: ["realm"],
  });

  /** 测试 SMTP 连接 */
  public testSMTPConnection = this.makeUpdateRequest<
    { realm: string },
    Record<string, string | number>
  >({
    method: "POST",
    path: "/{realm}/testSMTPConnection",
    urlParamKeys: ["realm"],
  });

  /** 探测 LDAP 服务器能力 */
  public ldapServerCapabilities = this.makeUpdateRequest<
    { realm: string },
    TestLdapConnectionRepresentation
  >({
    method: "POST",
    path: "/{realm}/ldap-server-capabilities",
    urlParamKeys: ["realm"],
  });

  /** 获取 Realm 支持的语言区域 */
  public getRealmSpecificLocales = this.makeRequest<
    { realm: string },
    string[]
  >({
    method: "GET",
    path: "/{realm}/localization",
    urlParamKeys: ["realm"],
  });

  /** 获取 Realm 本地化文本 */
  public getRealmLocalizationTexts = this.makeRequest<
    { realm: string; selectedLocale: string; first?: number; max?: number },
    Record<string, string>
  >({
    method: "GET",
    path: "/{realm}/localization/{selectedLocale}",
    urlParamKeys: ["realm", "selectedLocale"],
  });

  /** 添加或更新本地化键值 */
  public addLocalization = this.makeUpdateRequest<
    { realm: string; selectedLocale: string; key: string },
    string,
    void
  >({
    method: "PUT",
    path: "/{realm}/localization/{selectedLocale}/{key}",
    urlParamKeys: ["realm", "selectedLocale", "key"],
    headers: { "content-type": "text/plain" },
  });

  /** 删除 Realm 本地化文本 */
  public deleteRealmLocalizationTexts = this.makeRequest<
    { realm: string; selectedLocale: string; key?: string },
    void
  >({
    method: "DELETE",
    path: "/{realm}/localization/{selectedLocale}/{key}",
    urlParamKeys: ["realm", "selectedLocale", "key"],
  });

  constructor(client: KeycloakAdminClient) {
    super(client, {
      path: "/admin/realms",
      getBaseUrl: () => client.baseUrl,
    });
  }
}
