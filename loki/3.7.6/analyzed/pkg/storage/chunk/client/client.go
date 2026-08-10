package client

// client 包定义 chunk 存储抽象：Client 负责 chunk 读写删，ObjectAndIndexClient 允许 DynamoDB 等后端在同一调用中写入 chunk 与索引。

import (
	"context"
	"errors"

	"github.com/grafana/loki/v3/pkg/storage/chunk"
	"github.com/grafana/loki/v3/pkg/storage/stores/series/index"
)

// 包级错误：ErrMethodNotImplemented 表示可选方法未实现；ErrStorageObjectNotFound 表示对象缺失。
var (
	// ErrMethodNotImplemented when any of the storage clients do not implement a method
	ErrMethodNotImplemented = errors.New("method is not implemented")
	// ErrStorageObjectNotFound when object storage does not have requested object
	ErrStorageObjectNotFound = errors.New("object not found in storage")
)

// Client is for storing and retrieving chunks.
// Client 是 chunk 后端统一接口，含 Stop、Put/Get/Delete 及 NotFound/Retryable 错误判定。
type Client interface {
	Stop()
	PutChunks(ctx context.Context, chunks []chunk.Chunk) error
	GetChunks(ctx context.Context, chunks []chunk.Chunk) ([]chunk.Chunk, error)
	DeleteChunk(ctx context.Context, userID, chunkID string) error
	IsChunkNotFoundErr(err error) bool
	IsRetryableErr(err error) bool
}

// ObjectAndIndexClient allows optimisations where the same client handles both
// Only used by DynamoDB (dynamodbIndexReader and dynamoDBStorageClient)
// ObjectAndIndexClient 扩展原子 PutChunksAndIndex，避免 DynamoDB 双写不一致。
type ObjectAndIndexClient interface {
	PutChunksAndIndex(ctx context.Context, chunks []chunk.Chunk, index index.WriteBatch) error
}
// IsChunkNotFoundErr 与 IsRetryableErr 由各存储驱动自行解释底层错误语义。
