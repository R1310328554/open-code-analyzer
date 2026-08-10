package rulestore

// rulestore.Config 配置 ruler 规则持久化后端：local 目录或 bucket 对象存储，RegisterFlags 注册 ruler-storage 前缀命令行项。

import (
	"flag"
	"fmt"
	"reflect"
	"strings"

	"github.com/grafana/dskit/flagext"

	"github.com/grafana/loki/v3/pkg/ruler/rulestore/local"
	"github.com/grafana/loki/v3/pkg/storage/bucket"
)

// Config configures a rule store.
// Config 内联 bucket.Config，Backend 默认 filesystem，Local 为本地目录选项。
type Config struct {
	bucket.Config `yaml:",inline"`
	Backend       string       `yaml:"backend"`
	Local         local.Config `yaml:"local"`
}

// RegisterFlags 将 local 加入 ExtraBackends 并暴露 SupportedBackends 列表。
// RegisterFlags registers the backend storage config.
func (cfg *Config) RegisterFlags(f *flag.FlagSet) {
	prefix := "ruler-storage."

	cfg.ExtraBackends = []string{local.Name}
	cfg.Local.RegisterFlagsWithPrefix(prefix, f)
	f.StringVar(&cfg.Backend, prefix+"backend", "filesystem", fmt.Sprintf("Backend storage to use. Supported backends are: local, %s", strings.Join(bucket.SupportedBackends, ", ")))
	cfg.RegisterFlagsWithPrefix(prefix, f)
}

// IsDefaults 与 flagext 默认值 DeepEqual，用于判断是否显式配置存储。
// IsDefaults returns true if the storage options have not been set.
func (cfg *Config) IsDefaults() bool {
	defaults := Config{}
	flagext.DefaultValues(&defaults)

	return reflect.DeepEqual(*cfg, defaults)
}
// 运行时由 ruler 根据 Backend 字段选择 local.Client 或 bucket/object 实现。
