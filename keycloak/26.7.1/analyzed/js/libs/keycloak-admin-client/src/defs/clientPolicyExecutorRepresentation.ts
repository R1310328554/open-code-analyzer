/**
 * 客户端策略执行器：策略匹配后实际执行的约束或校验逻辑。
 * https://www.keycloak.org/docs-api/15.0/rest-api/#_clientpolicyexecutorrepresentation
 */
export default interface ClientPolicyExecutorRepresentation {
  /** 执行器 Provider 的 JSON 配置 */
  configuration?: object;
  /** 执行器 Provider 标识（如 secure-session、pkce-enforcer 等） */
  executor?: string;
}
