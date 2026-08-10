package chunkenc

// Chunk 核心接口与错误定义：描述压缩日志块的追加、迭代、
// 序列化及块级访问契约，并区分乱序与过旧条目错误。

import (
	"context"
	"errors"
	"fmt"
	"io"
	"time"

	"github.com/grafana/loki/v3/pkg/compression"
	"github.com/grafana/loki/v3/pkg/iter"
	"github.com/grafana/loki/v3/pkg/logproto"
	"github.com/grafana/loki/v3/pkg/logql/log"
	"github.com/grafana/loki/v3/pkg/util/filter"
)

// 包级错误变量标识块满、乱序、无效尺寸与校验失败等状态。
// Errors returned by the chunk interface.
var (
	ErrChunkFull       = errors.New("chunk full")
	ErrOutOfOrder      = errors.New("entry out of order")
	ErrInvalidSize     = errors.New("invalid size")
	ErrInvalidFlag     = errors.New("invalid flag")
	ErrInvalidChecksum = errors.New("invalid chunk checksum")
)

// errTooFarBehind 表示条目时间戳早于流允许的最旧时间。
type errTooFarBehind struct {
	// original timestamp of the entry itself.
	entryTs time.Time

	// cutoff is the oldest acceptable timstamp of the `stream` that entry belongs to.
	cutoff time.Time
}

func IsErrTooFarBehind(err error) bool {
	_, ok := err.(*errTooFarBehind)
	return ok
}

func ErrTooFarBehind(entryTs, cutoff time.Time) error {
	return &errTooFarBehind{entryTs: entryTs, cutoff: cutoff}
}

func (m *errTooFarBehind) Error() string {
	return fmt.Sprintf("entry too far behind, entry timestamp is: %s, oldest acceptable timestamp is: %s", m.entryTs.Format(time.RFC3339), m.cutoff.Format(time.RFC3339))
}

func IsOutOfOrderErr(err error) bool {
	return err == ErrOutOfOrder || IsErrTooFarBehind(err)
}

// Chunk 接口定义 Loki 内存/持久化日志块的全部读写操作。
// Chunk is the interface for the compressed logs chunk format.
type Chunk interface {
	Bounds() (time.Time, time.Time)
	SpaceFor(*logproto.Entry) bool
	// Append returns true if the entry appended was a duplicate
	Append(*logproto.Entry) (bool, error)
	Iterator(ctx context.Context, mintT, maxtT time.Time, direction logproto.Direction, pipeline log.StreamPipeline) (iter.EntryIterator, error)
	SampleIterator(ctx context.Context, from, through time.Time, extractor ...log.StreamSampleExtractor) iter.SampleIterator
	// Returns the list of blocks in the chunks.
	Blocks(mintT, maxtT time.Time) []Block
	// Size returns the number of entries in a chunk
	Size() int
	Bytes() ([]byte, error)
	BytesWith([]byte) ([]byte, error) // uses provided []byte for buffer instantiation
	io.WriterTo
	BlockCount() int
	Utilization() float64
	UncompressedSize() int
	CompressedSize() int
	Close() error
	Encoding() compression.Codec
	Rewrite(filter filter.Func) (Chunk, error)
}

// Block 表示 Chunk 内一个已压缩的时间有序日志子块。
// Block is a chunk block.
type Block interface {
	// MinTime is the minimum time of entries in the block
	MinTime() int64
	// MaxTime is the maximum time of entries in the block
	MaxTime() int64
	// Offset is the offset/position of the block in the chunk. Offset is unique for a given block per chunk.
	Offset() int
	// Entries is the amount of entries in the block.
	Entries() int
	// Iterator returns an entry iterator for the block.
	Iterator(ctx context.Context, pipeline log.StreamPipeline) iter.EntryIterator
	// SampleIterator returns a sample iterator for the block.
	SampleIterator(ctx context.Context, extractor ...log.StreamSampleExtractor) iter.SampleIterator
}
