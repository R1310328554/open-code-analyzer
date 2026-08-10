package pattern

// pattern_config 配置检测到的模式写回 Loki 的 HTTP 推送参数：地址、周期、TLS、BasicAuth 与 backoff 重试。

import (
	"flag"
	"time"

	"github.com/grafana/dskit/backoff"
	"github.com/prometheus/common/config"
)

// PersistenceConfig 控制模式持久化推送的目标 Loki、批量大小与客户端 TLS/认证。
// PersistenceConfig contains the configuration for pushing detected patterns back to Loki
type PersistenceConfig struct {
	LokiAddr         string                  `yaml:"loki_address,omitempty" doc:"description=The address of the Loki instance to push patterns to."`
	WriteTimeout     time.Duration           `yaml:"timeout,omitempty" doc:"description=The timeout for writing patterns to Loki."`
	PushPeriod       time.Duration           `yaml:"push_period,omitempty" doc:"description=How long to wait between pattern pushes to Loki."`
	HTTPClientConfig config.HTTPClientConfig `yaml:"http_client_config,omitempty" doc:"description=The HTTP client configuration for pushing patterns to Loki."`
	UseTLS           bool                    `yaml:"use_tls,omitempty" doc:"description=Whether to use TLS for pushing patterns to Loki."`
	BasicAuth        BasicAuth               `yaml:"basic_auth,omitempty" doc:"description=The basic auth configuration for pushing patterns to Loki."`
	BackoffConfig    backoff.Config          `yaml:"backoff_config,omitempty" doc:"description=The backoff configuration for pushing patterns to Loki."`
	BatchSize        int                     `yaml:"batch_size,omitempty" doc:"description=The maximum number of patterns to accumulate before pushing."`
}

// RegisterFlags 委托 RegisterFlagsWithPrefix 注册 loki-address/push-period 等 CLI 参数。
// RegisterFlags registers pattern push related flags.
func (cfg *PersistenceConfig) RegisterFlags(fs *flag.FlagSet) {
	cfg.RegisterFlagsWithPrefix(fs, "")
}

func (cfg *PersistenceConfig) RegisterFlagsWithPrefix(fs *flag.FlagSet, prefix string) {
	fs.StringVar(
		&cfg.LokiAddr,
		prefix+"loki-address",
		"",
		"Loki address to send patterns to.",
	)
	fs.DurationVar(
		&cfg.WriteTimeout,
		prefix+"timeout",
		10*time.Second,
		"How long to wait for write response from Loki",
	)
	fs.DurationVar(
		&cfg.PushPeriod,
		prefix+"push-period",
		1*time.Minute,
		"How often to push accumulated patterns to Loki",
	)
	fs.BoolVar(
		&cfg.UseTLS,
		prefix+"tls",
		false,
		"Does the loki connection use TLS?",
	)
	fs.IntVar(
		&cfg.BatchSize,
		prefix+"batch-size",
		1000,
		"Maximum number of patterns to accumulate before pushing",
	)

	cfg.BackoffConfig.RegisterFlagsWithPrefix(prefix+".", fs)
	cfg.BasicAuth.RegisterFlagsWithPrefix(prefix+".", fs)
}

// BasicAuth 存储推送模式样本至 Loki 时使用的 HTTP 基本认证凭据。
// BasicAuth contains basic HTTP authentication credentials.
type BasicAuth struct {
	Username string        `yaml:"username"           json:"username"`
	Password config.Secret `yaml:"password,omitempty" json:"password,omitempty"`
}

func (cfg *BasicAuth) RegisterFlagsWithPrefix(prefix string, fs *flag.FlagSet) {
	fs.StringVar(
		&cfg.Username,
		prefix+"basic-auth.username",
		"",
		"Basic auth username for sending patterns back to Loki.",
	)
	fs.Var(
		newSecretValue(config.Secret(""), &cfg.Password),
		prefix+"basic-auth.password",
		"Basic auth password for sending patterns back to Loki.",
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
// BatchSize 限制单次 push 前内存中累积的模式条目上限，PushPeriod 控制定时 flush 间隔。
