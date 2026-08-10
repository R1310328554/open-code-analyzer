/**
 * keycloak-admin-client 包公共入口：导出 Admin REST 客户端、Required Action 别名及常用类型。
 */
import { KeycloakAdminClient } from "./client.js";
import { RequiredActionAlias } from "./defs/requiredActionProviderRepresentation.js";

/** Required Action 内置别名枚举（与服务端 SPI 一致） */
export const requiredAction = RequiredActionAlias;
/** 默认导出：Keycloak Admin REST API 客户端类 */
export default KeycloakAdminClient;
/** 网络层错误类型与带错误处理的 fetch 封装 */
export { NetworkError, fetchWithError } from "./utils/fetchWithError.js";
export type { NetworkErrorOptions } from "./utils/fetchWithError.js";

/** 组织邀请表示类型 */
export type { default as OrganizationInvitationRepresentation } from "./defs/organizationInvitationRepresentation.js";
/** 组织邀请状态枚举 */
export { OrganizationInvitationStatus } from "./defs/organizationInvitationRepresentation.js";

/** Groups 资源模块（用户组 CRUD） */
export { Groups } from "./resources/groups.js";
// V2 API types (Kiota-generated) — Clients V2 Admin API 的 Kiota 生成类型
/** OIDC/SAML 客户端 V2 表示及联合类型 */
export type {
  OIDCClientRepresentation,
  SAMLClientRepresentation,
  ClientRepresentationV2,
} from "./resources/clientsV2.js";
