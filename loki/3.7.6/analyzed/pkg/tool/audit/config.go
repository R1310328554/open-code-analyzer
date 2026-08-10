package audit

// audit 包为 lokitool audit 子命令提供配置结构：加载 schema/storage 与租户、并发度、工作目录及索引 period 等审计参数。

import (
	"flag"
	"fmt"

	"github.com/grafana/dskit/flagext"
	dskitlog "github.com/grafana/dskit/log"

	"github.com/grafana/loki/v3/pkg/storage"
	lokiStorage "github.com/grafana/loki/v3/pkg/storage/config"
)

// FileConfig 指定从磁盘加载 YAML 配置文件的路径。
type FileConfig struct {
	ConfigFile string
}

func (c *FileConfig) RegisterFlags(f *flag.FlagSet) {
	f.StringVar(&c.ConfigFile, "config.file", "config.yaml", "configuration file to load")
}

// Config 聚合 Loki 存储、schema、租户与审计运行时参数，供 index 完整性检查使用。
// Config Loki related storage and schema configs
type Config struct {
	FileConfig    `yaml:",inline"`
	Tenant        string                   `yaml:"tenant,omitempty"`
	SchemaConfig  lokiStorage.SchemaConfig `yaml:"schema_config,omitempty"`
	StorageConfig storage.Config           `yaml:"storage_config,omitempty"`
	LogLevel      dskitlog.Level           `yaml:"log_level"`
	Concurrency   int                      `yaml:"concurrency"`
	WorkingDir    string                   `yaml:"working_dir"`
	Period        string                   `yaml:"period,omitempty"`
}

// RegisterFlags 将 Config 各字段注册到 kingpin/flag，支持 CLI 与配置文件双通道。
func (c *Config) RegisterFlags(f *flag.FlagSet) {
	c.FileConfig.RegisterFlags(f)
	c.SchemaConfig.RegisterFlags(f)
	c.StorageConfig.RegisterFlags(f)
	c.LogLevel.RegisterFlags(f)
	f.StringVar(&c.Tenant, "tenant", "", "tenant to analyze data")
	f.IntVar(&c.Concurrency, "concurrency", 100, "amount of files to check concurrently")
	f.StringVar(&c.WorkingDir, "working-dir", ".", "working directory to store downloaded files")
	f.StringVar(&c.Period, "period", "", "the table period in a format like 19959")
}

func (c *Config) Validate() error {
	if err := c.SchemaConfig.Validate(); err != nil {
		return fmt.Errorf("schema config is invalid: %v", err)
	}
	if err := c.StorageConfig.Validate(); err != nil {
		return fmt.Errorf("storage config is invalid: %v", err)
	}
	if len(c.Tenant) <= 0 {
		return fmt.Errorf("tenant argument missing. Use -tenant flag or add 'tenant' to the config file")
	}
	if c.Concurrency <= 0 {
		return fmt.Errorf("concurrency argument needs to be greater than 0")
	}
	if c.Period == "" {
		return fmt.Errorf("period argument missing. Use -period flag or add 'period' to the config file")
	}
	return nil
}

// Clone 利用值拷贝返回独立 Config，便于解析另一套 flag 而不污染原实例。
// Clone takes advantage of pass-by-value semantics to return a distinct *Config.
// This is primarily used to parse a different flag set without mutating the original *Config.
func (c *Config) Clone() flagext.Registerer {
	return func(c Config) *Config {
		return &c
	}(*c)
}
// Validate 校验 schema/storage、租户、并发度与 period 必填项，失败则阻止审计启动。
