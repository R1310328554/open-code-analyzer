package wal

// Promtail WAL 配置：开关、目录、旧段清理阈值及 Watcher 轮询频率的
// YAML 结构体与默认值；WAL 功能仍为 WIP，生产环境勿启用。

import (
	"time"
)

const (
	defaultMaxSegmentAge = time.Hour
)

// Watcher 默认轮询间隔：最小 250ms，最大 1s，配合指数退避使用。
// DefaultWatchConfig is the opinionated defaults for operating the Watcher.
var DefaultWatchConfig = WatchConfig{
	MinReadFrequency: time.Millisecond * 250,
	MaxReadFrequency: time.Second,
}

// Config contains all WAL-related settings.
// WAL 总配置：Enabled、Dir、MaxSegmentAge 与嵌套 WatchConfig。
type Config struct {
	// Whether WAL-support should be enabled.
	//
	// WAL support is a WIP. Do not enable in production setups until https://github.com/grafana/loki/issues/8197
	// is finished.
	Enabled bool `yaml:"enabled"`

	// Path where the WAL is written to.
	Dir string `yaml:"dir"`

	// MaxSegmentAge is threshold at which a WAL segment is considered old enough to be cleaned up. Default: 1h.
	//
	// Note that this functionality will likely be deprecated in favour of a programmatic cleanup mechanism.
	MaxSegmentAge time.Duration `yaml:"cleanSegmentsOlderThan"`

	WatchConfig WatchConfig `yaml:"watchConfig"`
}

// 控制 Watcher 在 Writer 通知与定时器双机制下的最小/最大读频率。
// WatchConfig allows the user to configure the Watcher.
//
// For the read frequency settings, the Watcher polls the WAL for new records with two mechanisms: First, it gets
// notified by the Writer when the WAL is written; also, it has a timer that gets fired every so often. This last
// one, implements and exponential back-off strategy to prevent the Watcher from doing read too often, if there's no new
// data.
type WatchConfig struct {
	// MinReadFrequency controls the minimum read frequency the Watcher polls the WAL for new records. If the poll is successful,
	// the frequency will remain the same. If not, it will be incremented using an exponential backoff.
	MinReadFrequency time.Duration `yaml:"minReadFrequency"`

	// MaxReadFrequency controls the maximum read frequency the Watcher polls the WAL for new records. As mentioned above
	// it caps the polling frequency to a maximum, to prevent to exponential backoff from making it too high.
	MaxReadFrequency time.Duration `yaml:"maxReadFrequency"`
}

// 反序列化前填充 MaxSegmentAge=1h 与 DefaultWatchConfig 默认值。
// UnmarshalYAML implement YAML Unmarshaler
func (c *Config) UnmarshalYAML(unmarshal func(interface{}) error) error {
	// Apply defaults
	c.MaxSegmentAge = defaultMaxSegmentAge
	c.WatchConfig = DefaultWatchConfig
	type plain Config
	return unmarshal((*plain)(c))
}
