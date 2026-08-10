package oss

// oss 包封装阿里云 OSS 对象存储客户端：将 Loki Config 映射到 Thanos objstore OSS provider 并创建 Bucket 实例。

import (
	"github.com/go-kit/log"
	"github.com/thanos-io/objstore"
	"github.com/thanos-io/objstore/providers/oss"
)

// NewBucketClient 组装 endpoint、bucket 与 AK/SK，调用 oss.NewBucketWithConfig 创建客户端。
// NewBucketClient creates a new Alibaba Cloud OSS bucket client
func NewBucketClient(cfg Config, component string, logger log.Logger) (objstore.Bucket, error) {
	ossCfg := oss.Config{
		Endpoint:        cfg.Endpoint,
		Bucket:          cfg.Bucket,
		AccessKeyID:     cfg.AccessKeyID,
		AccessKeySecret: cfg.AccessKeySecret.String(),
	}
	return oss.NewBucketWithConfig(logger, ossCfg, component, nil)
}
// component 参数传入 Thanos 指标标签；AccessKeySecret 经 String() 解密后写入底层配置。
