// Alertmanagers API 响应类型，对应 /api/v1/alertmanagers 端点。

export type AlertmanagerTarget = {
  url: string;
};

// AlertmanagersResult 区分 active 与 dropped 两组 Alertmanager 列表。
// Result type for /api/v1/alertmanagers endpoint.
// See: https://prometheus.io/docs/prometheus/latest/querying/api/#alertmanagers
export type AlertmanagersResult = {
  activeAlertmanagers: AlertmanagerTarget[];
  droppedAlertmanagers: AlertmanagerTarget[];
};
