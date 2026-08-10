/**
 * 客户端策略条件：决定策略在何种上下文下被评估（如客户端类型、Scope 等）。
 * https://www.keycloak.org/docs-api/15.0/rest-api/#_clientpolicyconditionrepresentation
 */
export default interface ClientPolicyConditionRepresentation {
  /** 条件 Provider 标识（如 client-scopes、client-roles 等） */
  condition?: string;
  /** 条件 Provider 的 JSON 配置参数 */
  configuration?: object;
}
