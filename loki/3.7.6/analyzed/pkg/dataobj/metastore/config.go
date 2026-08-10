package metastore

// config 定义 metastore 实验性配置：索引存储前缀与日志/元存储分区比例。

import (
	"flag"
	fmt "fmt"
)

// Config 承载 YAML 与命令行 flag 的 metastore 参数。
// Config is the configuration block for the metastore settings.
type Config struct {
	IndexStoragePrefix string `yaml:"index_storage_prefix" experimental:"true"`
	PartitionRatio     int    `yaml:"partition_ratio" experimental:"true"`
}

// RegisterFlags 注册 dataobj-metastore.* 前缀的命令行选项。
// RegisterFlags registers the flags for the metastore settings.
func (c *Config) RegisterFlags(f *flag.FlagSet) {
	prefix := "dataobj-metastore."
	f.StringVar(&c.IndexStoragePrefix, prefix+"index-storage-prefix", "index/v0", "Experimental: A prefix to use for storing indexes in object storage. Used for testing only.")
	f.IntVar(&c.PartitionRatio, prefix+"partition-ratio", 10, "Experimental: The ratio of log partitions to metastore partitions. For example, a value of 10 means there is 1 metastore partition for every 10 log partitions.")
}

// Validate 要求 partition_ratio 为正，否则返回配置错误。
// Validate validates the metastore settings.
func (c *Config) Validate() error {
	if c.PartitionRatio <= 0 {
		return fmt.Errorf("partition_ratio must be greater than 0, got %d", c.PartitionRatio)
	}
	return nil
}
// IndexStoragePrefix 默认 index/v0，PartitionRatio 默认 10 表示十比一映射。
