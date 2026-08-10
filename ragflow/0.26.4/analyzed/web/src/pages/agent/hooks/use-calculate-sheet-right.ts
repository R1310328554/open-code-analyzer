// use-calculate-sheet-right.ts — 根据视口宽度计算侧边 Sheet 的 Tailwind right 定位类名。

import { useSize } from 'ahooks';

/** body 宽度大于 1800px 时用固定偏移，否则占 1/3 屏宽。 */
export function useCalculateSheetRight() {
  const size = useSize(document.querySelector('body'));
  const bodyWidth = size?.width ?? 0;

  return bodyWidth > 1800 ? 'right-[620px]' : `right-1/3`;
}
