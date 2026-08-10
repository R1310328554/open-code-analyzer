package shardstreams

// shardstreams 配置包：定义 distributor 自动分片流的开关、速率阈值与时间分片策略。

import (
	"flag"
	"time"

	"github.com/grafana/loki/v3/pkg/util/flagext"
)

type Config struct {
	Enabled bool `yaml:"enabled" json:"enabled" doc:"description=Automatically shard streams to keep them under the per-stream rate limit. Sharding is dictated by the desired rate."`

	TimeShardingEnabled bool `yaml:"time_sharding_enabled" json:"time_sharding_enabled" doc:"description=Automatically shard streams by adding a __time_shard__ label, with values calculated from the log timestamps divided by MaxChunkAge/2. This allows the out-of-order ingestion of very old logs. If both flags are enabled, time-based sharding will happen before rate-based sharding."`

	TimeShardingIgnoreRecent time.Duration `yaml:"time_sharding_ignore_recent" json:"time_sharding_ignore_recent" doc:"description=Logs with timestamps that are newer than this value will not be time-sharded."`

	LoggingEnabled bool `yaml:"logging_enabled" json:"logging_enabled" doc:"description=Whether to log sharding streams behavior or not. Not recommended for production environments."`

// DesiredRate 为字节/秒阈值，超过则切分为新 shard 以遵守单流速率限制。
	// DesiredRate is the threshold used to shard the stream into smaller pieces.
	// Expected to be in bytes.
	DesiredRate flagext.ByteSize `yaml:"desired_rate" json:"desired_rate" doc:"description=Threshold used to cut a new shard. Default (1536KB) means if a rate is above 1536KB/s, it will be sharded into two streams."`
}

// RegisterFlagsWithPrefix 注册 shard-streams 相关 CLI/YAML 配置项及默认值。
func (cfg *Config) RegisterFlagsWithPrefix(prefix string, fs *flag.FlagSet) {
	fs.BoolVar(&cfg.Enabled, prefix+".enabled", true, "Automatically shard streams to keep them under the per-stream rate limit")
	fs.BoolVar(&cfg.TimeShardingEnabled, prefix+".time-sharding-enabled", false, "Automatically shard streams by time (in MaxChunkAge/2 buckets), to allow out-of-order ingestion of very old logs.")
	fs.DurationVar(&cfg.TimeShardingIgnoreRecent, prefix+".time-sharding-ignore-recent", 40*time.Minute, "Logs with timestamps that are newer than this value will not be time-sharded.")
	fs.BoolVar(&cfg.LoggingEnabled, prefix+".logging-enabled", false, "Enable logging when sharding streams")
	cfg.DesiredRate.Set("1536KB") //nolint:errcheck
	fs.Var(&cfg.DesiredRate, prefix+".desired-rate", "threshold used to cut a new shard. Default (1536KB) means if a rate is above 1536KB/s, it will be sharded.")
}
// 时间分片与速率分片可同时启用，时间分片优先于速率分片执行。
