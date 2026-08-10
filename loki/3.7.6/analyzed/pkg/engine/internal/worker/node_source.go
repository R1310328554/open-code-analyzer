package worker

// nodeSource 将跨任务输入流聚合为单一 executor.Pipeline，通过引用计数与 channel 实现背压：所有 streamSource 绑定同一 nodeSource。

import (
	"context"
	"sync"
	"time"

	"github.com/apache/arrow-go/v18/arrow"
	"go.uber.org/atomic"

	"github.com/grafana/loki/v3/pkg/engine/internal/executor"
	"github.com/grafana/loki/v3/pkg/xcap"
)

// nodeSource 懒初始化 closed/records 通道；Write 阻塞直到 Read 消费或 ctx 取消。
// nodeSource exposes data for a receiver of a stream as an [executor.Pipeline].
//
// Records are made available by a [nodeSource] calling [nodeSource.Write],
// after which each record can be read by the [nodeSource.Read] method.
type nodeSource struct {
	initOnce  sync.Once
	closeOnce sync.Once

	// streamCount is the number of streams that have been opened on this node
	// source.
	streamCount atomic.Int64

	closed  chan struct{}
	records chan arrow.RecordBatch
}

var _ executor.Pipeline = (*nodeSource)(nil)

func (src *nodeSource) Open(_ context.Context) error {
	src.lazyInit()
	return nil
}

// Read 从 records 通道取下一批 Arrow 数据，并记录 xcap 接收耗时。
// Read returns the next record of the node data. Blocks until results are
// available or until the provided ctx is canceled.
func (src *nodeSource) Read(ctx context.Context) (arrow.RecordBatch, error) {
	region := xcap.RegionFromContext(ctx)
	startRecv := time.Now()
	defer func() {
		region.Record(xcap.TaskRecvDuration.Observe(time.Since(startRecv).Seconds()))
	}()

	src.lazyInit()

	select {
	case <-ctx.Done():
		return nil, ctx.Err()
	case <-src.closed:
		return nil, executor.EOF
	case rec := <-src.records:
		return rec, nil
	}
}

// lazyInit 保证 closed 与 records 通道只创建一次。
func (src *nodeSource) lazyInit() {
	src.initOnce.Do(func() {
		src.closed = make(chan struct{})
		src.records = make(chan arrow.RecordBatch)
	})
}

// Write 向读端投递 RecordBatch；源已关闭时返回 executor.EOF。
// Write writes a record to the read end of the node source. Write blocks until
// the record has been read or the context is canceled.
func (src *nodeSource) Write(ctx context.Context, rec arrow.RecordBatch) error {
	src.lazyInit()

	select {
	case <-ctx.Done():
		return ctx.Err()
	case <-src.closed:
		return executor.EOF
	case src.records <- rec:
		return nil
	}
}

// Add 维护输入流引用计数；减至零自动 Close，负数则 panic。
// Add adds a delta, which may be negative, to the node source's input stream
// counter. If the counter becomes zero, the source is automatically closed. If
// the counter goes negative, Add panics.
func (src *nodeSource) Add(delta int64) {
	src.lazyInit()

	newValue := src.streamCount.Add(delta)
	if newValue == 0 {
		src.Close()
	} else if newValue < 0 {
		panic("negative stream count")
	}
}

// Close 关闭 closed 通道，后续 Read/Write 均返回 EOF。
// Close closes the source. All future Reads and Write calls will return
// [executor.EOF].
func (src *nodeSource) Close() {
	src.lazyInit()

	src.closeOnce.Do(func() { close(src.closed) })
}
// 多 stream 共享一个 nodeSource 使背压按节点而非按流数量施加。
