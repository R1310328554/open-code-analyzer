package aggregation

// aggregation.Config 配置 pattern ingester 的采样、降采样与向 Loki 推送聚合指标的 HTTP 客户端参数。

import (
	"flag"
	"time"

	"github.com/grafana/dskit/backoff"
	"github.com/prometheus/common/config"
)

type Config struct {
	SamplePeriod     time.Duration           `yaml:"sample_period"`
	LokiAddr         string                  `yaml:"loki_address,omitempty" doc:"description=The address of the Loki instance to push aggregated metrics to."`
	WriteTimeout     time.Duration           `yaml:"timeout,omitempty" doc:"description=The timeout for writing to Loki."`
	PushPeriod       time.Duration           `yaml:"push_period,omitempty" doc:"description=How long to wait in between pushes to Loki."`
	HTTPClientConfig config.HTTPClientConfig `yaml:"http_client_config,omitempty" doc:"description=The HTTP client configuration for pushing metrics to Loki."`
	UseTLS           bool                    `yaml:"use_tls,omitempty" doc:"description=Whether to use TLS for pushing metrics to Loki."`
	BasicAuth        BasicAuth               `yaml:"basic_auth,omitempty" doc:"description=The basic auth configuration for pushing metrics to Loki."`
	BackoffConfig    backoff.Config          `yaml:"backoff_config,omitempty" doc:"description=The backoff configuration for pushing metrics to Loki."`
}

// RegisterFlags 委托 RegisterFlagsWithPrefix 注册 downsample-period 等 CLI 参数。
// RegisterFlags registers pattern ingester related flags.
func (cfg *Config) RegisterFlags(fs *flag.FlagSet) {
	cfg.RegisterFlagsWithPrefix(fs, "")
}

func (cfg *Config) RegisterFlagsWithPrefix(fs *flag.FlagSet, prefix string) {
	fs.DurationVar(
		&cfg.SamplePeriod,
		prefix+"downsample-period",
		10*time.Second,
		"How often to sample metrics and patterns from raw push observations.",
	)
	fs.StringVar(
		&cfg.LokiAddr,
		prefix+"loki-address",
		"",
		"Loki address to send aggregations to.",
	)
	fs.DurationVar(
		&cfg.WriteTimeout,
		prefix+"timeout",
		10*time.Second,
		"How long to wait write response from Loki",
	)
	fs.DurationVar(
		&cfg.PushPeriod,
		prefix+"push-period",
		30*time.Second,
		"How long to wait write response from Loki",
	)
	fs.BoolVar(
		&cfg.UseTLS,
		prefix+"tls",
		false,
		"Does the loki connection use TLS?",
	)

	cfg.BackoffConfig.RegisterFlagsWithPrefix(prefix+".", fs)
	cfg.BasicAuth.RegisterFlagsWithPrefix(prefix+".", fs)
}

// BasicAuth 存储推送聚合指标至 Loki 时使用的 HTTP 基本认证用户名与 Secret 密码。
// BasicAuth contains basic HTTP authentication credentials.
type BasicAuth struct {
	Username string `yaml:"username"           json:"username"`
	// UsernameFile string `yaml:"username_file,omitempty" json:"username_file,omitempty"`
	Password config.Secret `yaml:"password,omitempty" json:"password,omitempty"`
	// PasswordFile string `yaml:"password_file,omitempty" json:"password_file,omitempty"`
}

func (cfg *BasicAuth) RegisterFlagsWithPrefix(prefix string, fs *flag.FlagSet) {
	fs.StringVar(
		&cfg.Username,
		prefix+"basic-auth.username",
		"",
		"Basic auth username for sending aggregations back to Loki.",
	)
	fs.Var(
		newSecretValue(config.Secret(""), &cfg.Password),
		prefix+"basic-auth.password",
		"Basic auth password for sending aggregations back to Loki.",
	)
}

// secretValue 包装 config.Secret 以满足 flag.Value 接口，避免密码明文打印。
type secretValue string

func newSecretValue(val config.Secret, p *config.Secret) *secretValue {
	*p = val
	return (*secretValue)(p)
}

func (s *secretValue) Set(val string) error {
	*s = secretValue(val)
	return nil
}

func (s *secretValue) Get() any { return string(*s) }

func (s *secretValue) String() string { return string(*s) }

// Limits 按租户开关 MetricAggregation 与 PatternPersistence 功能。
type Limits interface {
	MetricAggregationEnabled(userID string) bool
	PatternPersistenceEnabled(userID string) bool
}
// PushPeriod/WriteTimeout 控制批量推送节奏与单次 HTTP 写入超时；BackoffConfig 处理推送失败重试。
