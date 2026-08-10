/** OpenID Connect 标准 address 声明的结构化地址字段。 */
export default interface AddressClaimSet {
  /** 国家 */
  country?: string;
  /** 完整格式化地址 */
  formatted?: string;
  /** 城市/ locality */
  locality?: string;
  /** 邮政编码 */
  postal_code?: string;
  /** 省/州/地区 */
  region?: string;
  /** 街道地址 */
  street_address?: string;
}
