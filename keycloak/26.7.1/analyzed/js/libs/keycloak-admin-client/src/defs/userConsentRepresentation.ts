/**
 * 用户 OAuth/OIDC 授权同意记录：记录用户对某客户端已授予的作用域及时间戳。
 * https://www.keycloak.org/docs-api/11.0/rest-api/#_userconsentrepresentation
 */

export default interface UserConsentRepresentation {
  /** 客户端 ID（clientId） */
  clientId?: string;
  /** 同意记录创建时间（Unix 毫秒时间戳） */
  createdDate?: number;
  /** 用户已授予的客户端作用域名称列表 */
  grantedClientScopes?: string[];
  /** 同意记录最后更新时间（Unix 毫秒时间戳） */
  lastUpdatedDate?: number;
}
