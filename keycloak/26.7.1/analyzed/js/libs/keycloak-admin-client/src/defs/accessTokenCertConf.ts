/** 访问令牌 cnf（confirmation）声明：绑定证书指纹，用于 mTLS 或 Holder-of-Key。 */
export default interface AccessTokenCertConf {
  /** X.509 证书 SHA-256 指纹（x5t#S256） */
  "x5t#S256"?: string;
}
