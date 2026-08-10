/**
 * typings.d.ts — 全局与第三方模块 TypeScript 类型扩展。
 */

import '@tanstack/react-table';
declare module 'lodash';

/** 全局类型声明命名空间。 */
declare global {
  /** 可空类型别名：T 或 null。 */
  type Nullable<T> = T | null;
}

/** 扩展 TanStack Table ColumnMeta 的样式类名。 */
declare module '@tanstack/react-table' {
  /** 表头/单元格可选 className。 */
  interface ColumnMeta {
    headerClassName?: string;
    headerCellClassName?: string;
    cellClassName?: string;
  }
}
