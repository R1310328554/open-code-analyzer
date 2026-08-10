package indexshipper

// table_client 将 indexshipper storage.Client 适配为 series index.TableClient，供 table manager 列举与删除对象存储中的逻辑表。

import (
	"context"

	"github.com/grafana/loki/v3/pkg/storage/chunk/client"
	"github.com/grafana/loki/v3/pkg/storage/config"
	"github.com/grafana/loki/v3/pkg/storage/stores/series/index"
	"github.com/grafana/loki/v3/pkg/storage/stores/shipper/indexshipper/storage"
)

type tableClient struct {
	indexStorageClient storage.Client
}

// NewTableClient 用 storageKeyPrefix 构造 IndexStorageClient 并包装为 tableClient。
// NewTableClient creates a client for managing tables in object storage based index store.
// It is typically used when running a table manager.
func NewTableClient(objectClient client.ObjectClient, storageKeyPrefix string) index.TableClient {
	return &tableClient{storage.NewIndexStorageClient(objectClient, storageKeyPrefix)}
}

// ListTables 先刷新表名缓存再列举对象存储前缀下的全部索引表。
func (b *tableClient) ListTables(ctx context.Context) ([]string, error) {
	b.indexStorageClient.RefreshIndexTableNamesCache(ctx)
	return b.indexStorageClient.ListTables(ctx)
}

func (b *tableClient) CreateTable(_ context.Context, _ config.TableDesc) error {
	return nil
}

func (b *tableClient) Stop() {
	b.indexStorageClient.Stop()
}

// DeleteTable 绕过缓存列举公共索引文件并逐个 DeleteFile 清空整张表。
func (b *tableClient) DeleteTable(ctx context.Context, tableName string) error {
	files, _, err := b.indexStorageClient.ListFiles(ctx, tableName, true)
	if err != nil {
		return err
	}

	for _, file := range files {
		err := b.indexStorageClient.DeleteFile(ctx, tableName, file.Name)
		if err != nil {
			return err
		}
	}

	return nil
}

func (b *tableClient) DescribeTable(_ context.Context, name string) (desc config.TableDesc, isActive bool, err error) {
	return config.TableDesc{
		Name: name,
	}, true, nil
}

func (b *tableClient) UpdateTable(_ context.Context, _, _ config.TableDesc) error {
	return nil
}
// CreateTable/UpdateTable 对 TSDB shipper 为 no-op，表由 ingester 写入时隐式创建。
