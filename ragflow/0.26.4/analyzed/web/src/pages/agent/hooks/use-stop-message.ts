// use-stop-message.ts — 中止 Agent 对话：调用 cancelConversation 并在页面卸载时清理。

import { useCancelConversation } from '@/hooks/use-agent-request';
import { useCallback, useEffect } from 'react';

/** 提供 stopMessage(taskId)，有 taskId 时静默调用 cancelConversation。 */
export function useStopMessage() {
  const { cancelConversation } = useCancelConversation();

  const stopMessage = useCallback(
    (taskId?: string) => {
      if (taskId) {
        void cancelConversation(taskId).catch(() => undefined);
      }
    },
    [cancelConversation],
  );

  return { stopMessage };
}

/** 聊天可见时监听 beforeunload，离开页面前尝试停止当前任务。 */
export function useStopMessageUnmount(chatVisible: boolean, taskId?: string) {
  const { stopMessage } = useStopMessage();

  const handleBeforeUnload = useCallback(() => {
    if (chatVisible) {
      stopMessage(taskId);
    }
  }, [chatVisible, stopMessage, taskId]);

  useEffect(() => {
    window.addEventListener('beforeunload', handleBeforeUnload);
    return () => {
      window.removeEventListener('beforeunload', handleBeforeUnload);
    };
  }, [handleBeforeUnload]);

  return { stopMessage };
}
