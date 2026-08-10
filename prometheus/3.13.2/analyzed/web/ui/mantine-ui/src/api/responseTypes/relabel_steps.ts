// Relabel Steps API 响应类型，对应 /api/v1/relabel_steps 端点，用于调试 relabel 规则链。

import { Labels } from "./targets";

export type RelabelStep = {
  rule: { [key: string]: unknown };
  output: Labels;
  keep: boolean;
};

// RelabelStepsResult 为逐步 relabel 模拟结果数组，供 UI 展示每步标签变化。
// Result type for /api/v1/relabel_steps endpoint.
// See: https://prometheus.io/docs/prometheus/latest/querying/api/#relabel_steps
export type RelabelStepsResult = {
  steps: RelabelStep[];
};
