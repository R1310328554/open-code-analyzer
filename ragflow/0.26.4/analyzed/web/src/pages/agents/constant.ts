// constant.ts — Agents 列表页：画布类型枚举（Agent 对话流 / Dataflow 流水线）。

/** 创建画布时选择的流程类型。 */
export enum FlowType {
  /** 对话式 Agent 画布。 */
  Agent = 'agent',
  /** 文档/Dataflow 流水线画布。 */
  Flow = 'flow',
}
