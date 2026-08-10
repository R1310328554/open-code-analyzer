/**
 * 可验证凭证 Offer Required Action 的配置：定义 OID4VCI 预授权发放流程中的客户端与凭证类型。
 * Represents a configuration of verifiable credential offer action
 * */
export default interface VerifiableCredentialOfferActionConfigRepresentation {
  /** 目标凭证配置 ID */
  credentialConfigurationId?: string;
  /** 执行 Offer 流程的客户端 ID */
  clientId?: string;
  /** 是否使用预授权码（pre-authorized）模式 */
  preAuthorized?: boolean;
}
