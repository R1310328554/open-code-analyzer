package azure

// bucket_client 封装 Thanos Azure 对象存储：合并 Loki Config 到 azure.DefaultConfig 后创建底层 Bucket，并包装 keyRewriteBucket。

import (
	"net/http"

	"github.com/go-kit/log"
	"github.com/thanos-io/objstore"
	"github.com/thanos-io/objstore/providers/azure"
)

// NewBucketClient 创建 Azure bucket 并用 ChunkDelimiter 包装键名重写层。
func NewBucketClient(cfg Config, name string, logger log.Logger, wrapRT func(http.RoundTripper) http.RoundTripper) (objstore.Bucket, error) {
	bucket, err := newBucketClient(cfg, name, logger, wrapRT, azure.NewBucketWithConfig)
	if err != nil {
		return nil, err
	}
	return &keyRewriteBucket{
		Bucket:    bucket,
		delimiter: cfg.ChunkDelimiter,
	}, nil
}

// newBucketClient 从 DefaultConfig 起步填充账户、容器、重试与可选 Endpoint，再调用 factory。
func newBucketClient(cfg Config, name string, logger log.Logger, wrapRT func(http.RoundTripper) http.RoundTripper, factory func(log.Logger, azure.Config, string, func(http.RoundTripper) http.RoundTripper) (*azure.Bucket, error)) (objstore.Bucket, error) {
	// Start with default config to make sure that all parameters are set to sensible values, especially
	// HTTP Config field.
	bucketConfig := azure.DefaultConfig
	bucketConfig.StorageAccountName = cfg.StorageAccountName
	bucketConfig.StorageAccountKey = cfg.StorageAccountKey.String()
	bucketConfig.StorageConnectionString = cfg.StorageConnectionString.String()
	bucketConfig.ContainerName = cfg.ContainerName
	bucketConfig.MaxRetries = cfg.MaxRetries
	bucketConfig.UserAssignedID = cfg.UserAssignedID
	bucketConfig.HTTPConfig.Transport = cfg.Transport

	if cfg.Endpoint != "" {
		// azure.DefaultConfig has the default Endpoint, overwrite it only if a different one was explicitly provided.
		bucketConfig.Endpoint = cfg.Endpoint
	}

	return factory(logger, bucketConfig, name, wrapRT)
}
// Endpoint 非空时覆盖默认后缀；Transport 注入自定义 RoundTripper 用于指标与追踪。
