// use-build-webhook-url.ts — 根据当前 Agent 路由 id 拼接 Webhook 回调完整 URL。

import { useParams } from 'react-router';

/** 读取 URL 参数 id，返回 `{protocol}//{host}/api/v1/agents/{id}/webhook`。 */
export function useBuildWebhookUrl() {
  const { id } = useParams();

  const text = `${location.protocol}//${location.host}/api/v1/agents/${id}/webhook`;
  return text;
}
