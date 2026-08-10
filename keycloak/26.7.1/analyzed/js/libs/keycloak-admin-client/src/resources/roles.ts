import Resource from "./resource.js";
import type RoleRepresentation from "../defs/roleRepresentation.js";
import type UserRepresentation from "../defs/userRepresentation.js";
import type { KeycloakAdminClient } from "../client.js";
import type { ManagementPermissionReference } from "../defs/managementPermissionReference.js";

/** 角色列表查询参数 */
export interface RoleQuery {
  first?: number;
  max?: number;
  search?: string;
  briefRepresentation?: boolean;
}

/** Realm 角色 Admin 资源：按名称/ID 管理 Realm 角色及复合角色关系。 */
export class Roles extends Resource<{ realm?: string }> {
  /**
   * Realm 角色
   * Realm roles
   */

  public find = this.makeRequest<RoleQuery, RoleRepresentation[]>({
    method: "GET",
    path: "/roles",
  });

  /** 创建 */
  public create = this.makeRequest<RoleRepresentation, { roleName: string }>({
    method: "POST",
    path: "/roles",
    returnResourceIdInLocationHeader: { field: "roleName" },
  });

  /**
   * 按名称操作角色
   * Roles by name
   */

  public findOneByName = this.makeRequest<
    { name: string },
    RoleRepresentation | undefined
  >({
    method: "GET",
    path: "/roles/{name}",
    urlParamKeys: ["name"],
    catchNotFound: true,
  });

  /** 按名称更新 */
  public updateByName = this.makeUpdateRequest<
    { name: string },
    RoleRepresentation,
    void
  >({
    method: "PUT",
    path: "/roles/{name}",
    urlParamKeys: ["name"],
  });

  /** 按名称删除 */
  public delByName = this.makeRequest<{ name: string }, void>({
    method: "DELETE",
    path: "/roles/{name}",
    urlParamKeys: ["name"],
  });

  /** 列出拥有指定角色的用户 */
  public findUsersWithRole = this.makeRequest<
    {
      name: string;
      briefRepresentation?: boolean;
      first?: number;
      max?: number;
    },
    UserRepresentation[]
  >({
    method: "GET",
    path: "/roles/{name}/users",
    urlParamKeys: ["name"],
    catchNotFound: true,
  });

  /**
   * 按 ID 操作角色
   * Roles by id
   */

  public findOneById = this.makeRequest<
    { id: string },
    RoleRepresentation | undefined
  >({
    method: "GET",
    path: "/roles-by-id/{id}",
    urlParamKeys: ["id"],
    catchNotFound: true,
  });

  /** 添加复合角色 */
  public createComposite = this.makeUpdateRequest<
    { roleId: string },
    RoleRepresentation[],
    void
  >({
    method: "POST",
    path: "/roles-by-id/{roleId}/composites",
    urlParamKeys: ["roleId"],
  });

  /** 获取复合角色列表 */
  public getCompositeRoles = this.makeRequest<
    { id: string; search?: string; first?: number; max?: number },
    RoleRepresentation[]
  >({
    method: "GET",
    path: "/roles-by-id/{id}/composites",
    urlParamKeys: ["id"],
  });

  /** 获取 Realm 复合角色 */
  public getCompositeRolesForRealm = this.makeRequest<
    { id: string },
    RoleRepresentation[]
  >({
    method: "GET",
    path: "/roles-by-id/{id}/composites/realm",
    urlParamKeys: ["id"],
  });

  /** 获取客户端复合角色 */
  public getCompositeRolesForClient = this.makeRequest<
    { id: string; clientId: string },
    RoleRepresentation[]
  >({
    method: "GET",
    path: "/roles-by-id/{id}/composites/clients/{clientId}",
    urlParamKeys: ["id", "clientId"],
  });

  /** 删除复合角色 */
  public delCompositeRoles = this.makeUpdateRequest<
    { id: string },
    RoleRepresentation[],
    void
  >({
    method: "DELETE",
    path: "/roles-by-id/{id}/composites",
    urlParamKeys: ["id"],
  });

  /** 按 ID 更新 */
  public updateById = this.makeUpdateRequest<
    { id: string },
    RoleRepresentation,
    void
  >({
    method: "PUT",
    path: "/roles-by-id/{id}",
    urlParamKeys: ["id"],
  });

  /** 按 ID 删除 */
  public delById = this.makeRequest<{ id: string }, void>({
    method: "DELETE",
    path: "/roles-by-id/{id}",
    urlParamKeys: ["id"],
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
    path: "/roles-by-id/{id}/management/permissions",
    urlParamKeys: ["id"],
  });

  /** 获取细粒度管理权限 */
  public listPermissions = this.makeRequest<
    { id: string },
    ManagementPermissionReference
  >({
    method: "GET",
    path: "/roles-by-id/{id}/management/permissions",
    urlParamKeys: ["id"],
  });

  constructor(client: KeycloakAdminClient) {
    super(client, {
      path: "/admin/realms/{realm}",
      getUrlParams: () => ({
        realm: client.realmName,
      }),
      getBaseUrl: () => client.baseUrl,
    });
  }
}
