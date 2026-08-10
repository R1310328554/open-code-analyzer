/**
 * dom-util.ts — DOM 滚动辅助：将可滚动元素滚至底部。
 */

/** 将元素 scrollTop 设为 scrollHeight，实现滚到底部。 */
export const scrollToBottom = (element: HTMLElement) => {
  element.scrollTo(0, element.scrollHeight);
};
