/**
 * 用户关联的可验证凭证（Verifiable Credential）表示：记录已签发 VC 的配置、版本与用户属性快照。
 * Represents a verifiable credential associated with a user.
 * */
export default interface UserVerifiableCredentialRepresentation {
  /** 凭证作用域名称（credential scope） */
  credentialScopeName?: string;
  /** 凭证配置 ID（对应 OID4VCI credential configuration） */
  credentialConfigurationId?: string;
  /** 凭证内容修订号/版本标识 */
  revision?: string;
  /** 凭证创建时间（Unix 毫秒时间戳） */
  createdDate?: number;
  /** 凭证最后更新时间（Unix 毫秒时间戳） */
  updatedDate?: number;
  /** 签发时绑定的用户属性快照（属性名 → 值列表） */
  userAttributes?: Record<string, string[]>;
}
