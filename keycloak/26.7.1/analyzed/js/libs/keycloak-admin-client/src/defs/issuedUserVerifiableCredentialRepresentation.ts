/**
 * 已签发的用户可验证凭据（User Verifiable Credential）记录。
 * 用于 Verifiable Credentials 扩展，追踪向用户颁发的 VC 及其生命周期。
 */
export default interface IssuedUserVerifiableCredentialRepresentation {
  /** VC 记录 ID */
  id?: string;
  /** 持有该凭据的用户 ID */
  userId?: string;
  /** 凭据类型标识（如特定 VC schema） */
  credentialType?: string;
  /** 签发时间（毫秒时间戳） */
  issuedAt?: number;
  /** 过期时间（毫秒时间戳） */
  expiresAt?: number;
  /** 签发该 VC 的 OAuth 客户端 ID */
  clientId?: string;
  /** 凭据版本/修订号 */
  revision?: string;
  /** 签发客户端显示名称 */
  clientName?: string;
  /** 签发客户端基础 URL */
  clientBaseUrl?: string;
}
