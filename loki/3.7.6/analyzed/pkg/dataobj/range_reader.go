package dataobj

// rangeReader 抽象对象字节范围读取：支持全量 Read 与指定 offset/length 的 ReadRange。

import (
	"context"
	"fmt"
	"io"
	"math"

	"github.com/thanos-io/objstore"
)

// 实现须支持并发创建多个 Read/ReadRange 返回的 ReadCloser。
// rangeReader is an interface that can read a range of bytes from an object.
type rangeReader interface {
	// Size returns the full size of the object.
	Size(ctx context.Context) (int64, error)

	// Read returns a reader over the entire object. Callers may create multiple
	// concurrent instances of Read.
	Read(ctx context.Context) (io.ReadCloser, error)

	// ReadRange returns a reader over a range of bytes. Callers may create
	// multiple concurrent instances of ReadRange.
	ReadRange(ctx context.Context, offset int64, length int64) (io.ReadCloser, error)
}

// bucketRangeReader 通过 objstore.BucketReader 对存储路径做范围 GET。
type bucketRangeReader struct {
	bucket objstore.BucketReader
	path   string
}

func (rr *bucketRangeReader) Size(ctx context.Context) (int64, error) {
	attrs, err := rr.bucket.Attributes(ctx, rr.path)
	if err != nil {
		return 0, fmt.Errorf("reading attributes: %w", err)
	}
	return attrs.Size, nil
}

func (rr *bucketRangeReader) Read(ctx context.Context) (io.ReadCloser, error) {
	return rr.bucket.Get(ctx, rr.path)
}

func (rr *bucketRangeReader) ReadRange(ctx context.Context, offset int64, length int64) (io.ReadCloser, error) {
	return rr.bucket.GetRange(ctx, rr.path, offset, length)
}

// readerAtRangeReader 基于 io.ReaderAt 与已知 size 提供本地切片读取。
type readerAtRangeReader struct {
	size int64
	r    io.ReaderAt
}

func (rr *readerAtRangeReader) Size(ctx context.Context) (int64, error) {
	if ctx.Err() != nil {
		return 0, ctx.Err()
	}
	return rr.size, nil
}

func (rr *readerAtRangeReader) Read(ctx context.Context) (io.ReadCloser, error) {
	if ctx.Err() != nil {
		return nil, ctx.Err()
	}
	return io.NopCloser(io.NewSectionReader(rr.r, 0, rr.size)), nil
}

// ReadRange 用 SectionReader 包装 ReaderAt 子区间，先检查 ctx 取消。
func (rr *readerAtRangeReader) ReadRange(ctx context.Context, offset int64, length int64) (io.ReadCloser, error) {
	if ctx.Err() != nil {
		return nil, ctx.Err()
	} else if length > math.MaxInt {
		return nil, fmt.Errorf("length too large: %d", length)
	}
	return io.NopCloser(io.NewSectionReader(rr.r, offset, length)), nil
}
// bucket 实现依赖 Attributes 获取对象大小，ReaderAt 则直接使用已知长度。
