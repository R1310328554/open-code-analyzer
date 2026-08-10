/**
 * 工作流（Workflow）表示：Realm 内可编排的多步骤后台任务及其执行状态。
 */
export default interface WorkflowRepresentation {
  /** 工作流 UUID */
  id?: string;
  /** 工作流名称 */
  name?: string;
  /** 是否启用 */
  enabled?: boolean;
  /** 有序步骤列表 */
  steps?: Step[];
}

/** 工作流中的单个步骤：含优先级、调度时间与完成状态 */
export interface Step {
  /** 步骤名称 */
  name?: string;
  /** 执行优先级（数值越小越优先） */
  priority?: number;
  /** 步骤实现/处理器标识 */
  uses?: string;
  /** 计划执行时间（Unix 毫秒时间戳，JSON 键为 scheduled-at） */
  "scheduled-at"?: number;
  /** 步骤状态：已完成或待执行 */
  status?: "COMPLETED" | "PENDING";
}
