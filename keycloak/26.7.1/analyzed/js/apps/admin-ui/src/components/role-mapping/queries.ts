import type KeycloakAdminClient from "@keycloak/keycloak-admin-client";
import type MappingsRepresentation from "@keycloak/keycloak-admin-client/lib/defs/mappingsRepresentation";
import type RoleRepresentation from "@keycloak/keycloak-admin-client/lib/defs/roleRepresentation";
import type { ClientScopes } from "@keycloak/keycloak-admin-client/lib/resources/clientScopes";
import type { Clients } from "@keycloak/keycloak-admin-client/lib/resources/clients";
import type { Groups } from "@keycloak/keycloak-admin-client/lib/resources/groups";
import type { Roles } from "@keycloak/keycloak-admin-client/lib/resources/roles";
import type { Users } from "@keycloak/keycloak-admin-client/lib/resources/users";
import {
  deleteRoleMappings,
  getAllEffectiveRoles,
  getRoleMappings,
} from "./resource";
import { Row } from "./RoleMapping";

/** Admin Client 上可参与角色映射的资源键名。 */
export type ResourcesKey = keyof KeycloakAdminClient;

/** 各资源类型支持的「删除角色映射」Admin API 方法名。 */
type DeleteFunctions =
  | keyof Pick<Groups, "delClientRoleMappings" | "delRealmRoleMappings">
  | keyof Pick<ClientScopes, "delClientScopeMappings" | "delRealmScopeMappings">
  | keyof Pick<Roles, "delCompositeRoles">;

/** 各资源类型支持的「列出已生效/复合角色」Admin API 方法名。 */
type ListEffectiveFunction =
  | keyof Pick<Clients, "listCompositeRealmScopeMappings">
  | keyof Pick<Groups, "listRoleMappings" | "listAvailableRealmRoleMappings">
  | keyof Pick<
      ClientScopes,
      | "listScopeMappings"
      | "listAvailableRealmScopeMappings"
      | "listCompositeClientScopeMappings"
    >
  | keyof Pick<Roles, "getCompositeRoles" | "getCompositeRolesForClient">
  | keyof Pick<
      Users,
      "listCompositeClientRoleMappings" | "listCompositeRealmRoleMappings"
    >;

/** 各资源类型支持的「列出可分配角色」Admin API 方法名。 */
type ListAvailableFunction =
  | keyof Pick<
      Groups,
      "listAvailableClientRoleMappings" | "listAvailableRealmRoleMappings"
    >
  | keyof Pick<
      ClientScopes,
      "listAvailableClientScopeMappings" | "listAvailableRealmScopeMappings"
    >
  | keyof Pick<Roles, "find">
  | keyof Pick<Clients, "listRoles">;

/** 单一资源类型下删除、已生效、可分配三类操作的 API 方法列表。 */
type FunctionMapping = {
  delete: DeleteFunctions[];
  listAvailable: ListAvailableFunction[];
  listEffective: ListEffectiveFunction[];
};

/** 资源键到 FunctionMapping 的可选映射表。 */
type ResourceMapping = Partial<Record<ResourcesKey, FunctionMapping>>;

/** 用户/组资源共用同一套角色映射 API 方法。 */
const groupFunctions: FunctionMapping = {
  delete: ["delClientRoleMappings", "delRealmRoleMappings"],
  listEffective: [
    "listRoleMappings",
    "listCompositeRealmRoleMappings",
    "listCompositeClientRoleMappings",
  ],
  listAvailable: [
    "listAvailableClientRoleMappings",
    "listAvailableRealmRoleMappings",
  ],
};

/** 客户端/客户端作用域资源共用作用域映射 API 方法。 */
const clientFunctions: FunctionMapping = {
  delete: ["delClientScopeMappings", "delRealmScopeMappings"],
  listEffective: [
    "listScopeMappings",
    "listCompositeRealmScopeMappings",
    "listCompositeClientScopeMappings",
  ],
  listAvailable: [
    "listAvailableClientScopeMappings",
    "listAvailableRealmScopeMappings",
  ],
};

/** 按资源类型选择对应 Admin Client 子资源上的查询/删除方法。 */
const mapping: ResourceMapping = {
  groups: groupFunctions,
  users: groupFunctions,
  clientScopes: clientFunctions,
  clients: clientFunctions,
  roles: {
    delete: ["delCompositeRoles", "delCompositeRoles"],
    listEffective: [
      "getCompositeRoles",
      "getCompositeRoles",
      "getCompositeRolesForClient",
    ],
    listAvailable: ["listRoles", "find"],
  },
};

/** 根据选中行批量删除角色映射，委托 resource 层 POST ui-ext 端点。 */
export const deleteMapping = (
  adminClient: KeycloakAdminClient,
  type: ResourcesKey,
  id: string,
  rows: Row[],
) => {
  const roles = rows.map((row) => ({
    roleId: row.role.id!,
    roleName: row.role.name!,
    clientId: row.client?.id,
  }));

  return [deleteRoleMappings(adminClient, type, id, roles)];
};

/**
 * 获取指定资源的角色映射概览。
 * groups 可传入独立 groupsResource；roles 类型走 ui-ext 聚合接口。
 */
export const getMapping = async (
  adminClient: KeycloakAdminClient,
  type: ResourcesKey,
  id: string,
  groupsResource?: any,
): Promise<MappingsRepresentation> => {
  const query = mapping[type]!.listEffective[0];
  const resource =
    type === "groups" && groupsResource ? groupsResource : adminClient[type];
  const result = (resource as any)[query]({ id });
  if (type !== "roles") {
    return result as MappingsRepresentation;
  }

  const roleMappings = await getRoleMappings(adminClient, id);

  return {
    clientMappings: roleMappings.clientMappings,
    realmMappings: roleMappings.realmMappings,
  };
};

/** 列出领域级有效角色（过滤掉 clientRole），供角色映射表格展示。 */
export const getEffectiveRoles = async (
  adminClient: KeycloakAdminClient,
  type: ResourcesKey,
  id: string,
): Promise<Row[]> => {
  const effectiveRoles = await getAllEffectiveRoles(adminClient, { type, id });
  return effectiveRoles
    .filter((role) => !role.clientRole)
    .map((role) => ({
      role: {
        id: role.id,
        name: role.name,
        description: role.description,
      },
    }));
};

/** 分页查询当前资源可分配的角色列表。 */
export const getAvailableRoles = async (
  adminClient: KeycloakAdminClient,
  type: ResourcesKey,
  params: Record<string, string | number>,
  groupsResource?: any,
): Promise<Row[]> => {
  const query = mapping[type]!.listAvailable[1];
  const resource =
    type === "groups" && groupsResource ? groupsResource : adminClient[type];
  return (await (resource as any)[query](params)).map(
    (role: RoleRepresentation) => ({
      role,
    }),
  );
};
