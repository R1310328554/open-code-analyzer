// use-add-box.ts — 调试模式下多聊天框并排：uuid 标识增删，非调试仅保留一个。

import { useCallback, useEffect, useState } from 'react';
import { v4 as uuid } from 'uuid';

/** 维护 chatBoxIds 数组；调试模式最多可增至 3 个并排对比。 */
export function useAddChatBox(isDebugMode: boolean) {
  const [ids, setIds] = useState<string[]>([uuid()]);

  // UI 据此隐藏「移除」或限制再添加
  const hasSingleChatBox = ids.length === 1;

  // 达到三栏上限时禁用「添加聊天框」
  const hasThreeChatBox = ids.length === 3;

  const addChatBox = useCallback(() => {
    setIds((prev) => [...prev, uuid()]);
  }, []);

  const removeChatBox = useCallback((id: string) => {
    setIds((prev) => prev.filter((x) => x !== id));
  }, []);

  /** 退出调试模式时截断为首个 chatBox，避免多栏残留。 */
  useEffect(() => {
    if (!isDebugMode) {
      setIds((pre) => pre.slice(0, 1));
    }
  }, [isDebugMode]);

  return {
    chatBoxIds: ids,
    hasSingleChatBox,
    hasThreeChatBox,
    addChatBox,
    removeChatBox,
  };
}
