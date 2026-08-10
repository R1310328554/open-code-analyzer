// This directory was copied and adapted from https://github.com/grafana/agent/tree/main/pkg/metrics.
// We cannot vendor the agent in since the agent vendors loki in, which would cause a cyclic dependency.
// NOTE: many changes have been made to the original code for our use-case.

package cleaner

// Config 定义 WAL 清理器的 min_age 与 period，通过 ruler.wal-cleaner 命令行标志注册。

import (
	"flag"
	"time"
)

// MinAge 为候选删除的最小闲置时长；Period 为清理周期，0 表示禁用后台任务。
// Config specifies the configurable settings of the WAL cleaner
type Config struct {
	MinAge time.Duration `yaml:"min_age,omitempty"`
	Period time.Duration `yaml:"period,omitempty"`
}

// RegisterFlags 绑定 ruler.wal-cleaner.min-age 与 period 两个 duration 标志。
func (c *Config) RegisterFlags(f *flag.FlagSet) {
	f.DurationVar(&c.MinAge, "ruler.wal-cleaner.min-age", DefaultCleanupAge, "The minimum age of a WAL to consider for cleaning.")
	f.DurationVar(&c.Period, "ruler.wal-cleaner.period", DefaultCleanupPeriod, "How often to run the WAL cleaner. 0 = disabled.")
}

func (c *Config) Validate() error {
	return nil
}
// Validate 当前恒返回 nil，预留未来对 period/minAge 组合的校验扩展点。
