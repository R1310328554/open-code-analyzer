/**
 * 密钥/证书材料：用于 Realm 签名密钥、客户端密钥对等场景的导入与导出。
 * https://www.keycloak.org/docs-api/11.0/rest-api/#_certificaterepresentation
 */
export default interface CertificateRepresentation {
  /** PEM 编码的私钥 */
  privateKey?: string;
  /** PEM 编码的公钥 */
  publicKey?: string;
  /** X.509 证书链（PEM） */
  certificate?: string;
  /** 密钥标识符（Key ID），用于 JWT/JWKS 关联 */
  kid?: string;
  /** JSON Web Key Set 字符串 */
  jwks?: string;
}
