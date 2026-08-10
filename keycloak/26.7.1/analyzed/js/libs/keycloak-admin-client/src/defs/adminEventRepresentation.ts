import type AuthDetailsRepresentation from "./authDetailsRepresentation.js";

/** 管理控制台/Admin API 操作产生的审计事件记录。 */
export default interface AdminEventRepresentation {
  /** 操作者认证详情 */
  authDetails?: AuthDetailsRepresentation;
  /** 操作失败时的错误信息 */
  error?: string;
  /** 操作类型（CREATE、UPDATE、DELETE 等） */
  operationType?: string;
  /** 事件所属 realm ID */
  realmId?: string;
  /** 被操作资源的 JSON 表示（快照） */
  representation?: string;
  /** REST 资源路径 */
  resourcePath?: string;
  /** 资源类型（如 USER、CLIENT） */
  resourceType?: string;
  /** 事件发生时间（毫秒时间戳） */
  time?: number;
  /** 附加键值详情 */
  details?: Record<string, any>;
}
