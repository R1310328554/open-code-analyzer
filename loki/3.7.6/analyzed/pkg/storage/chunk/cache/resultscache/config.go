package resultscache

// resultscache 配置封装底层 chunk cache 与可选 snappy 压缩；Validate 要求至少一种 cache 后端且 compression 合法。

import (
	"context"
	"flag"
	"time"

	"github.com/pkg/errors"

	"github.com/grafana/loki/v3/pkg/storage/chunk/cache"
)

// Config 嵌套 cache.Config，Compression 为空表示禁用压缩。
// Config is the config for the results cache.
type Config struct {
	CacheConfig cache.Config `yaml:"cache"`
	Compression string       `yaml:"compression"`
}

func (cfg *Config) RegisterFlagsWithPrefix(f *flag.FlagSet, prefix string) {
	cfg.CacheConfig.RegisterFlagsWithPrefix(prefix, "", f)
	f.StringVar(&cfg.Compression, prefix+"compression", "", "Use compression in cache. The default is an empty value '', which disables compression. Supported values are: 'snappy' and ''.")
}

func (cfg *Config) RegisterFlags(f *flag.FlagSet) {
	cfg.RegisterFlagsWithPrefix(f, "")
}

func (cfg *Config) Validate() error {
	switch cfg.Compression {
	case "snappy", "":
		// valid
	default:
		return errors.Errorf("unsupported compression type: %s", cfg.Compression)
	}

	if !cache.IsCacheConfigured(cfg.CacheConfig) {
		return errors.New("no cache configured")
	}

	return nil
}

// Limits 由租户限制提供 MaxCacheFreshness，控制可缓存数据的最大新鲜度窗口。
type Limits interface {
	MaxCacheFreshness(ctx context.Context, tenantID string) time.Duration
}
// RegisterFlagsWithPrefix 转发 cache 子配置 flag；Validate 拒绝未配置 cache 的启动。
