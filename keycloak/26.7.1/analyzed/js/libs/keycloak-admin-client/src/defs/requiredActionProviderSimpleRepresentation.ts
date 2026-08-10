/**
 * Required Action 提供者简要表示：用于列表或下拉选择场景。
 */
export default interface RequiredActionProviderSimpleRepresentation {
  /** 提供者实例 ID */
  id?: string;
  /** 显示名称 */
  name?: string;
  /** SPI 提供者 ID（如 VERIFY_EMAIL、CONFIGURE_TOTP） */
  providerId?: string;
}
