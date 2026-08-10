/**
 * 客户端初始访问令牌：用于动态客户端注册（Dynamic Client Registration）的临时凭证。
 * https://www.keycloak.org/docs-api/11.0/rest-api/index.html#_clientinitialaccesspresentation
 */
export default interface ClientInitialAccessPresentation {
  /** 初始访问记录 ID */
  id?: string;
  /** 可用于注册的 Bearer 令牌 */
  token?: string;
  /** 创建时间戳（毫秒） */
  timestamp?: number;
  /** 过期时间戳（毫秒） */
  expiration?: number;
  /** 允许的最大注册次数 */
  count?: number;
  /** 剩余可用注册次数 */
  remainingCount?: number;
}
