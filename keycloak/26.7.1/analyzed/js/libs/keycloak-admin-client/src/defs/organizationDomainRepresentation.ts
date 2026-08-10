/** 组织关联的电子邮件域名及其验证状态。 */
export default interface OrganizationDomainRepresentation {
  /** 域名（如 example.com，用于按邮箱后缀自动归属组织） */
  name?: string;
  /** 域名是否已通过 DNS/邮件验证 */
  verified?: boolean;
}
