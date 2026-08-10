/**
 * 用户凭据（密码、OTP、WebAuthn 等）的 REST 表示，用于创建、更新或展示凭据信息。
 * https://www.keycloak.org/docs-api/11.0/rest-api/index.html#_credentialrepresentation
 */

export default interface CredentialRepresentation {
  /** 凭据创建时间（毫秒时间戳） */
  createdDate?: number;
  /** Provider 特定的凭据元数据（JSON 字符串） */
  credentialData?: string;
  /** 凭据实例 ID */
  id?: string;
  /** 同类凭据的优先级（数值越小优先级越高） */
  priority?: number;
  /** Provider 存储的密钥/哈希等敏感数据（JSON 字符串） */
  secretData?: string;
  /** 是否为临时凭据（如首次登录强制改密） */
  temporary?: boolean;
  /** 凭据类型（password、otp、webauthn-passwordless 等） */
  type?: string;
  /** 用户自定义标签（如「工作手机 OTP」） */
  userLabel?: string;
  /** 明文凭据值（仅写入请求时使用，响应中通常不包含） */
  value?: string;
  /** 联邦用户存储组件 ID（凭据来自外部存储时） */
  federationLink?: string;
}
