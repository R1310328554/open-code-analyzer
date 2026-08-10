import AuthenticationFlowRepresentation from "@keycloak/keycloak-admin-client/lib/defs/authenticationFlowRepresentation";
import RealmRepresentation from "@keycloak/keycloak-admin-client/lib/defs/realmRepresentation";

/** 认证流被领域引用的方式：特定客户端、特定提供者或默认绑定。 */
type UsedBy = "SPECIFIC_CLIENTS" | "SPECIFIC_PROVIDERS" | "DEFAULT";

/** 扩展 Admin UI 所需的认证流表示，附带使用方信息与所属领域。 */
export type AuthenticationType = AuthenticationFlowRepresentation & {
  usedBy?: { type?: UsedBy; values: string[] };
  realm: RealmRepresentation;
};

/**
 * 领域内置认证流字段名到 UI 显示标签的映射。
 * 键为 RealmRepresentation 上的流属性名，值为管理控制台中的可读名称。
 */
export const REALM_FLOWS = new Map<string, string>([
  ["browserFlow", "browser"],
  ["registrationFlow", "registration"],
  ["directGrantFlow", "direct grant"],
  ["resetCredentialsFlow", "reset credentials"],
  ["clientAuthenticationFlow", "clients"],
  ["dockerAuthenticationFlow", "docker auth"],
  ["firstBrokerLoginFlow", "firstBrokerLogin"],
]);
