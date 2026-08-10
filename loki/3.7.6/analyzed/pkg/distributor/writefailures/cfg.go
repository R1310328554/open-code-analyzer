package writefailures

// writefailures 包配置：控制 distributor 写入失败日志的速率限制与 insights 标签。

import (
	"flag"

	"github.com/grafana/loki/v3/pkg/util/flagext"
)

type Cfg struct {
	LogRate flagext.ByteSize `yaml:"rate"`

	AddInsightsLabel bool `yaml:"add_insights_label"`
}

// RegisterFlagsWithPrefix 注册 write-failures 子系统的 rate 与 add-insights-label 标志。
// RegisterFlags registers distributor-related flags.
func (cfg *Cfg) RegisterFlagsWithPrefix(prefix string, fs *flag.FlagSet) {
	_ = cfg.LogRate.Set("1KB")
	fs.Var(&cfg.LogRate, prefix+".rate", "Log volume allowed (per second). Default: 1KB.")

	fs.BoolVar(&cfg.AddInsightsLabel, prefix+".add-insights-label", false, "Whether a insight=true key should be logged or not. Default: false.")
}
// 默认 LogRate 为 1KB/s，防止高故障率租户刷屏 distributor 日志。
