/**
 * 认证流程编辑器中展示的单个执行步骤元信息（含 UI 与可配置性）。
 * @see https://www.keycloak.org/docs-api/11.0/rest-api/index.html#_authenticationexecutioninforepresentation
 */
export default interface AuthenticationExecutionInfoRepresentation {
  /** 执行步骤唯一 ID */
  id?: string;
  /** 当前要求级别 */
  requirement?: string;
  /** 管理 UI 显示名称 */
  displayName?: string;
  /** 执行 alias */
  alias?: string;
  /** 步骤说明 */
  description?: string;
  /** 可选的要求级别列表 */
  requirementChoices?: string[];
  /** 是否可在 UI 中配置 */
  configurable?: boolean;
  /** 是否为嵌套认证流 */
  authenticationFlow?: boolean;
  /** Provider/Flow 的 SPI ID */
  providerId?: string;
  /** 关联认证配置 ID */
  authenticationConfig?: string;
  /** 所属 flow ID */
  flowId?: string;
  /** 嵌套层级 */
  level?: number;
  /** 同级排序索引 */
  index?: number;
}
