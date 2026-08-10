package rwrulefmt

// rwrulefmt 在 Prometheus rulefmt.RuleGroup 上扩展 remote_write 配置，供 Loki Ruler 将告警/录制结果转发到外部 Prometheus remote write 端点。

import "github.com/prometheus/prometheus/model/rulefmt"

// 本包作为 rulefmt 薄封装，保持 YAML 内联兼容并附加 Loki 专有字段。
// Wrapper around Prometheus rulefmt.

// RuleGroup 内联标准 rulefmt 组定义，并可选携带 RWConfigs remote write 列表。
// RuleGroup is a list of sequentially evaluated recording and alerting rules.
type RuleGroup struct {
	rulefmt.RuleGroup `yaml:",inline"`
	// RWConfigs is used by the remote write forwarding ruler
// RWConfigs 由 forwarding ruler 读取，将匹配规则输出推送到指定 HTTP 端点。
	RWConfigs []RemoteWriteConfig `yaml:"remote_write,omitempty"`
}

// RemoteWriteConfig 仅含 URL 字段，对应规则组级 remote_write YAML 块。
// RemoteWriteConfig is used to specify a remote write endpoint
type RemoteWriteConfig struct {
	URL string `json:"url,omitempty"`
}
// CompareGroups 会比较 RWConfigs 长度与各 URL，防止 sync 时静默丢失转发配置。
