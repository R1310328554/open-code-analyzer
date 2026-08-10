import type ClientPolicyConditionRepresentation from "./clientPolicyConditionRepresentation.js";

/**
 * 单条客户端策略：由条件（何时生效）与关联 Profile（如何约束）组成。
 * https://www.keycloak.org/docs-api/15.0/rest-api/#_clientpolicyrepresentation
 */
export default interface ClientPolicyRepresentation {
  /** 策略生效需满足的全部条件 */
  conditions?: ClientPolicyConditionRepresentation[];
  /** 策略说明文本 */
  description?: string;
  /** 是否启用该策略 */
  enabled?: boolean;
  /** 执行模式（如 PERMISSIVE、ENFORCING） */
  mode?: string;
  /** 策略唯一名称 */
  name?: string;
  /** 关联的 Client Profile 名称列表 */
  profiles?: string[];
}
