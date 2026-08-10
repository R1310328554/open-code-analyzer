import type ClientPolicyExecutorRepresentation from "./clientPolicyExecutorRepresentation.js";

/**
 * 客户端 Profile：一组可复用的执行器配置，供 Client Policy 引用。
 * https://www.keycloak.org/docs-api/15.0/rest-api/#_clientprofilerepresentation
 */
export default interface ClientProfileRepresentation {
  /** Profile 说明文本 */
  description?: string;
  /** 该 Profile 包含的策略执行器列表 */
  executors?: ClientPolicyExecutorRepresentation[];
  /** Profile 唯一名称 */
  name?: string;
}
