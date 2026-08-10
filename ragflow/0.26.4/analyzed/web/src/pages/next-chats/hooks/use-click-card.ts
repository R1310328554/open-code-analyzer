// use-click-card.ts — 会话卡片点击：切换 URL 并中止进行中的 SSE 输出。

import { useCallback, useState } from 'react';
import { useChatUrlParams } from './use-chat-url';

/** 处理左侧会话卡片点击，更新路由并中断当前流式回复。 */
export function useHandleClickConversationCard() {
  // 用于 cancel 当前 completion SSE 请求
  const [controller, setController] = useState(new AbortController());
  const { setConversationBoth } = useChatUrlParams();

  /** abort 当前控制器并创建新实例，供后续请求复用。 */
  const stopOutputMessage = useCallback(() => {
    setController((pre) => {
      pre.abort();
      return new AbortController();
    });
  }, []);

  /** 点击卡片：写入 conversationId/isNew 并停止输出。 */
  const handleConversationCardClick = useCallback(
    (conversationId: string, isNew: boolean) => {
      setConversationBoth(conversationId, isNew ? 'true' : '');
      stopOutputMessage();
    },
    [setConversationBoth, stopOutputMessage],
  );

  return { controller, handleConversationCardClick, stopOutputMessage };
}
