import type ClientPolicyRepresentation from "./clientPolicyRepresentation.js";

/**
 * 客户端策略集合：Realm 级 Client Policies 功能的顶层容器。
 * https://www.keycloak.org/docs-api/15.0/rest-api/#_clientpoliciesrepresentation
 */
export default interface ClientPoliciesRepresentation {
  /** 全局策略（对所有客户端生效） */
  globalPolicies?: ClientPolicyRepresentation[];
  /** Realm 内定义的命名策略列表 */
  policies?: ClientPolicyRepresentation[];
}
