import {
  maybeQuoteLabelName,
  metricContainsExtendedCharset,
} from "../promql/utils";
import { escapeString } from "./escapeString";

// formatSeries 将标签 map 格式化为 PromQL 向量选择器或扩展字符集的 {...} 语法。
// TODO: Maybe replace this with the new PromLens-derived serialization code in src/promql/serialize.ts?
export const formatSeries = (labels: { [key: string]: string }): string => {
  if (labels === null) {
    return "scalar";
  }

  if (metricContainsExtendedCharset(labels.__name__ || "")) {
    return `{"${escapeString(labels.__name__)}",${Object.entries(labels)
      .filter(([k]) => k !== "__name__")
      .map(([k, v]) => `${maybeQuoteLabelName(k)}="${escapeString(v)}"`)
      .join(", ")}}`;
  }

  return `${labels.__name__ || ""}{${Object.entries(labels)
    .filter(([k]) => k !== "__name__")
    .map(([k, v]) => `${maybeQuoteLabelName(k)}="${escapeString(v)}"`)
    .join(", ")}}`;
};
