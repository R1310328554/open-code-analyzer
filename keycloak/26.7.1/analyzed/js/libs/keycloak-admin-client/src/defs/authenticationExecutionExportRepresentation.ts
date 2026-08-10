/**
 * 认证流程导出/导入用的执行步骤表示。
 * @see https://www.keycloak.org/docs-api/11.0/rest-api/index.html#_authenticationexecutionexportrepresentation
 */
export default interface AuthenticationExecutionExportRepresentation {
  /** 子流程别名（嵌套 flow 时） */
  flowAlias?: string;
  /** 是否允许用户自行配置（如 OTP 注册） */
  userSetupAllowed?: boolean;
  /** 关联的 Authenticator 配置 alias */
  authenticatorConfig?: string;
  /** Authenticator Provider ID */
  authenticator?: string;
  /** 执行要求：REQUIRED、ALTERNATIVE、DISABLED 等 */
  requirement?: string;
  /** 同层内的执行优先级（越小越先） */
  priority?: number;
  /** 是否为子认证流（API 字段名保留历史拼写 autheticatorFlow） */
  autheticatorFlow?: boolean;
}
