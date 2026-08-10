/**
 * 通过 Admin API 生成或轮换 Realm 密钥时使用的 Java Keystore 配置参数。
 * https://www.keycloak.org/docs-api/11.0/rest-api/#_keystoreconfig
 */
export default interface KeyStoreConfig {
  /** 是否生成/更新 Realm 级 SSL/TLS 证书密钥对 */
  realmCertificate?: boolean;
  /** Keystore 文件访问密码 */
  storePassword?: string;
  /** 私钥条目密码（可与 storePassword 相同） */
  keyPassword?: string;
  /** Keystore 内密钥条目的 alias */
  keyAlias?: string;
  /** Realm 证书在 Keystore 中的 alias（与 keyAlias 区分时单独指定） */
  realmAlias?: string;
  /** Keystore 格式（如 JKS、PKCS12） */
  format?: string;
  /** 生成 RSA 密钥时的位长（如 2048、4096） */
  keySize?: number;
  /** 生成证书的有效期（天数） */
  validity?: number;
}
