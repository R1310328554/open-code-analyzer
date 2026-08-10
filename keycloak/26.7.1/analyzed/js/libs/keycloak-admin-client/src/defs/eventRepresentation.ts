/**
 * 用户/客户端事件日志条目：记录登录、登出、令牌操作等运行时行为。
 * https://www.keycloak.org/docs-api/11.0/rest-api/index.html#_eventrepresentation
 */
import type EventType from "./eventTypes.js";

export default interface EventRepresentation {
  /** 触发事件的 OAuth 客户端 ID */
  clientId?: string;
  /** 事件附加详情（键值对，内容因事件类型而异） */
  details?: Record<string, any>;
  /** 操作失败时的错误描述 */
  error?: string;
  /** 发起请求的客户端 IP 地址 */
  ipAddress?: string;
  /** 事件所属 realm ID */
  realmId?: string;
  /** 关联的用户会话 ID */
  sessionId?: string;
  /** 事件发生时间（毫秒时间戳） */
  time?: number;
  /** 事件类型枚举值 */
  type?: EventType;
  /** 关联用户 ID */
  userId?: string;
}
