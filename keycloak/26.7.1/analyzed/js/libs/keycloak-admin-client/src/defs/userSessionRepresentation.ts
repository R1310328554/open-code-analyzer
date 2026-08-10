/**
 * 用户登录会话表示：Admin Session API 列出的在线会话及其客户端绑定信息。
 */
export default interface UserSessionRepresentation {
  /** 会话 UUID */
  id?: string;
  /** 会话关联的客户端映射（clientId → 内部 client UUID） */
  clients?: Record<string, string>;
  /** 发起会话的客户端 IP 地址 */
  ipAddress?: string;
  /** 最后一次访问时间（Unix 毫秒时间戳） */
  lastAccess?: number;
  /** 会话开始时间（Unix 毫秒时间戳） */
  start?: number;
  /** 所属用户 UUID */
  userId?: string;
  /** 所属用户名 */
  username?: string;
  /** 是否为临时/匿名用户会话 */
  transientUser?: boolean;
}
