import type { KeycloakAdminClient } from "../client.js";
import type { PaginationQuery, SearchQuery } from "./users.js";
import type GroupRepresentation from "../defs/groupRepresentation.js";
import type { ManagementPermissionReference } from "../defs/managementPermissionReference.js";
import type MappingsRepresentation from "../defs/mappingsRepresentation.js";
import type RoleRepresentation from "../defs/roleRepresentation.js";
import type { RoleMappingPayload } from "../defs/roleRepresentation.js";
import type UserRepresentation from "../defs/userRepresentation.js";
import Resource from "./resource.js";

interface Query {
  q?: string;
  search?: string;
  exact?: boolean;
}

interface PaginatedQuery {
  first?: number;
  max?: number;
}

interface SummarizedQuery {
  briefRepresentation?: boolean;
  populateHierarchy?: boolean;
}

export type GroupQuery = Query & PaginatedQuery & SummarizedQuery;
export type SubGroupQuery = Query &
  PaginatedQuery &
  SummarizedQuery & {
    parentId: string;
  };

/** 组数量统计查询参数 */
export interface GroupCountQuery {
  search?: string;
  top?: boolean;
}

/** 用户组 Admin 资源：组 CRUD、层级子组、成员、角色映射；支持组织上下文。 */
export class Groups extends Resource<{ realm?: string }> {
  /** 查询列表 */
  public find = this.makeRequest<GroupQuery, GroupRepresentation[]>({
    method: "GET",
    queryParamKeys: [
      "search",
      "q",
      "exact",
      "briefRepresentation",
      "populateHierarchy",
      "first",
      "max",
    ],
  });

  /** 创建 */
  public create = this.makeRequest<GroupRepresentation, { id: string }>({
    method: "POST",
    returnResourceIdInLocationHeader: { field: "id" },
  });

  /** 更新根级组信息 */
  public updateRoot = this.makeRequest<GroupRepresentation, void>({
    method: "POST",
  });

  /**
   * 单个用户/组
   * Single user
   */

  public findOne = this.makeRequest<
    { id: string },
    GroupRepresentation | undefined
  >({
    method: "GET",
    path: "/{id}",
    urlParamKeys: ["id"],
    catchNotFound: true,
  });

  /** 更新 */
  public update = this.makeUpdateRequest<
    { id: string },
    GroupRepresentation,
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
  public count = this.makeRequest<GroupCountQuery, { count: number }>({
    method: "GET",
    path: "/count",
  });

  /**
   * Creates a child group on the specified parent group. If the group already exists, then an error is returned.
   */
  public createChildGroup = this.makeUpdateRequest<
    { id: string },
    Omit<GroupRepresentation, "id">,
    { id: string }
  >({
    method: "POST",
    path: "/{id}/children",
    urlParamKeys: ["id"],
    returnResourceIdInLocationHeader: { field: "id" },
  });

  /**
   * Updates a child group on the specified parent group. If the group doesn’t exist, then an error is returned.
   * Can be used to move a group from one parent to another.
   */
  public updateChildGroup = this.makeUpdateRequest<
    { id: string },
    GroupRepresentation,
    void
  >({
    method: "POST",
    path: "/{id}/children",
    urlParamKeys: ["id"],
  });

  /**
   * Finds all subgroups on the specified parent group matching the provided parameters.
   */
  public listSubGroups = this.makeRequest<SubGroupQuery, GroupRepresentation[]>(
    {
      method: "GET",
      path: "/{parentId}/children",
      urlParamKeys: ["parentId"],
      queryParamKeys: ["search", "first", "max", "briefRepresentation"],
      catchNotFound: true,
    },
  );

  /**
   * 组成员
   * Members
   */

  public listMembers = this.makeRequest<
    { id: string; first?: number; max?: number; briefRepresentation?: boolean },
    UserRepresentation[]
  >({
    method: "GET",
    path: "/{id}/members",
    urlParamKeys: ["id"],
    catchNotFound: true,
  });

  /**
   * 角色映射
   * Role mappings
   * https://www.keycloak.org/docs-api/11.0/rest-api/#_role_mapper_resource
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

  /** 列出组织成员所属组 */
  public listOrgGroups = this.makeRequest<
    { id: string; briefRepresentation?: boolean } & PaginationQuery &
      SearchQuery,
    GroupRepresentation[]
  >({
    method: "GET",
    path: "/../members/{id}/groups",
    urlParamKeys: ["id"],
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
   * 细粒度管理权限
   * Authorization permissions
   */
  public updatePermission = this.makeUpdateRequest<
    { id: string },
    ManagementPermissionReference,
    ManagementPermissionReference
  >({
    method: "PUT",
    path: "/{id}/management/permissions",
    urlParamKeys: ["id"],
  });

  /** 获取细粒度管理权限 */
  public listPermissions = this.makeRequest<
    { id: string },
    ManagementPermissionReference
  >({
    method: "GET",
    path: "/{id}/management/permissions",
    urlParamKeys: ["id"],
  });

  /** 将用户加入组织组 */
  public addMemberToOrgGroup = this.makeRequest<
    { groupId: string; userId: string },
    void
  >({
    method: "PUT",
    path: "/{groupId}/members/{userId}",
    urlParamKeys: ["groupId", "userId"],
  });

  /** 将用户从组织组移除 */
  public removeMemberFromOrgGroup = this.makeRequest<
    { groupId: string; userId: string },
    void
  >({
    method: "DELETE",
    path: "/{groupId}/members/{userId}",
    urlParamKeys: ["groupId", "userId"],
  });

  #orgId?: string;

  public getOrgId(): string | undefined {
    return this.#orgId;
  }

  public isOrgGroups(): boolean {
    return !!this.#orgId;
  }

  constructor(client: KeycloakAdminClient, orgId?: string) {
    super(client, {
      path: `/admin/realms/{realm}/${orgId ? "organizations/{orgId}/" : ""}groups`,
      getUrlParams: () => ({
        realm: client.realmName,
        orgId,
      }),
      getBaseUrl: () => client.baseUrl,
    });
    this.#orgId = orgId;
  }
}
