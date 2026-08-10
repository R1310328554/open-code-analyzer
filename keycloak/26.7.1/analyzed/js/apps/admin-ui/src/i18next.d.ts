// https://www.i18next.com/overview/typescript
import "i18next";

/**
 * 扩展 i18next 的 TypeScript 模块声明，使 Admin UI 与 react-i18next 的类型检查一致。
 * 参考官方文档：https://www.i18next.com/overview/typescript
 */
declare module "i18next" {
  interface CustomTypeOptions {
    // TODO: This flag should be removed and code that errors out should be made functional.
    // This will have to be done incrementally as the amount of errors the default produces is just too much.
    /** 允许在 Trans/HTML 子节点中传入对象；关闭默认严格模式后可渐进修复存量类型错误。 */
    allowObjectInHTMLChildren: true;
  }
}
