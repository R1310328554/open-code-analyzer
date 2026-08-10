// user-setting.ts — 用户/租户信息与系统健康状态类型。

/** 当前登录用户：凭证、偏好（语言/时区/主题）与权限标志。 */
export interface IUserInfo {
  access_token: string;
  avatar?: any;
  color_schema: string;
  create_date: string;
  create_time: number;
  email: string;
  id: string;
  is_active: string;
  is_anonymous: string;
  is_authenticated: string;
  is_superuser: boolean;
  language: string;
  last_login_time: string;
  login_channel: string;
  nickname: string;
  password: string;
  status: string;
  timezone: string;
  update_date: string;
  update_time: number;
}

/** 任务执行器各阶段耗时序列（键为阶段名）。 */
export type TaskExecutorElapsed = Record<string, number[]>;

/** 单个 task executor 心跳：pending/done/failed 与 lag。 */
export interface TaskExecutorHeartbeatItem {
  boot_at: string;
  current: null;
  done: number;
  failed: number;
  lag: number;
  name: string;
  now: string;
  pending: number;
}

/** 系统组件健康：ES、存储、DB、Redis 与 executor 心跳。 */
export interface ISystemStatus {
  es: Es;
  storage: Storage;
  database: Database;
  redis: Redis;
  task_executor_heartbeat: Record<string, TaskExecutorHeartbeatItem[]>;
}

/** Redis 探活结果：status、耗时与 pending 队列长度。 */
interface Redis {
  status: string;
  elapsed: number;
  error: string;
  pending: number;
}

/** 对象存储探活结果。 */
export interface Storage {
  status: string;
  elapsed: number;
  error: string;
}

/** 关系型数据库探活结果。 */
export interface Database {
  status: string;
  elapsed: number;
  error: string;
}

/** Elasticsearch 探活：节点数与 active_shards。 */
interface Es {
  status: string;
  elapsed: number;
  error: string;
  number_of_nodes: number;
  active_shards: number;
}

/** 租户成员用户：角色、状态与关联 user_id。 */
export interface ITenantUser {
  id: string;
  avatar: string;
  delta_seconds: number;
  email: string;
  is_active: string;
  is_anonymous: string;
  is_authenticated: string;
  is_superuser: boolean;
  nickname: string;
  role: string;
  status: string;
  update_date: string;
  user_id: string;
}

/** 用户所属租户摘要：tenant_id、角色与昵称。 */
export interface ITenant {
  avatar: string;
  delta_seconds: number;
  email: string;
  nickname: string;
  role: string;
  tenant_id: string;
  update_date: string;
}
