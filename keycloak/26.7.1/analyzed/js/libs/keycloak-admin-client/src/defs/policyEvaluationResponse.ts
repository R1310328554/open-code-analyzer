import type AccessTokenRepresentation from "./accessTokenRepresentation.js";
import type EvaluationResultRepresentation from "./evaluationResultRepresentation.js";
import type { DecisionEffect } from "./policyRepresentation.js";

/** 授权策略评估（Policy Evaluation）API 的完整响应：含逐资源结果与可选 RPT。 */
export default interface PolicyEvaluationResponse {
  /** 按资源/Scope 划分的评估结果列表 */
  results?: EvaluationResultRepresentation[];
  /** 响应是否以 UMA Entitlement 形式返回（而非纯评估明细） */
  entitlements?: boolean;
  /** 整体决策效果（PERMIT 或 DENY） */
  status?: DecisionEffect;
  /** 评估通过时签发的 Requesting Party Token（RPT），内含授权声明 */
  rpt?: AccessTokenRepresentation;
}
