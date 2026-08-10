/**
 * 认证器配置元信息：描述某认证器 Provider 可配置项的结构与 UI 展示属性。
 * https://www.keycloak.org/docs-api/11.0/rest-api/index.html#_authenticatorconfiginforepresentation
 */
export default interface AuthenticatorConfigInfoRepresentation {
  /** 配置项在管理控制台中的显示名称 */
  name?: string;
  /** 认证器 Provider 的唯一标识符 */
  providerId?: string;
  /** 面向管理员的帮助说明文本 */
  helpText?: string;
  /** 该认证器支持的配置属性列表 */
  properties?: ConfigPropertyRepresentation[];
}

/** 单个配置属性的 schema 定义，供管理控制台渲染表单 */
export interface ConfigPropertyRepresentation {
  /** 属性键名（写入 config 的字段名） */
  name?: string;
  /** 表单标签文本 */
  label?: string;
  /** 字段级帮助说明 */
  helpText?: string;
  /** 控件类型（如 string、boolean、List 等） */
  type?: string;
  /** 未填写时的默认值 */
  defaultValue?: any;
  /** 枚举型属性的可选值列表 */
  options?: string[];
  /** 是否为敏感字段（密码框等） */
  secret?: boolean;
  /** 是否必填 */
  required?: boolean;
  /** 输入框占位提示 */
  placeholder?: string;
}
