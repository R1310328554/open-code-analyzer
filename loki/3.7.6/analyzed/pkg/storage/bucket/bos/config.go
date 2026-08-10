package bos

// config 定义百度 BOS 后端配置：bucket 名、endpoint、BCE AccessKey 与 SecretKey。

import (
	"flag"

	"github.com/grafana/dskit/flagext"
)

// Config 通过 yaml 与 flag 双通道配置，SecretKey 使用 flagext.Secret 避免日志泄露。
// Config holds the configuration for Baidu Cloud BOS client
type Config struct {
	Bucket    string         `yaml:"bucket"`
	Endpoint  string         `yaml:"endpoint"`
	AccessKey string         `yaml:"access_key"`
	SecretKey flagext.Secret `yaml:"secret_key"`
}

// RegisterFlags 无前缀注册 bos.* 标志，供 Loki 顶层 storage 配置引用。
func (cfg *Config) RegisterFlags(f *flag.FlagSet) {
	cfg.RegisterFlagsWithPrefix("", f)
}

func (cfg *Config) RegisterFlagsWithPrefix(prefix string, f *flag.FlagSet) {
	f.StringVar(&cfg.Bucket, prefix+"bos.bucket", "", "Name of BOS bucket.")
	f.StringVar(&cfg.Endpoint, prefix+"bos.endpoint", "", "BOS endpoint to connect to.")
	f.StringVar(&cfg.AccessKey, prefix+"bos.access-key", "", "Baidu Cloud Engine (BCE) Access Key ID.")
	f.Var(&cfg.SecretKey, prefix+"bos.secret-key", "Baidu Cloud Engine (BCE) Secret Access Key.")
}
// RegisterFlagsWithPrefix 允许嵌套配置块为 BOS 标志加统一前缀。
