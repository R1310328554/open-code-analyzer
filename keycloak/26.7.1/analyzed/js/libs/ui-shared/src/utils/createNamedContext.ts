import type { Context } from "react";
import { createContext } from "react";

/** 带 displayName 的 React Context，便于 React DevTools 识别 */
export type NamedContext<T> = Context<T> &
  Required<Pick<Context<T>, "displayName">>;

/**
 * 创建具名 React Context，统一设置 displayName 以改善调试体验。
 * @param displayName DevTools 中显示的上下文名称
 * @param defaultValue 默认值（未匹配 Provider 时使用）
 */
export function createNamedContext<T>(displayName: string, defaultValue: T) {
  const context = createContext(defaultValue);
  context.displayName = displayName;
  return context as NamedContext<T>;
}
