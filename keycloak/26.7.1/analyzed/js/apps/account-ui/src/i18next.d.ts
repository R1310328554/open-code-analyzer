// https://www.i18next.com/overview/typescript
/**
 * 扩展 i18next 的 TypeScript 模块声明。
 * 临时允许 HTML 子节点为对象类型，以兼容现有 JSX 用法。
 */
import "i18next";

declare module "i18next" {
  interface CustomTypeOptions {
    // TODO: This flag should be removed and code that errors out should be made functional.
    // This will have to be done incrementally as the amount of errors the defaults produce is just too much.
    /** 允许 Trans 组件的 children 为对象；后续应逐步移除并修复类型错误。 */
    allowObjectInHTMLChildren: true;
  }
}
