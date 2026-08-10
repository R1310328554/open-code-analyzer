// use-switch-debug-mode.ts — 聊天页调试模式开关：控制多 chatBox 与高级对比 UI。

import { useCallback, useState } from 'react';

/** 本地 state 切换 isDebugMode，供 useAddChatBox 等子 hook 联动。 */
export function useSwitchDebugMode() {
  const [isDebugMode, setIsDebugMode] = useState(false);

  /** 取反当前调试状态。 */
  const switchDebugMode = useCallback(() => {
    setIsDebugMode(!isDebugMode);
  }, [isDebugMode]);

  return {
    isDebugMode,
    switchDebugMode,
  };
}
