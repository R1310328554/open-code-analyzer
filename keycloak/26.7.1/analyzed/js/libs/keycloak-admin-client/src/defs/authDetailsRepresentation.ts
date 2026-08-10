/** 管理操作审计事件中记录的操作者上下文（客户端、用户、IP 等）。 */
export default interface AuthDetailsRepresentation {
  /** 发起请求的 OAuth 客户端 ID */
  clientId?: string;
  /** 客户端 IP 地址 */
  ipAddress?: string;
  /** 操作所在 realm 的内部 ID */
  realmId?: string;
  /** 操作者用户 ID */
  userId?: string;
}
