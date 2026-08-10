/**
 * Realm 签名/加密密钥元数据集合，用于 Admin API 查询当前活跃密钥与各算法密钥列表。
 * https://www.keycloak.org/docs-api/11.0/rest-api/index.html#_keysmetadatarepresentation-keymetadatarepresentation
 */
export default interface KeysMetadataRepresentation {
  /** 各算法当前活跃密钥的 kid 映射（如 RS256 → kid 字符串） */
  active?: { [index: string]: string };
  /** Realm 内全部密钥条目的元信息列表 */
  keys?: KeyMetadataRepresentation[];
}

/** 单个签名或加密密钥的元数据（不含私钥材料）。 */
export interface KeyMetadataRepresentation {
  /** 密钥 Provider SPI 标识（如 rsa-generated、java-keystore） */
  providerId?: string;
  /** Provider 优先级，数值越大越优先被选为活跃密钥 */
  providerPriority?: number;
  /** 密钥 ID（JWT/JWS header 中的 kid） */
  kid?: string;
  /** 密钥状态（如 ACTIVE、PASSIVE、DISABLED） */
  status?: string;
  /** 密钥用途类型（如 RSA、EC、OCT） */
  type?: string;
  /** 签名/加密算法名称（如 RS256、ES256） */
  algorithm?: string;
  /** PEM 编码的公钥 */
  publicKey?: string;
  /** PEM 编码的 X.509 证书（若密钥以证书形式暴露） */
  certificate?: string;
  /** 证书/密钥有效期截止时间（ISO-8601 或毫秒时间戳字符串） */
  validTo?: string;
}
