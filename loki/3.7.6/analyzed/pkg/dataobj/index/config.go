package index

// index 包配置：嵌入 logsobj.BuilderBaseConfig 并扩展 Kafka 批处理与刷盘参数。

import (
	"errors"
	"flag"
	"time"

	"github.com/grafana/loki/v3/pkg/dataobj/consumer/logsobj"
)

type Config struct {
	logsobj.BuilderBaseConfig `yaml:",inline"`
	EventsPerIndex            int           `yaml:"events_per_index" experimental:"true"`
	FlushInterval             time.Duration `yaml:"flush_interval" experimental:"true"`
	MaxIdleTime               time.Duration `yaml:"max_idle_time" experimental:"true"`
}

func (cfg *Config) RegisterFlags(f *flag.FlagSet) {
	cfg.RegisterFlagsWithPrefix("dataobj-index-builder.", f)
}

// RegisterFlagsWithPrefix 注册 builder 基础参数与 events-per-index 等实验性 flag。
func (cfg *Config) RegisterFlagsWithPrefix(prefix string, f *flag.FlagSet) {
	// Set defaults for base builder configuration
	_ = cfg.TargetPageSize.Set("128KB")
	_ = cfg.TargetObjectSize.Set("64MB")
	_ = cfg.BufferSize.Set("2MB")
	_ = cfg.TargetSectionSize.Set("16MB")
	cfg.BuilderBaseConfig.RegisterFlagsWithPrefix(prefix, f)

	f.IntVar(&cfg.EventsPerIndex, prefix+"events-per-index", 32, "Experimental: The number of events to batch before building an index")
	f.DurationVar(&cfg.FlushInterval, prefix+"flush-interval", 1*time.Minute, "Experimental: How often to check for stale partitions to flush")
	f.DurationVar(&cfg.MaxIdleTime, prefix+"max-idle-time", 30*time.Minute, "Experimental: Maximum time to wait before flushing buffered events")
}

// Validate 校验嵌入的 BuilderBaseConfig 与自身字段的合法性。
// Validate validates the BuilderConfig.
func (cfg *Config) Validate() error {
	var errs []error

	if err := cfg.BuilderBaseConfig.Validate(); err != nil {
		errs = append(errs, err)
	}

	return errors.Join(errs...)
}
// 默认 EventsPerIndex 为三十二，MaxIdleTime 三十分钟触发空闲分区刷盘。
