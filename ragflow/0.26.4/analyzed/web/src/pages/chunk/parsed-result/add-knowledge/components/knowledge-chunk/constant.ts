// constant.ts — 知识库分块卡片文本展示模式枚举。

/** Full 全文展示，Ellipse 省略截断。 */
export enum ChunkTextMode {
  /** 完整显示分块正文 */
  Full = 'full',
  /** 单行或固定高度省略显示 */
  Ellipse = 'ellipse',
}
