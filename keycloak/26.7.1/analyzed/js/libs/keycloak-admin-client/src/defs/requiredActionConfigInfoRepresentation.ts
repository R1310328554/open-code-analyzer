import { ConfigPropertyRepresentation } from "./configPropertyRepresentation.js";

/**
 * Required Action 配置元信息：描述可配置属性及其类型/默认值（供 Admin UI 渲染表单）。
 */
export default interface RequiredActionConfigInfoRepresentation {
  /** 可配置属性定义列表 */
  properties?: ConfigPropertyRepresentation[];
}
