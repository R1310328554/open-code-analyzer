package planner

// Bloom Planner 配置与租户限流适配：定义规划周期、日表偏移范围、
// 保留策略及任务队列参数，并将 Limits 接口映射为队列消费者上限。

import (
	"flag"
	"fmt"
	"time"

	"github.com/grafana/loki/v3/pkg/bloombuild/planner/queue"
	"github.com/grafana/loki/v3/pkg/bloombuild/planner/strategies"
)

// Config 控制规划间隔、可构建的日表窗口及 retention/queue 嵌套配置。
// Config configures the bloom-planner component.
type Config struct {
	PlanningInterval time.Duration   `yaml:"planning_interval"`
	MinTableOffset   int             `yaml:"min_table_offset"`
	MaxTableOffset   int             `yaml:"max_table_offset"`
	RetentionConfig  RetentionConfig `yaml:"retention"`
	Queue            queue.Config    `yaml:"queue"`
}

// RegisterFlagsWithPrefix 以给定前缀注册 interval、table-offset 等标志。
// RegisterFlagsWithPrefix registers flags for the bloom-planner configuration.
func (cfg *Config) RegisterFlagsWithPrefix(prefix string, f *flag.FlagSet) {
	f.DurationVar(&cfg.PlanningInterval, prefix+".interval", 8*time.Hour, "Interval at which to re-run the bloom creation planning.")
	f.IntVar(&cfg.MinTableOffset, prefix+".min-table-offset", 0, "Newest day-table offset (from today, inclusive) to build blooms for. 0 start building from today, 1 from yesterday and so on. Increase to lower cost by not re-writing data to object storage too frequently since recent data changes more often at the cost of not having blooms available as quickly.")
	// TODO(owen-d): ideally we'd set this per tenant based on their `reject_old_samples_max_age` setting,
	// but due to how we need to discover tenants, we can't do that yet. Tenant+Period discovery is done by
	// iterating the table periods in object storage and looking for tenants within that period.
	// In order to have this done dynamically, we'd need to account for tenant specific overrides, which are also
	// dynamically reloaded.
	// I'm doing it the simple way for now.
	f.IntVar(&cfg.MaxTableOffset, prefix+".max-table-offset", 1, "Oldest day-table offset (from today, inclusive) to build blooms for. 1 till yesterday, 2 till day before yesterday and so on. This can be used to lower cost by not trying to build blooms for older data which doesn't change. This can be optimized by aligning it with the maximum `reject_old_samples_max_age` setting of any tenant.")
	cfg.RetentionConfig.RegisterFlagsWithPrefix(prefix+".retention", f)
	cfg.Queue.RegisterFlagsWithPrefix(prefix+".queue", f)
}

func (cfg *Config) Validate() error {
	if cfg.MinTableOffset > cfg.MaxTableOffset {
		return fmt.Errorf("min-table-offset (%d) must be less than or equal to max-table-offset (%d)", cfg.MinTableOffset, cfg.MaxTableOffset)
	}

	if err := cfg.RetentionConfig.Validate(); err != nil {
		return err
	}

	if err := cfg.Queue.Validate(); err != nil {
		return err
	}

	return nil
}

// Limits 聚合 retention、规划策略与 builder 超时/重试等租户级限制。
type Limits interface {
	RetentionLimits
	strategies.Limits
	BloomCreationEnabled(tenantID string) bool
	BloomBuildMaxBuilders(tenantID string) int
	BuilderResponseTimeout(tenantID string) time.Duration
	BloomTaskMaxRetries(tenantID string) int
}

// QueueLimits 将租户 BloomBuildMaxBuilders 限制桥接到通用任务队列。
type QueueLimits struct {
	limits Limits
}

func NewQueueLimits(limits Limits) *QueueLimits {
	return &QueueLimits{limits: limits}
}

// MaxConsumers 计算某租户最多可占用的 builder 数量，0 表示不限制。
// MaxConsumers is used to compute how many of the available builders are allowed to handle tasks for a given tenant.
// 0 is returned when neither limits are applied. 0 means all builders can be used.
func (c *QueueLimits) MaxConsumers(tenantID string, allConsumers int) int {
	if c == nil || c.limits == nil {
		return 0
	}

	maxBuilders := c.limits.BloomBuildMaxBuilders(tenantID)
	if maxBuilders == 0 {
		return 0
	}

	return min(allConsumers, maxBuilders)
}
