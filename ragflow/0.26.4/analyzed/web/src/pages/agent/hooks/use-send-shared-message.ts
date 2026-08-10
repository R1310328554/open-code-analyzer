// use-send-shared-message.ts — 分享页聊天：解析 URL 参数、Task 模式自动触发与参数弹窗。

import { SharedFrom } from '@/constants/chat';
import { useSetModalState } from '@/hooks/common-hooks';
import { useFetchExternalAgentInputs } from '@/hooks/use-agent-request';
import { IEventList } from '@/hooks/use-send-message';
import {
  buildRequestBody,
  useSendAgentMessage,
} from '@/pages/agent/chat/use-send-agent-message';
import { BeginQuery } from '@/pages/agent/interface';
import { isEmpty } from 'lodash';
import trim from 'lodash/trim';
import { useCallback, useEffect, useRef, useState } from 'react';
import { useSearchParams } from 'react-router';
import { AgentDialogueMode } from '../constant';

/** 输入框 trim 后为空则禁用发送按钮。 */
export const useSendButtonDisabled = (value: string) => {
  return trim(value) === '';
};

/** URL 中以 data_ 开头的查询键会剥离前缀后注入 Begin 表单。 */
const DATA_PREFIX = 'data_';

/** 分享页 URL 解析结果：来源、sharedId、主题及 data_* 预填字段。 */
interface SharedChatSearchParams {
  from: SharedFrom;
  sharedId: string | null;
  release: string | null;
  locale: string | null;
  theme: string | null;
  data: Record<string, string>;
  visibleAvatar: boolean;
}

/** 从 searchParams 提取分享上下文与 data_ 前缀的键值对。 */
export const useGetSharedChatSearchParams = () => {
  const [searchParams] = useSearchParams();
  const data = Object.fromEntries(
    Array.from(searchParams.entries())
      .filter(([key]) => key.startsWith(DATA_PREFIX))
      .map(([key, value]) => [key.replace(DATA_PREFIX, ''), value]),
  );
  return {
    from: searchParams.get('from') as SharedFrom,
    sharedId: searchParams.get('shared_id'),
    release: searchParams.get('release'),
    locale: searchParams.get('locale'),
    theme: searchParams.get('theme'),
    data,
    visibleAvatar: searchParams.get('visible_avatar')
      ? searchParams.get('visible_avatar') !== '1'
      : true,
  } as SharedChatSearchParams;
};

/** 分享页发送消息：Task 模式无 inputs 时自动 ok([])，否则走参数弹窗。 */
export const useSendNextSharedMessage = (
  addEventList: (data: IEventList, messageId: string) => void,
) => {
  const {
    from,
    sharedId: conversationId,
    release,
  } = useGetSharedChatSearchParams();
  const botType = from === SharedFrom.Agent ? 'agentbots' : 'chatbots';
  const releaseQuery = release ? `?release=${encodeURIComponent(release)}` : '';
  const url = `/api/v1/${botType}/${conversationId}/completions${releaseQuery}`;
  const { data: inputsData } = useFetchExternalAgentInputs();

  const [params, setParams] = useState<BeginQuery[]>([]);
  const sendedTaskMessage = useRef(false);

  // Task 模式跳过对话输入，直接以 beginInputs 触发执行
  const isTaskMode = inputsData.mode === AgentDialogueMode.Task;

  const {
    visible: parameterDialogVisible,
    hideModal: hideParameterDialog,
    showModal: showParameterDialog,
  } = useSetModalState();

  const { handlePressEnter, ...ret } = useSendAgentMessage({
    url,
    addEventList,
    beginParams: params,
    isShared: true,
    isTaskMode,
    releaseMode: release,
  });
  const ok = useCallback(
    (params: BeginQuery[]) => {
      if (isTaskMode) {
        const msgBody = buildRequestBody('');

        ret.sendMessage({
          message: msgBody,
          beginInputs: params,
        });
      } else {
        setParams(params);
      }

      hideParameterDialog();
    },
    [hideParameterDialog, isTaskMode, ret],
  );

  const onPressEnter = useCallback(() => {
    handlePressEnter();
  }, [handlePressEnter]);

  /** Task 且无 inputs 时仅自动发送一次空参数任务。 */
  const runTask = useCallback(() => {
    if (
      isTaskMode &&
      isEmpty(inputsData?.inputs) &&
      !sendedTaskMessage.current
    ) {
      ok([]);
      sendedTaskMessage.current = true;
    }
  }, [inputsData?.inputs, isTaskMode, ok]);

  useEffect(() => {
    runTask();
  }, [runTask]);

  return {
    ...ret,
    hasError: false,
    parameterDialogVisible,
    inputsData,
    isTaskMode,
    hideParameterDialog,
    showParameterDialog,
    ok,
    handlePressEnter: onPressEnter,
  };
};
