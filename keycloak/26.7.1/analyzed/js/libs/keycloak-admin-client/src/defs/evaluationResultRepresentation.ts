import type { DecisionEffect } from "./policyRepresentation.js";
import type PolicyResultRepresentation from "./policyResultRepresentation.js";
import type ResourceRepresentation from "./resourceRepresentation.js";
import type ScopeRepresentation from "./scopeRepresentation.js";

/**
 * 授权策略评估结果：针对单个资源及其 scope 的 permit/deny 判定及策略链详情。
 */
export default interface EvaluationResultRepresentation {
  /** 被评估的资源 */
  resource?: ResourceRepresentation;
  /** 请求中涉及的全部 scope */
  scopes?: ScopeRepresentation[];
  /** 参与评估的策略及其判定结果列表 */
  policies?: PolicyResultRepresentation[];
  /** 综合判定效果（PERMIT 或 DENY） */
  status?: DecisionEffect;
  /** 最终允许访问的 scope 子集 */
  allowedScopes?: ScopeRepresentation[];
  /** 最终拒绝的 scope 子集 */
  deniedScopes?: ScopeRepresentation[];
}
