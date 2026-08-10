package index

// index 定义 series 外部索引（DynamoDB/Bigtable 等）的读写 Client、Query/Entry 模型及 QueryPages 分页回调抽象。

import (
	"context"
)

// QueryPagesCallback from an IndexQuery.
type QueryPagesCallback func(Query, ReadBatchResult) bool

// ReadClient 通过 QueryPages 批量执行索引读查询。
// Client for the read path.
type ReadClient interface {
	QueryPages(ctx context.Context, queries []Query, callback QueryPagesCallback) error
}

// WriteClient 提供 WriteBatch 批量写入与 NewWriteBatch 工厂。
// Client for the write path.
type WriteClient interface {
	NewWriteBatch() WriteBatch
	BatchWrite(context.Context, WriteBatch) error
}

// Client 聚合读写路径并暴露 Stop 用于关闭底层连接。
// Client is a client for the storage of the index (e.g. DynamoDB or Bigtable).
type Client interface {
	ReadClient
	WriteClient
	Stop()
}

// ReadBatchResult represents the results of a QueryPages.
type ReadBatchResult interface {
	Iterator() ReadBatchIterator
}

// ReadBatchIterator is an iterator over a ReadBatch.
type ReadBatchIterator interface {
	Next() bool
	RangeValue() []byte
	Value() []byte
}

// WriteBatch represents a batch of writes.
type WriteBatch interface {
	Add(tableName, hashValue string, rangeValue []byte, value []byte)
	Delete(tableName, hashValue string, rangeValue []byte)
}

// Query 描述一次索引查找：表名、hash 键、range 前缀/起点及 ValueEqual 过滤。
// Query describes a query for entries
type Query struct {
	TableName string
	HashValue string

	// One of RangeValuePrefix or RangeValueStart might be set:
	// - If RangeValuePrefix is not nil, must read all keys with that prefix.
	// - If RangeValueStart is not nil, must read all keys from there onwards.
	// - If neither is set, must read all keys for that row.
	// RangeValueStart should only be used for querying Chunk IDs.
	// If this is going to change then please take care of func isChunksQuery in pkg/chunk/storage/caching_index_client.go which relies on it.
	RangeValuePrefix []byte
	RangeValueStart  []byte

	// Filters for querying
	ValueEqual []byte

	// If the result of this lookup is immutable or not (for caching).
	Immutable bool
}

// Entry 表示一条索引行：TableName、HashValue、RangeValue 与可选 Value 载荷。
// Entry describes an entry in the chunk index
type Entry struct {
	TableName string
	HashValue string

	// For writes, RangeValue will always be set.
	RangeValue []byte

	// New for v6 schema, label value is not written as part of the range key.
	Value []byte
}

// QueryKey 将 Query 各字段用分隔符拼成缓存键字符串。
func QueryKey(q Query) string {
	ret := q.TableName + sep + q.HashValue

	if len(q.RangeValuePrefix) != 0 {
		ret += sep + string(q.RangeValuePrefix)
	}

	if len(q.RangeValueStart) != 0 {
		ret += sep + string(q.RangeValueStart)
	}

	if len(q.ValueEqual) != 0 {
		ret += sep + string(q.ValueEqual)
	}

	return ret
}
// RangeValueStart 专用于 chunk ID 范围扫描，变更时需同步 caching_index_client 逻辑。
