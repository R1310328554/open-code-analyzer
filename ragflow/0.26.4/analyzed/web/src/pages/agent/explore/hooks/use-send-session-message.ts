// use-send-session-message.ts — 探索页发消息：会话创建、Begin 参数与开场白。

import sonnerMessage from '@/components/ui/message';
import { useSetModalState } from '@/hooks/common-hooks';
import {
  useCreateAgentSession,
  useFetchAgent,
} from '@/hooks/use-agent-request';
import { useSendAgentMessage } from '@/pages/agent/chat/use-send-agent-message';
import { buildBeginInputListFromObject } from '@/pages/agent/form/begin-form/utils';
import { get, isEmpty } from 'lodash';
import { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import { useParams } from 'react-router';
import { BeginId } from '../../constant';
import { useExploreUrlParams } from './use-explore-url-params';

/** 从画布 DSL 的 Begin 节点读取 enablePrologue 时的 prologue 文案。 */
export const useGetBeginNodePrologue = () => {
  const { data } = useFetchAgent();
  const nodes = get(data, 'dsl.graph.nodes', []);

  return useMemo(() => {
    const beginNode = nodes.find((node: any) => node.id === BeginId);
    const formData: Record<string, any> = get(beginNode, 'data.form', {});
    if (formData?.enablePrologue) {
      return formData?.prologue;
    }
  }, [nodes]);
};

/** 探索聊天发送逻辑：自动建会话、Begin 参数弹窗、开场白与消息流。 */
export const useSendSessionMessage = () => {
  const { setSessionId, sessionId, isNew } = useExploreUrlParams();

  const { data: canvasInfo } = useFetchAgent();

  const { id: canvasId } = useParams();

  const { createAgentSession } = useCreateAgentSession();

  const isCreatingSession = useRef(false);

  const [beginParams, setBeginParams] = useState<any[]>([]);

  const prologue = useGetBeginNodePrologue();

  const {
    visible: parameterDialogVisible,
    hideModal: hideParameterDialog,
    showModal: showParameterDialog,
  } = useSetModalState();

  // 将 Begin 节点 inputs 转为参数表单列表
  const beginInputs = useMemo(() => {
    const beginNode = canvasInfo?.dsl?.graph?.nodes?.find(
      (node: any) => node.id === BeginId,
    );
    const inputs = beginNode?.data?.form?.inputs;
    return buildBeginInputListFromObject(inputs || {});
  }, [canvasInfo]);

  const {
    setDerivedMessages,
    addPrologue,
    derivedMessages,
    handlePressEnter: handleSendPressEnter,
    value,
    ...chatLogic
  } = useSendAgentMessage({
    beginParams,
  });

  /** Begin 参数弹窗确认：写入 beginParams 并关闭弹窗。 */
  const handleParametersOk = useCallback(
    (params: any[]) => {
      setBeginParams(params);
      hideParameterDialog();
    },
    [hideParameterDialog],
  );

  /** 存在 Begin 输入且尚未填参时弹出参数对话框。 */
  const shouldShowParameterDialog = useCallback(() => {
    if (beginInputs.length > 0 && beginParams.length === 0) {
      showParameterDialog();
    }
  }, [beginInputs, beginParams, showParameterDialog]);

  /** 发送入口：补开场白、无 sessionId 时创建会话，再委托 chat 发送。 */
  const handlePressEnter = useCallback(async () => {
    if (isCreatingSession.current) {
      return;
    }

    if (
      prologue &&
      isEmpty(sessionId) &&
      !isNew &&
      derivedMessages.length === 0
    ) {
      addPrologue(prologue);
    }

    let exploreSessionId = sessionId;

    if (isEmpty(sessionId) && canvasId) {
      isCreatingSession.current = true;
      try {
        const sessionName = value?.trim() || 'New Session';
        const result = await createAgentSession({
          id: canvasId,
          name: sessionName,
        });

        exploreSessionId = result.id;

        setSessionId(result.id, false);

        setTimeout(() => {
          isCreatingSession.current = false;
        }, 100);
      } catch (error) {
        isCreatingSession.current = false;
        sonnerMessage.error('Failed to create session');
        console.error('Failed to create session:', error);
        return;
      }
    }

    return handleSendPressEnter?.({ exploreSessionId });
  }, [
    addPrologue,
    canvasId,
    createAgentSession,
    derivedMessages.length,
    handleSendPressEnter,
    isNew,
    prologue,
    sessionId,
    setSessionId,
    value,
  ]);

  useEffect(() => {
    if (isNew && isEmpty(sessionId)) {
      setDerivedMessages([]);
    }
  }, [isNew, sessionId, setDerivedMessages]);

  useEffect(() => {
    if (prologue && isNew && isEmpty(sessionId)) {
      addPrologue(prologue);
    }
  }, [addPrologue, isNew, prologue, sessionId]);

  return {
    ...chatLogic,
    value,
    derivedMessages,
    handlePressEnter,
    canvasInfo,
    parameterDialogVisible,
    handleParametersOk,
    beginInputs,
    shouldShowParameterDialog,
    setDerivedMessages,
  };
};
