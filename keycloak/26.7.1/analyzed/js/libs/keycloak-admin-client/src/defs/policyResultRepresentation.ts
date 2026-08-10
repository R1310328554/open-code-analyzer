import type PolicyRepresentation from "./policyRepresentation.js";
import type { DecisionEffect } from "./policyRepresentation.js";

/** 单条策略在评估树中的结果节点，可递归包含关联子策略的评估 outcome。 */
export default interface PolicyResultRepresentation {
  /** 被评估的策略定义 */
  policy?: PolicyRepresentation;
  /** 该策略节点的决策效果（PERMIT/DENY） */
  status?: DecisionEffect;
  /** 参与聚合决策的关联子策略评估结果（递归结构） */
  associatedPolicies?: PolicyResultRepresentation[];
  /** 本策略节点实际生效或拒绝的 Scope 名称列表 */
  scopes?: string[];
}
