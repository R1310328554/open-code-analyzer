/**
 * 用户或组的有效角色映射：包含 Realm 级与按 Client 划分的角色集合。
 * https://www.keycloak.org/docs-api/11.0/rest-api/index.html#_mappingsrepresentation
 */
import type RoleRepresentation from "./roleRepresentation.js";

export default interface MappingsRepresentation {
  /** 按 clientId 分组的有效 Client 角色映射 */
  clientMappings?: Record<string, any>;
  /** 直接继承的 Realm 角色列表 */
  realmMappings?: RoleRepresentation[];
}
