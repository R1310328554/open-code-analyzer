// use-run-dataflow.ts — Pipeline 画布运行：先保存 DSL，再 SSE 触发 Agent 执行并打开日志。

import message from '@/components/ui/message';
import { useSendMessageBySSE } from '@/hooks/use-send-message';
import api from '@/utils/api';
import { get } from 'lodash';
import { useCallback, useState } from 'react';
import { useParams } from 'react-router';
import { UseFetchLogReturnType } from './use-fetch-pipeline-log';
import { useSaveGraph } from './use-save-graph';

/** 上传文件后 saveGraph → showLogSheet → agentChatCompletion，返回 messageId。 */
export function useRunDataflow({
  showLogSheet,
  setMessageId,
}: {
  showLogSheet: () => void;
} & Pick<UseFetchLogReturnType, 'setMessageId'>) {
  const { send } = useSendMessageBySSE(api.agentChatCompletion);
  const { id } = useParams();
  const { saveGraph, loading } = useSaveGraph();
  const [uploadedFileData, setUploadedFileData] =
    useState<Record<string, any>>();

  const run = useCallback(
    async (fileResponseData: Record<string, any>) => {
      // 运行前持久化当前画布，失败则中止
      const saveRet = await saveGraph();
      const success = saveRet?.code === 0;
      if (!success) return;

      showLogSheet();
      const res = await send({
        agent_id: id,
        query: '',
        'openai-compatible': false,
        session_id: null,
        files: [fileResponseData.file],
      });

      if (res && res?.response.status === 200 && get(res, 'data.code') === 0) {
        // 记录已上传文件并绑定 SSE 返回的 message_id 供日志拉取
        // fetch canvas
        setUploadedFileData(fileResponseData.file[0]);
        const msgId = get(res, 'data.data.message_id');
        if (msgId) {
          setMessageId(msgId);
        }

        return msgId;
      } else {
        message.error(get(res, 'data.message', ''));
      }
    },
    [id, saveGraph, send, setMessageId, setUploadedFileData, showLogSheet],
  );

  return { run, loading: loading, uploadedFileData };
}

/** useRunDataflow 返回值类型，供 Pipeline 面板 props 推导。 */
export type RunDataflowType = ReturnType<typeof useRunDataflow>;
