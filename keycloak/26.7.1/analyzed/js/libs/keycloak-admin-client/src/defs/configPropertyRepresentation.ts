/**
 * 组件/IdP 映射器等配置项的属性描述：定义表单字段的类型、标签与校验规则。
 * https://www.keycloak.org/docs-api/11.0/rest-api/index.html#_configpropertyrepresentation
 */
export interface ConfigPropertyRepresentation {
  /** 配置键名（写入 config 对象的属性名） */
  name?: string;
  /** 管理控制台显示的字段标签 */
  label?: string;
  /** 字段帮助说明文本 */
  helpText?: string;
  /** 输入控件类型（如 String、boolean、List 等） */
  type?: string;
  /** 未设置时的默认值 */
  defaultValue?: object;
  /** 枚举/下拉选项的可选值列表 */
  options?: string[];
  /** 是否为敏感字段（控制台以密码框渲染） */
  secret?: boolean;
  /** 是否必填 */
  required?: boolean;
}
