package queue

// Bloom Planner 任务队列配置：限制每租户排队任务数、是否持久化到磁盘
// 及启动时是否清理任务目录，类型别名复用 pkg/queue 的 Limits 接口。

import (
	"flag"
	"fmt"

	"github.com/grafana/loki/v3/pkg/queue"
)

type Config struct {
	MaxQueuedTasksPerTenant int    `yaml:"max_queued_tasks_per_tenant"`
	StoreTasksOnDisk        bool   `yaml:"store_tasks_on_disk"`
	TasksDiskDirectory      string `yaml:"tasks_disk_directory"`
	CleanTasksDirectory     bool   `yaml:"clean_tasks_directory"`
}

// RegisterFlagsWithPrefix 注册 max-tasks-per-tenant 与 store-tasks-on-disk 等标志。
// RegisterFlagsWithPrefix registers flags for the bloom-planner configuration.
func (cfg *Config) RegisterFlagsWithPrefix(prefix string, f *flag.FlagSet) {
	f.IntVar(&cfg.MaxQueuedTasksPerTenant, prefix+".max-tasks-per-tenant", 30000, "Maximum number of tasks to queue per tenant.")
	f.BoolVar(&cfg.StoreTasksOnDisk, prefix+".store-tasks-on-disk", false, "Whether to store tasks on disk.")
	f.StringVar(&cfg.TasksDiskDirectory, prefix+".tasks-disk-directory", "/tmp/bloom-planner-queue", "Directory to store tasks on disk.")
	f.BoolVar(&cfg.CleanTasksDirectory, prefix+".clean-tasks-directory", false, "Whether to clean the tasks directory on startup.")
}

// Validate 要求启用磁盘存储时必须指定非空 tasks_disk_directory。
func (cfg *Config) Validate() error {
	if cfg.StoreTasksOnDisk && cfg.TasksDiskDirectory == "" {
		return fmt.Errorf("tasks_disk_directory must be set when store_tasks_on_disk is true")
	}

	return nil
}

// Limits 别名指向通用 RequestQueue 的租户消费者限制接口。
type Limits = queue.Limits
