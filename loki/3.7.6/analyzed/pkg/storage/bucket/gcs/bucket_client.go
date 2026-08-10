package gcs

// gcs 包封装 Google Cloud Storage 桶客户端创建：将 Loki Config 映射到 Thanos objstore GCS provider 并支持自定义 RoundTripper。

import (
	"context"
	"net/http"

	"github.com/go-kit/log"
	"github.com/thanos-io/objstore"
	"github.com/thanos-io/objstore/providers/gcs"
)

// NewBucketClient 从 cfg 填充 gcs.DefaultConfig，再调用 gcs.NewBucketWithConfig 创建桶。
// NewBucketClient creates a new GCS bucket client
func NewBucketClient(ctx context.Context, cfg Config, name string, logger log.Logger, wrapRT func(http.RoundTripper) http.RoundTripper) (objstore.Bucket, error) {
// 以 Thanos 默认 HTTP 配置为基底，再覆盖桶名、服务账号 JSON、分块大小与重试次数。
	// start with default http configs
	bucketConfig := gcs.DefaultConfig
	bucketConfig.Bucket = cfg.BucketName
	bucketConfig.ServiceAccount = cfg.ServiceAccount.String()
	bucketConfig.ChunkSizeBytes = cfg.ChunkBufferSize
	bucketConfig.MaxRetries = cfg.MaxRetries
	bucketConfig.HTTPConfig.Transport = cfg.Transport

	return gcs.NewBucketWithConfig(ctx, logger, bucketConfig, name, wrapRT)
}
// wrapRT 用于注入 tracing 或指标 RoundTripper；Transport 由上层 bucket 包按组件配置。
