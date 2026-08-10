// agent.ts — Agent 画布调试与 Webhook Trace 请求体类型。

/** 单组件调试请求：component_id 与 params 入参。 */
export interface IDebugSingleRequestBody {
  component_id: string;
  params: Record<string, any>;
}

/** Webhook 触发 Trace 轮询：webhook_id 与 since_ts 游标。 */
export interface IAgentWebhookTraceRequest {
  since_ts: number; // 首次请求返回的时间戳，后续轮询增量
  webhook_id: string; // 每次外部 Webhook 请求生成的唯一 ID
}
