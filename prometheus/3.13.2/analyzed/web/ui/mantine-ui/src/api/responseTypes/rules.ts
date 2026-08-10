// Rules/Alerts API 响应类型：告警规则、录制规则与规则组，对应 /rules 与 /alerts 端点。

type RuleState = "pending" | "firing" | "inactive" | "unknown";

export interface Alert {
  labels: Record<string, string>;
  state: RuleState;
  value: string;
  annotations: Record<string, string>;
  activeAt: string;
  keepFiringSince: string;
}

// CommonRuleFields 为 alerting/recording 规则共用的元数据字段。
type CommonRuleFields = {
  name: string;
  query: string;
  evaluationTime: string;
  health: "ok" | "unknown" | "err";
  lastError?: string;
  lastEvaluation: string;
};

export type AlertingRule = {
  type: "alerting";
// 告警规则的 labels 字段始终存在，即使未配置额外标签。
  // For alerting rules, the 'labels' field is always present, even when there are no labels.
  labels: Record<string, string>;
  annotations: Record<string, string>;
  duration: number;
  keepFiringFor: number;
  state: RuleState;
  alerts: Alert[];
} & CommonRuleFields;

// RecordingRule 仅含 type=recording，labels 仅在配置时返回。
type RecordingRule = {
  type: "recording";
// 录制规则的 labels 仅在规则定义了标签时才出现在 JSON 中。
  // For recording rules, the 'labels' field is only present when there are labels.
  labels?: Record<string, string>;
} & CommonRuleFields;

export type Rule = AlertingRule | RecordingRule;

export interface RuleGroup {
  name: string;
  file: string;
  interval: string;
  rules: Rule[];
  evaluationTime: string;
  lastEvaluation: string;
}

export type AlertingRuleGroup = Omit<RuleGroup, "rules"> & {
  rules: AlertingRule[];
};

// RulesResult 为 /rules 返回的全部规则组，混合 alerting 与 recording。
// Result type for /api/v1/alerts endpoint.
// See: https://prometheus.io/docs/prometheus/latest/querying/api/#alerts
export interface RulesResult {
  groups: RuleGroup[];
}

// AlertingRulesResult 在 type=alert 查询参数下保证 groups 内均为 AlertingRule。
// Same as RulesResult above, but can be used when the caller ensures via a
// "type=alert" query parameter that all rules are alerting rules.
export interface AlertingRulesResult {
  groups: AlertingRuleGroup[];
}
