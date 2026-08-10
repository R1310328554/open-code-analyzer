/**
 * Required Action 提供者表示：登录后强制用户完成的操作（如验证邮箱、修改密码）。
 * https://www.keycloak.org/docs-api/11.0/rest-api/index.html#_requiredactionproviderrepresentation
 */

/** 内置 Required Action 别名常量 */
export enum RequiredActionAlias {
  /** 验证邮箱 */
  VERIFY_EMAIL = "VERIFY_EMAIL",
  /** 更新个人资料 */
  UPDATE_PROFILE = "UPDATE_PROFILE",
  /** 配置 TOTP 双因素认证 */
  CONFIGURE_TOTP = "CONFIGURE_TOTP",
  /** 更新密码 */
  UPDATE_PASSWORD = "UPDATE_PASSWORD",
  /** 接受服务条款 */
  TERMS_AND_CONDITIONS = "TERMS_AND_CONDITIONS",
}

export default interface RequiredActionProviderRepresentation {
  /** 动作别名（唯一标识，如 VERIFY_EMAIL） */
  alias?: string;
  /** 提供者配置项 */
  config?: Record<string, any>;
  /** 是否作为新用户的默认 Required Action */
  defaultAction?: boolean;
  /** 是否启用该 Required Action */
  enabled?: boolean;
  /** 显示名称 */
  name?: string;
  /** SPI 提供者 ID */
  providerId?: string;
  /** 执行优先级（数值越小越先执行） */
  priority?: number;
}
