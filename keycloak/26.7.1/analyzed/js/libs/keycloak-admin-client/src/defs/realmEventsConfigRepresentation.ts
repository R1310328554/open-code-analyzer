/**
 * Realm 事件与审计日志配置：控制用户事件、Admin 事件及监听器的启用与保留策略。
 * https://www.keycloak.org/docs-api/11.0/rest-api/#_realmeventsconfigrepresentation
 */

export interface RealmEventsConfigRepresentation {
  /** 是否启用用户事件记录 */
  eventsEnabled?: boolean;
  /** 用户事件在数据库中的保留时长（秒） */
  eventsExpiration?: number;
  /** 事件监听器 SPI ID 列表（如 jboss-logging、email） */
  eventsListeners?: string[];
  /** 启用的用户事件类型名称列表 */
  enabledEventTypes?: string[];
  /** 是否记录 Admin REST API 操作事件 */
  adminEventsEnabled?: boolean;
  /** Admin 事件是否包含请求/响应详情 */
  adminEventsDetailsEnabled?: boolean;
}
