/**
 * Realm 密码策略编辑器中可用的策略类型元信息（SPI 注册项）。
 * https://www.keycloak.org/docs-api/11.0/rest-api/index.html#_passwordpolicytyperepresentation
 */
export default interface PasswordPolicyTypeRepresentation {
  /** 策略类型 ID（如 length、upperCase、hashAlgorithm） */
  id?: string;
  /** 管理控制台展示名称 */
  displayName?: string;
  /** 配置值类型（如 int、String），决定 UI 输入控件 */
  configType?: string;
  /** 策略默认值（未显式配置时使用） */
  defaultValue?: string;
  /** 是否允许在同一 Realm 中配置多条同类型策略实例 */
  multipleSupported?: boolean;
}
