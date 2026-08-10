// use-cancel-dataflow.ts — 封装取消当前数据流运行的操作，成功后停止 trace 轮询。

import { useCancelDataflow } from '@/hooks/use-agent-request';
import { useCallback } from 'react';

/** 调用 cancelDataflow API，返回码为 0 时执行 stopFetchTrace。 */
export function useCancelCurrentDataflow({
  messageId,
  stopFetchTrace,
}: {
  messageId: string;
  stopFetchTrace(): void;
}) {
  const { cancelDataflow } = useCancelDataflow();

  const handleCancel = useCallback(async () => {
    const code = await cancelDataflow(messageId);
    if (code === 0) {
      stopFetchTrace();
    }
  }, [cancelDataflow, messageId, stopFetchTrace]);

  return { handleCancel };
}
