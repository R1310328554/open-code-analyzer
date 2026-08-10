// use-move-note.ts — 便签节点悬浮预览：跟随鼠标定位 SVG 并控制图片显隐。

import { useMouse } from 'ahooks';
import { useCallback, useEffect, useRef, useState } from 'react';

/** 提供 ref、showImage/hideImage 与 imgVisible，鼠标移动时更新 SVG 位置。 */
export function useMoveNote() {
  const ref = useRef<SVGSVGElement>(null);
  const mouse = useMouse();
  const [imgVisible, setImgVisible] = useState(false);

  /** 统一切换预览图可见状态。 */
  const toggleVisible = useCallback((visible: boolean) => {
    setImgVisible(visible);
  }, []);

  const showImage = useCallback(() => {
    toggleVisible(true);
  }, [toggleVisible]);

  const hideImage = useCallback(() => {
    toggleVisible(false);
  }, [toggleVisible]);

  /** 将 SVG 定位在鼠标右下方（偏移 +10/-70px）。 */
  useEffect(() => {
    if (ref.current) {
      ref.current.style.top = `${mouse.clientY - 70}px`;
      ref.current.style.left = `${mouse.clientX + 10}px`;
    }
  }, [mouse.clientX, mouse.clientY]);

  return {
    ref,
    showImage,
    hideImage,
    mouse,
    imgVisible,
  };
}
