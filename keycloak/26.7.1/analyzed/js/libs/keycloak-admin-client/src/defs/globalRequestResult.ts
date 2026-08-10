/**
 * 批量 Admin API 请求的全局执行结果：汇总成功与失败的子请求标识。
 * https://www.keycloak.org/docs-api/11.0/rest-api/index.html#_globalrequestresult
 */
export default interface GlobalRequestResult {
  /** 已成功处理的子请求 ID 或路径列表 */
  successRequests?: string[];
  /** 处理失败的子请求 ID 或路径列表 */
  failedRequests?: string[];
}
