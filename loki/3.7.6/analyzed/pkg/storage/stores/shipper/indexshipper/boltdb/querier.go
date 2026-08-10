package boltdb

// querier 按表聚合索引查询：并行扫描本地 writer 快照与 IndexShipper 已下载文件，统一经 QueryBoltDB 回调 series 索引页。

import (
	"context"
	"fmt"

	"github.com/grafana/dskit/tenant"
	"go.etcd.io/bbolt"

	"github.com/grafana/loki/v3/pkg/storage/stores/series/index"
	shipperindex "github.com/grafana/loki/v3/pkg/storage/stores/shipper/indexshipper/index"
	"github.com/grafana/loki/v3/pkg/storage/stores/shipper/indexshipper/util"
)

type Writer interface {
	ForEach(ctx context.Context, tableName string, callback func(b *bbolt.DB) error) error
}

type Querier interface {
	QueryPages(ctx context.Context, queries []index.Query, callback index.QueryPagesCallback) error
}

// querier 绑定可选 Writer 与 Shipper，ReadOnly 模式下 writer 为 nil 仅查远程缓存。
type querier struct {
	writer       Writer
	indexShipper Shipper
}

// NewQuerier 构造默认 querier 实现，供 IndexClient 在 init 阶段注入依赖。
func NewQuerier(writer Writer, indexShipper Shipper) Querier {
	return &querier{
		writer:       writer,
		indexShipper: indexShipper,
	}
}

// QueryPages 先按表分组，再并行查询 writer（若存在）与 shipper 中各 IndexFile。
// QueryPages queries both the writer and indexShipper for the given queries.
func (q *querier) QueryPages(ctx context.Context, queries []index.Query, callback index.QueryPagesCallback) error {
	userID, err := tenant.TenantID(ctx)
	if err != nil {
		return err
	}

	userIDBytes := util.GetUnsafeBytes(userID)
	queriesByTable := util.QueriesByTable(queries)
	for table, queries := range queriesByTable {
		err := util.DoParallelQueries(ctx, func(ctx context.Context, queries []index.Query, callback index.QueryPagesCallback) error {
			// writer could be nil when running in ReadOnly mode
			if q.writer != nil {
				err := q.writer.ForEach(ctx, table, func(b *bbolt.DB) error {
					return QueryBoltDB(ctx, b, userIDBytes, queries, callback)
				})
				if err != nil {
					return err
				}
			}

			return q.indexShipper.ForEach(ctx, table, userID, func(_ bool, idx shipperindex.Index) error {
				boltdbIndexFile, ok := idx.(*IndexFile)
				if !ok {
					return fmt.Errorf("unexpected index type %T", idx)
				}

				return QueryBoltDB(ctx, boltdbIndexFile.GetBoltDB(), userIDBytes, queries, callback)
			})
		}, queries, callback)
		if err != nil {
			return err
		}
	}

	return nil
}
// ForEach 回调须返回 *IndexFile 类型，否则视为 shipper 配置或打开函数不一致错误。
