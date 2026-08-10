/**
 * 认证器实例配置：绑定到认证流程中某执行步骤的实际参数值。
 * https://www.keycloak.org/docs-api/11.0/rest-api/index.html#_authenticatorconfigrepresentation
 */
export default interface AuthenticatorConfigRepresentation {
  /** 配置记录的唯一 ID */
  id?: string;
  /** 配置别名，便于在流程中引用与识别 */
  alias?: string;
  /** 键值对形式的 Provider 配置参数 */
  config?: { [index: string]: string };
}

// 自定义类型：服务端原始定义为 `{[index: string]: any}[]`，
// 但管理控制台依赖 id、displayName 等固定字段进行展示。
/** 认证 Provider 的注册信息摘要（用于选择器与列表展示） */
export interface AuthenticationProviderRepresentation {
  /** Provider 内部 ID */
  id?: string;
  /** 控制台显示名称 */
  displayName?: string;
  /** Provider 功能描述 */
  description?: string;
  /** 是否支持密钥类配置项 */
  supportsSecret?: boolean;
}
