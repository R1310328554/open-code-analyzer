// custom.d.ts — 前端全局类型声明：可空泛型、Markdown 模块与 jsoneditor 第三方库。

/** 可空类型别名：T 或 null。 */
type Nullable<T> = T | null;

/** 声明 *.md 文件模块，默认导出字符串内容。 */
declare module '*.md' {
  const content: string;
  export default content;
}

/** 声明 jsoneditor 库模块，默认导出 JSONEditor 构造函数。 */
declare module 'jsoneditor' {
  const JSONEditor: any;
  export default JSONEditor;
  export = JSONEditor;
}
