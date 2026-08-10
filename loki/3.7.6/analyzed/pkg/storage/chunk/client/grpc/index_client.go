package grpc

// index_client 将 index.Client 语义映射到 gRPC store：WriteBatch 转 WriteIndex/DeleteIndex RPC，QueryPages 流式接收 QueryIndexResponse。

import (
	"context"
	"io"

	"github.com/pkg/errors"

	"github.com/grafana/loki/v3/pkg/storage/chunk/client/util"
	"github.com/grafana/loki/v3/pkg/storage/stores/series/index"
)

// WriteBatch.Add 追加 IndexEntry 到 Writes 切片，BatchWrite 时一次性发送。
func (w *WriteBatch) Add(tableName, hashValue string, rangeValue []byte, value []byte) {
	w.Writes = append(w.Writes, &IndexEntry{
		TableName:  tableName,
		HashValue:  hashValue,
		RangeValue: rangeValue,
		Value:      value,
	})
}

func (w *WriteBatch) Delete(tableName, hashValue string, rangeValue []byte) {
	w.Deletes = append(w.Deletes, &IndexEntry{
		TableName:  tableName,
		HashValue:  hashValue,
		RangeValue: rangeValue,
	})
}

func (s *StorageClient) NewWriteBatch() index.WriteBatch {
	return &WriteBatch{}
}

// BatchWrite 先 WriteIndex 再 DeleteIndex，均使用 context.Background 忽略传入 ctx。
func (s *StorageClient) BatchWrite(_ context.Context, batch index.WriteBatch) error {
	writeBatch := batch.(*WriteBatch)
	batchWrites := &WriteIndexRequest{Writes: writeBatch.Writes}
	_, err := s.client.WriteIndex(context.Background(), batchWrites)
	if err != nil {
		return errors.WithStack(err)
	}

	batchDeletes := &DeleteIndexRequest{Deletes: writeBatch.Deletes}
	_, err = s.client.DeleteIndex(context.Background(), batchDeletes)
	if err != nil {
		return errors.WithStack(err)
	}

	return nil
}

// QueryPages 并行发起多条 QueryIndex 流，QueryIndexResponse 实现 ReadBatchIterator。
func (s *StorageClient) QueryPages(ctx context.Context, queries []index.Query, callback index.QueryPagesCallback) error {
	return util.DoParallelQueries(ctx, s.query, queries, callback)
}

func (s *StorageClient) query(ctx context.Context, query index.Query, callback index.QueryPagesCallback) error {
	indexQuery := &QueryIndexRequest{
		TableName:        query.TableName,
		HashValue:        query.HashValue,
		RangeValuePrefix: query.RangeValuePrefix,
		RangeValueStart:  query.RangeValueStart,
		ValueEqual:       query.ValueEqual,
		Immutable:        query.Immutable,
	}
	streamer, err := s.client.QueryIndex(ctx, indexQuery)
	if err != nil {
		return errors.WithStack(err)
	}
	for {
		readBatch, err := streamer.Recv()
		if err == io.EOF {
			break
		}
		if err != nil {
			return errors.WithStack(err)
		}
		if !callback(query, readBatch) {
			return nil
		}
	}

	return nil
}

func (r *QueryIndexResponse) Iterator() index.ReadBatchIterator {
	return &grpcIter{
		i:                  -1,
		QueryIndexResponse: r,
	}
}

// grpcIter 遍历 QueryIndexResponse.Rows，按序返回 RangeValue 与 Value。
type grpcIter struct {
	i int
	*QueryIndexResponse
}

func (b *grpcIter) Next() bool {
	b.i++
	return b.i < len(b.Rows)
}

func (b *grpcIter) RangeValue() []byte {
	return b.Rows[b.i].RangeValue
}

func (b *grpcIter) Value() []byte {
	return b.Rows[b.i].Value
}
// Delete 仅填充 hash/range 无 Value；callback 返回 false 可提前终止流式读取。
