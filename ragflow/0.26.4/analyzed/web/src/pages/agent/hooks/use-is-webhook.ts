// use-is-webhook.ts — 读取 Begin 节点 mode，判断 Webhook 或 Conversational 对话模式。

import { AgentDialogueMode, BeginId } from '../constant';
import useGraphStore from '../store';

/** Begin 节点 form.mode 为 Webhook 时返回 true。 */
export function useIsWebhookMode() {
  const getNode = useGraphStore((state) => state.getNode);

  // 固定读取画布上的 Begin 起始节点
  const beginNode = getNode(BeginId);

  return beginNode?.data.form?.mode === AgentDialogueMode.Webhook;
}

/** Begin 节点 form.mode 为 Conversational 时返回 true。 */
export function useIsConversationMode() {
  const getNode = useGraphStore((state) => state.getNode);

  const beginNode = getNode(BeginId);

  return beginNode?.data.form?.mode === AgentDialogueMode.Conversational;
}
