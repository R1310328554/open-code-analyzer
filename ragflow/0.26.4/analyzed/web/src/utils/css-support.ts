/**
 * css-support.ts — 运行时检测浏览器是否支持 CSS Anchor Positioning 相关特性。
 */

/** 是否同时支持 position-anchor、anchor-name 与 anchor()/anchor-size() 语法。 */
export const supportsCssAnchor =
  CSS.supports('position-anchor', '--anchor-name') &&
  CSS.supports('anchor-name', '--anchor-name') &&
  CSS.supports('top', 'anchor(--anchor-name bottom)') &&
  CSS.supports('width', 'anchor-size(--anchor-name width)');
