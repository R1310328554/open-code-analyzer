import type UserRepresentation from "@keycloak/keycloak-admin-client/lib/defs/userRepresentation";

import {
  fetchAdminUI,
  postAdminUI,
} from "../../context/auth/admin-ui-endpoint";
import KeycloakAdminClient from "@keycloak/keycloak-admin-client";

/** 按资源类型与 id 定位 ui-ext 查询的通用参数。 */
type IDQuery = {
  id: string;
  type: string;
};

/** 支持分页与搜索的角色列表查询参数。 */
type PaginatingQuery = IDQuery & {
  first: number;
  max: number;
  search?: string;
};

type EffectiveClientRolesQuery = IDQuery;

/** ui-ext 端点路径及可选分页/搜索 query 的封装。 */
type Query = Partial<Omit<PaginatingQuery, "adminClient">> & {
  endpoint: string;
};

/** 客户端角色在 ui-ext 可用角色接口中的表示。 */
type ClientRole = {
  id: string;
  role: string;
  description?: string;
  client: string;
  clientId: string;
};

/** 有效角色（含是否客户端角色及所属客户端信息）。 */
export type EffectiveRole = {
  id: string;
  name: string;
  description?: string;
  clientRole: boolean;
  client?: string;
  clientId?: string;
};

/** 向 Admin UI 扩展 REST 发起 GET，路径为 /ui-ext/{endpoint}/{type}/{id}。 */
const fetchEndpoint = async (
  adminClient: KeycloakAdminClient,
  { id, type, first, max, search, endpoint }: Query,
): Promise<any> =>
  fetchAdminUI(
    adminClient,
    `/ui-ext/${endpoint}/${type}/${encodeURIComponent(id!)}`,
    {
      first: (first || 0).toString(),
      max: (max || 10).toString(),
      search: search || "",
    },
  );

/** 分页获取可分配给资源的客户端角色。 */
export const getAvailableClientRoles = (
  adminClient: KeycloakAdminClient,
  query: PaginatingQuery,
): Promise<ClientRole[]> =>
  fetchEndpoint(adminClient, { ...query, endpoint: "available-roles" });

/** 获取资源上已生效的客户端角色列表。 */
export const getEffectiveClientRoles = (
  adminClient: KeycloakAdminClient,
  query: EffectiveClientRolesQuery,
): Promise<ClientRole[]> =>
  fetchEndpoint(adminClient, { ...query, endpoint: "effective-roles" });

/** 获取领域与客户端角色的完整有效集合（含 clientRole 标记）。 */
export const getAllEffectiveRoles = (
  adminClient: KeycloakAdminClient,
  query: EffectiveClientRolesQuery,
): Promise<EffectiveRole[]> =>
  fetchEndpoint(adminClient, { ...query, endpoint: "effective-roles-all" });

type RoleRepresentation = {
  id: string;
  name: string;
  description?: string;
  composite: boolean;
  clientRole: boolean;
  containerId: string;
};

type ClientMappingRepresentation = {
  id: string;
  client: string;
  mappings: RoleRepresentation[];
};

/** 角色复合映射：领域角色与按客户端分组的客户端角色。 */
export type RoleMappingRepresentation = {
  realmMappings?: RoleRepresentation[];
  clientMappings?: Record<string, ClientMappingRepresentation>;
};

/** 通过 ui-ext 获取指定角色 id 的直接与复合映射详情。 */
export const getRoleMappings = async (
  adminClient: KeycloakAdminClient,
  id: string,
): Promise<RoleMappingRepresentation> =>
  fetchAdminUI(
    adminClient,
    `/ui-ext/role-mappings/roles/${encodeURIComponent(id)}`,
    {},
  );

/** 批量删除角色映射时提交的单条角色标识。 */
export type RoleDeleteRequest = {
  roleId: string;
  roleName: string;
  clientId?: string;
};

/** POST 至 ui-ext 批量移除用户/组/客户端等资源的角色映射。 */
export const deleteRoleMappings = async (
  adminClient: KeycloakAdminClient,
  type: string,
  id: string,
  roles: RoleDeleteRequest[],
): Promise<void> => {
  await postAdminUI(
    adminClient,
    `/ui-ext/role-mapping-delete/${type}/${encodeURIComponent(id)}`,
    roles,
  );
};

/** 用户搜索条件，与 Admin API 用户查询参数对齐。 */
type UserQuery = {
  lastName?: string;
  firstName?: string;
  email?: string;
  username?: string;
  emailVerified?: boolean;
  idpAlias?: string;
  idpUserId?: string;
  enabled?: boolean;
  briefRepresentation?: boolean;
  exact?: boolean;
  q?: string;
};

/** 附带暴力破解锁定状态的用户表示，供用户管理扩展列表使用。 */
export type BruteUser = UserRepresentation & {
  bruteForceStatus?: Record<string, object>;
};

/** 查询用户并返回暴力破解状态（ui-ext/brute-force-user）。 */
export const findUsers = (
  adminClient: KeycloakAdminClient,
  query: UserQuery,
): Promise<BruteUser[]> =>
  fetchAdminUI(
    adminClient,
    "ui-ext/brute-force-user",
    query as Record<string, string>,
  );

/** 分页查询引用某认证配置/流的使用方 id 列表。 */
export const fetchUsedBy = (
  adminClient: KeycloakAdminClient,
  query: PaginatingQuery,
): Promise<string[]> =>
  fetchEndpoint(adminClient, {
    ...query,
    endpoint: "authentication-management",
  });
