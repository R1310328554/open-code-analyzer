import type ClientProfileRepresentation from "./clientProfileRepresentation.js";

/**
 * 客户端 Profile 集合：Realm 级 Client Profiles 功能的顶层容器。
 * https://www.keycloak.org/docs-api/15.0/rest-api/#_clientprofilesrepresentation
 */
export default interface ClientProfilesRepresentation {
  /** 全局 Profile（内置或跨 Realm 共享的默认配置） */
  globalProfiles?: ClientProfileRepresentation[];
  /** Realm 内自定义 Profile 列表 */
  profiles?: ClientProfileRepresentation[];
}
