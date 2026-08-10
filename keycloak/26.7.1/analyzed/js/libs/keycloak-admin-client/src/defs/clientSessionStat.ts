/** 客户端会话统计：Realm 仪表盘中按客户端聚合的在线/离线会话计数。 */
export interface ClientSessionStat {
  /** 客户端内部 UUID */
  id: string;
  /** 客户端标识符（clientId） */
  clientId: string;
  /** 当前活跃会话数（字符串形式，便于大数展示） */
  active: string;
  /** 离线会话数（Remember Me / 刷新令牌持久化） */
  offline: string;
}
