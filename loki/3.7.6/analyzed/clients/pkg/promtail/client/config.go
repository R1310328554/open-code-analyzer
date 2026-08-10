package client

// Promtail HTTP 推送客户端配置：URL、batch 窗口/大小、退避、外部标签与多租户。
// 默认值与 Helm chart 应对齐；支持 YAML 与已弃用的 CLI flag。

import (
	"flag"
	"time"

	"github.com/grafana/dskit/backoff"
	"github.com/grafana/dskit/flagext"
	"github.com/prometheus/common/config"

	lokiflag "github.com/grafana/loki/v3/pkg/util/flagext"
)

// 修改默认 batch/退避/超时时需同步 Helm chart 与 fluent-bit 默认值。
// NOTE the helm chart for promtail and fluent-bit also have defaults for these values, please update to match if you make changes here.
const (
	BatchWait      = 1 * time.Second
	BatchSize  int = 1024 * 1024
	MinBackoff     = 500 * time.Millisecond
	MaxBackoff     = 5 * time.Minute
	MaxRetries int = 10
	Timeout        = 10 * time.Second
)

// 单 client 块：BatchWait/BatchSize、HTTP 客户端、Headers、TenantID 与限流丢弃策略。
// Config describes configuration for an HTTP pusher client.
type Config struct {
	// Note even though the command line flag arguments which use this config
	// are deprecated, this struct is still the primary way to configure
	// a promtail client

	Name      string `yaml:"name,omitempty"`
	URL       flagext.URLValue
	BatchWait time.Duration `yaml:"batchwait"`
	BatchSize int           `yaml:"batchsize"`

	Client  config.HTTPClientConfig `yaml:",inline"`
	Headers map[string]string       `yaml:"headers,omitempty"`

	BackoffConfig backoff.Config `yaml:"backoff_config"`
	// The labels to add to any time series or alerts when communicating with loki
	ExternalLabels lokiflag.LabelSet `yaml:"external_labels,omitempty"`
	Timeout        time.Duration     `yaml:"timeout"`

	// The tenant ID to use when pushing logs to Loki (empty string means
	// single tenant mode)
	TenantID string `yaml:"tenant_id"`

	// When enabled, Promtail will not retry batches that get a
	// 429 'Too Many Requests' response from the distributor. Helps
	// prevent HOL blocking in multitenant deployments.
	DropRateLimitedBatches bool `yaml:"drop_rate_limited_batches"`
}

// 注册带前缀的 client.* 命令行 flag（均已标记 deprecated）。
// RegisterFlags with prefix registers flags where every name is prefixed by
// prefix. If prefix is a non-empty string, prefix should end with a period.
func (c *Config) RegisterFlagsWithPrefix(prefix string, f *flag.FlagSet) {
	f.Var(&c.URL, prefix+"client.url", "URL of log server (deprecated).")
	f.DurationVar(&c.BatchWait, prefix+"client.batch-wait", BatchWait, "Maximum wait period before sending batch (deprecated).")
	f.IntVar(&c.BatchSize, prefix+"client.batch-size-bytes", BatchSize, "Maximum batch size to accrue before sending (deprecated).")
	// Default backoff schedule: 0.5s, 1s, 2s, 4s, 8s, 16s, 32s, 64s, 128s, 256s(4.267m) For a total time of 511.5s(8.5m) before logs are lost
	f.IntVar(&c.BackoffConfig.MaxRetries, prefix+"client.max-retries", MaxRetries, "Maximum number of retires when sending batches (deprecated).")
	f.DurationVar(&c.BackoffConfig.MinBackoff, prefix+"client.min-backoff", MinBackoff, "Initial backoff time between retries (deprecated).")
	f.DurationVar(&c.BackoffConfig.MaxBackoff, prefix+"client.max-backoff", MaxBackoff, "Maximum backoff time between retries (deprecated).")
	f.DurationVar(&c.Timeout, prefix+"client.timeout", Timeout, "Maximum time to wait for server to respond to a request (deprecated).")
	f.Var(&c.ExternalLabels, prefix+"client.external-labels", "list of external labels to add to each log (e.g: --client.external-labels=lb1=v1,lb2=v2) (deprecated).")

	f.StringVar(&c.TenantID, prefix+"client.tenant-id", "", "Tenant ID to use when pushing logs to Loki (deprecated).")
	f.BoolVar(&c.DropRateLimitedBatches, prefix+"client.drop-rate-limited-batches", false, "Do not retry batches that have been rate limited by Loki (deprecated).")
}

// 无前缀注册全部 client 相关 flag。
// RegisterFlags registers flags.
func (c *Config) RegisterFlags(flags *flag.FlagSet) {
	c.RegisterFlagsWithPrefix("", flags)
}

// YAML 反序列化：未设 URL 时填充默认退避与 batch，并校验 HTTPClientConfig。
// UnmarshalYAML implement Yaml Unmarshaler
func (c *Config) UnmarshalYAML(unmarshal func(interface{}) error) error {
	type raw Config
	var cfg raw
	if c.URL.URL != nil {
		// we used flags to set that value, which already has sane default.
		cfg = raw(*c)
	} else {
		// force sane defaults.
		cfg = raw{
			BackoffConfig: backoff.Config{
				MaxBackoff: MaxBackoff,
				MaxRetries: MaxRetries,
				MinBackoff: MinBackoff,
			},
			BatchSize: BatchSize,
			BatchWait: BatchWait,
			Timeout:   Timeout,
		}
	}

	cfg.Client = config.DefaultHTTPClientConfig

	if err := unmarshal(&cfg); err != nil {
		return err
	}

	// explicitly call Validate on HTTPClientConfig as it's UnmarshalYAML
	// method doesn't get invoked given that it's not a pointer.
	if err := cfg.Client.Validate(); err != nil {
		return err
	}

	*c = Config(cfg)
	return nil
}
