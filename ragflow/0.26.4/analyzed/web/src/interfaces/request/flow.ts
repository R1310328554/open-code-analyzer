// flow.ts — Agent 画布/流程单组件调试请求体类型。

/** 单组件调试请求：component_id 与 params 入参数组。 */
export interface IDebugSingleRequestBody {
  component_id: string;
  params: any[];
}
