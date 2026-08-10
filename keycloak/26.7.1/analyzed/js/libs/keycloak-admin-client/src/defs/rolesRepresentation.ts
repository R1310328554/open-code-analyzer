/**
 * Realm 角色集合表示：按 Realm、Client、Application 维度分组的角色列表。
 * https://www.keycloak.org/docs-api/11.0/rest-api/index.html#_rolesrepresentation
 */

import type RoleRepresentation from "./roleRepresentation.js";

export default interface RolesRepresentation {
  /** Realm 级角色列表 */
  realm?: RoleRepresentation[];
  /** 按客户端 ID 分组的客户端角色 */
  client?: { [index: string]: RoleRepresentation[] };
  /** 按应用 ID 分组的应用角色（遗留字段） */
  application?: { [index: string]: RoleRepresentation[] };
}
