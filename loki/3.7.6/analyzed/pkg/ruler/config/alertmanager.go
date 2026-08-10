package config

// ruler config alertmanager 定义租户 Alertmanager URL、DNS 发现、通知队列容量与 notifier TLS/认证客户端选项。

import (
	"flag"
	"time"

	"github.com/grafana/dskit/crypto/tls"
	"github.com/prometheus/prometheus/model/relabel"

	"github.com/grafana/loki/v3/pkg/util"
)

type AlertManagerConfig struct {
	// URL of the Alertmanager to send notifications to.
	AlertmanagerURL string `yaml:"alertmanager_url"`
	// Whether to use DNS SRV records to discover Alertmanager.
	AlertmanagerDiscovery bool `yaml:"enable_alertmanager_discovery"`
	// How long to wait between refreshing the list of Alertmanager based on DNS service discovery.
	AlertmanagerRefreshInterval time.Duration `yaml:"alertmanager_refresh_interval"`
	// Enables the ruler notifier to use the Alertmananger V2 API.
	AlertmanangerEnableV2API bool `yaml:"enable_alertmanager_v2"`
	// Configuration for alert relabeling.
	AlertRelabelConfigs []*relabel.Config `yaml:"alert_relabel_configs,omitempty" doc:"description=List of alert relabel configs."`
	// Capacity of the queue for notifications to be sent to the Alertmanager.
	NotificationQueueCapacity int `yaml:"notification_queue_capacity"`
	// HTTP timeout duration when sending notifications to the Alertmanager.
	NotificationTimeout time.Duration `yaml:"notification_timeout"`
	// Client configs for interacting with the Alertmanager
	Notifier NotifierConfig `yaml:"alertmanager_client,omitempty"`
}

// NotifierConfig 内联 TLS、BasicAuth 与 HeaderAuth 供 HTTP 通知客户端使用。
type NotifierConfig struct {
	TLS        tls.ClientConfig `yaml:",inline"`
	BasicAuth  util.BasicAuth   `yaml:",inline"`
	HeaderAuth util.HeaderAuth  `yaml:",inline"`
}

// RegisterFlags 注册 ruler.alertmanager-client.* 前缀的 TLS 与认证标志。
func (cfg *NotifierConfig) RegisterFlags(f *flag.FlagSet) {
	cfg.TLS.RegisterFlagsWithPrefix("ruler.alertmanager-client", f)
	cfg.BasicAuth.RegisterFlagsWithPrefix("ruler.alertmanager-client.", f)
	cfg.HeaderAuth.RegisterFlagsWithPrefix("ruler.alertmanager-client.", f)
}
// AlertmanagerDiscovery 启用时通过 SRV 记录周期性刷新 Alertmanager 目标列表。
