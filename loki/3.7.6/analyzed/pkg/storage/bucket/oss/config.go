package oss

// oss 包定义阿里云 OSS 后端配置：endpoint、bucket 名称及 AccessKey 凭证，通过 RegisterFlags 暴露 CLI 参数。

import (
	"flag"

	"github.com/grafana/dskit/flagext"
)

// Config 保存 OSS 连接四元组；access_key_secret 使用 flagext.Secret 避免日志泄露。
// Config holds the configuration for Alibaba Cloud OSS client
type Config struct {
	Endpoint        string         `yaml:"endpoint"`
	Bucket          string         `yaml:"bucket"`
	AccessKeyID     string         `yaml:"access_key_id"`
	AccessKeySecret flagext.Secret `yaml:"access_key_secret"`
}

// RegisterFlags 注册无前缀的 oss.* 命令行标志。
// RegisterFlags registers the flags for Alibaba Cloud OSS storage config
func (cfg *Config) RegisterFlags(f *flag.FlagSet) {
	cfg.RegisterFlagsWithPrefix("", f)
}

// RegisterFlagsWithPrefix 绑定 oss.bucketname、oss.endpoint 与访问密钥相关 flag。
// RegisterFlagsWithPrefix registers the flags for Alibaba Cloud OSS storage config with prefix
func (cfg *Config) RegisterFlagsWithPrefix(prefix string, f *flag.FlagSet) {
	f.StringVar(&cfg.Bucket, prefix+"oss.bucketname", "", "Name of OSS bucket.")
	f.StringVar(&cfg.Endpoint, prefix+"oss.endpoint", "", "Endpoint to connect to.")
	f.StringVar(&cfg.AccessKeyID, prefix+"oss.access-key-id", "", "alibabacloud Access Key ID")
	f.Var(&cfg.AccessKeySecret, prefix+"oss.access-key-secret", "alibabacloud Secret Access Key")
}
// OSS 后端在 bucket 包 SupportedBackends 中注册，与 S3 兼容 API 不同需单独配置块。
