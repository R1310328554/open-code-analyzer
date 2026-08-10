package config

// dataobj 顶层配置：聚合 consumer、index、metastore 与存储前缀。

import (
	"flag"

	"github.com/grafana/loki/v3/pkg/dataobj/consumer"
	"github.com/grafana/loki/v3/pkg/dataobj/index"
	"github.com/grafana/loki/v3/pkg/dataobj/metastore"
)

type Config struct {
	Consumer  consumer.Config  `yaml:"consumer"`
	Index     index.Config     `yaml:"index"`
	Metastore metastore.Config `yaml:"metastore"`
	// StorageBucketPrefix is the prefix to use for the storage bucket.
	StorageBucketPrefix string `yaml:"storage_bucket_prefix"`
	Enabled             bool   `yaml:"enabled"`
}

// RegisterFlags 注册 dataobj 相关命令行 flag 并委托子配置。
func (cfg *Config) RegisterFlags(f *flag.FlagSet) {
	cfg.Consumer.RegisterFlags(f)
	cfg.Index.RegisterFlags(f)
	cfg.Metastore.RegisterFlags(f)
	f.StringVar(
		&cfg.StorageBucketPrefix,
		"dataobj-storage-bucket-prefix",
		"dataobj/",
		"The prefix to use for the storage bucket.",
	)
	f.BoolVar(
		&cfg.Enabled,
		"dataobj.enabled",
		false,
		"Enable data objects.",
	)
}

// Validate 在 enabled 时校验 consumer/index/metastore 子配置。
func (cfg *Config) Validate() error {
	if !cfg.Enabled {
		// Do not validate configuration if disabled.
		return nil
	}
	if err := cfg.Consumer.Validate(); err != nil {
		return err
	}
	if err := cfg.Index.Validate(); err != nil {
		return err
	}
	if err := cfg.Metastore.Validate(); err != nil {
		return err
	}
	return nil
}
// 未启用 dataobj 时 Validate 直接跳过子模块校验。
