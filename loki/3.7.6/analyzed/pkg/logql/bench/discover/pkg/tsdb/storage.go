package tsdb

// storage 根据 StorageConfig 构造 S3/GCS/Azure/本地 FS 对象客户端与索引存储客户端。

import (
	"context"
	"fmt"
	"sync"

	"github.com/grafana/loki/v3/pkg/storage/chunk/client"
	"github.com/grafana/loki/v3/pkg/storage/chunk/client/aws"
	"github.com/grafana/loki/v3/pkg/storage/chunk/client/azure"
	"github.com/grafana/loki/v3/pkg/storage/chunk/client/gcp"
	"github.com/grafana/loki/v3/pkg/storage/chunk/client/hedging"
	"github.com/grafana/loki/v3/pkg/storage/chunk/client/local"
	shipperstorage "github.com/grafana/loki/v3/pkg/storage/stores/shipper/indexshipper/storage"
)

// newS3ObjectClient 等变量可替换，便于单测注入 mock ObjectClient。
var (
	newS3ObjectClient = func(cfg aws.S3Config, hedgingCfg hedging.Config) (client.ObjectClient, error) {
		return aws.NewS3ObjectClient(cfg, hedgingCfg)
	}

	newGCSObjectClient = func(ctx context.Context, cfg gcp.GCSConfig, hedgingCfg hedging.Config) (client.ObjectClient, error) {
		return gcp.NewGCSObjectClient(ctx, cfg, hedgingCfg)
	}

	newAzureObjectClient = func(cfg *azure.BlobStorageConfig, hedgingCfg hedging.Config) (client.ObjectClient, error) {
		return azure.NewBlobStorage(cfg, getAzureBlobMetrics(), hedgingCfg)
	}

	newFSObjectClient = func(cfg local.FSConfig) (client.ObjectClient, error) {
		return local.NewFSObjectClient(cfg)
	}
)

var (
	azureBlobMetricsOnce sync.Once
	azureBlobMetrics     azure.BlobStorageMetrics
)

// getAzureBlobMetrics 懒初始化 Azure 指标，sync.Once 保证只注册一次。
func getAzureBlobMetrics() azure.BlobStorageMetrics {
	azureBlobMetricsOnce.Do(func() {
		azureBlobMetrics = azure.NewBlobStorageMetrics()
	})

	return azureBlobMetrics
}

func NewObjectClient(cfg StorageConfig) (client.ObjectClient, error) {
	switch cfg.StorageType {
	case StorageTypeS3:
		return newS3ObjectClient(cfg.S3, hedging.Config{})
	case StorageTypeGCS:
		return newGCSObjectClient(context.Background(), cfg.GCS, hedging.Config{})
	case StorageTypeAzure:
		return newAzureObjectClient(&cfg.Azure, hedging.Config{})
	case StorageTypeFilesystem:
		return newFSObjectClient(cfg.Filesystem)
	default:
		return nil, fmt.Errorf("unsupported --storage-type %q (supported: s3, gcs, azure, filesystem)", cfg.StorageType)
	}
}

// NewIndexStorageClient 校验配置后包装 ObjectClient 为 shipper 索引客户端。
func NewIndexStorageClient(cfg StorageConfig) (shipperstorage.Client, error) {
	if err := cfg.NormalizeAndValidate(); err != nil {
		return nil, err
	}

	objClient, err := NewObjectClient(cfg)
	if err != nil {
		return nil, err
	}

	return shipperstorage.NewIndexStorageClient(objClient, cfg.Prefix), nil
}
// hedging 配置当前传空，与 Loki 生产默认一致，后续可扩展重试策略。
