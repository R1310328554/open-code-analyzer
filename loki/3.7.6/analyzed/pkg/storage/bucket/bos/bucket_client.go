package bos

// bucket_client 基于 Thanos BOS provider 创建百度云对象存储 Bucket：将 Loki bos.Config 映射为 bos.Config 并调用 NewBucketWithConfig。

import (
	"github.com/go-kit/log"
	"github.com/thanos-io/objstore"
	"github.com/thanos-io/objstore/providers/bos"
)

// NewBucketClient 填充 Endpoint、Bucket、AccessKey 与 SecretKey 后构造 BOS 客户端。
func NewBucketClient(cfg Config, name string, logger log.Logger) (objstore.Bucket, error) {
	bosCfg := bos.Config{
		Endpoint:  cfg.Endpoint,
		Bucket:    cfg.Bucket,
		SecretKey: cfg.SecretKey.String(),
		AccessKey: cfg.AccessKey,
	}
	return bos.NewBucketWithConfig(logger, bosCfg, name)
}
// name 参数供 Thanos 指标与日志标识该 bucket 实例，与 cfg.Bucket 容器名不同。
