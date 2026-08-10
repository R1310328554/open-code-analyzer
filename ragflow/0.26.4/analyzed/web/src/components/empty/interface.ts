// empty/interface.ts — 空状态组件 Empty 与 EmptyCard 的 Props 类型定义。

import { EmptyType } from './constant';

/** 通用空状态：预设 type、文案与图标宽度。 */
export type EmptyProps = {
  className?: string;
  children?: React.ReactNode;
  type?: EmptyType;
  text?: string;
  iconWidth?: number;
};

/** 卡片式空状态：标题、描述与自定义图标。 */
export type EmptyCardProps = {
  icon?: React.ReactNode;
  className?: string;
  children?: React.ReactNode;
  title?: string;
  description?: string;
  style?: React.CSSProperties;
} & Omit<React.HTMLAttributes<HTMLDivElement>, 'title'>;
