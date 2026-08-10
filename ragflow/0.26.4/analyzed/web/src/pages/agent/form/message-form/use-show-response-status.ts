// use-show-response-status.ts — Message 节点：Webhook 流式模式下展示 HTTP 响应状态码字段。

import { isEmpty } from 'lodash';
import { useEffect, useMemo } from 'react';
import { UseFormReturn } from 'react-hook-form';
import {
  AgentDialogueMode,
  BeginId,
  WebhookExecutionMode,
} from '../../constant';
import useGraphStore from '../../store';

/** 读取 Begin 节点 mode/execution_mode，决定是否展示 status 并默认 200。 */
export function useShowWebhookResponseStatus(form: UseFormReturn<any>) {
  const getNode = useGraphStore((state) => state.getNode);

  const formData = getNode(BeginId)?.data.form;

  const isWebhookMode = formData?.mode === AgentDialogueMode.Webhook;

  // Webhook + Streaming 时才需要配置响应状态码
  const showWebhookResponseStatus = useMemo(() => {
    return (
      isWebhookMode &&
      formData?.execution_mode === WebhookExecutionMode.Streaming
    );
  }, [formData?.execution_mode, isWebhookMode]);

  /** status 为空且需展示时，自动填入 200 并触发校验。 */
  useEffect(() => {
    if (showWebhookResponseStatus && isEmpty(form.getValues('status'))) {
      form.setValue('status', 200, { shouldValidate: true, shouldDirty: true });
    }
  }, [form, showWebhookResponseStatus]);

  return { showWebhookResponseStatus, isWebhookMode };
}
