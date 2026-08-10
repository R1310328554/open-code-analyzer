package worker

// streamSource 接收远程 StreamDataMessage 并转发至绑定的 nodeSource，是跨 worker 输入流在本地侧的入口适配器。

import (
	"context"
	"errors"
	"sync"

	"github.com/apache/arrow-go/v18/arrow"

	"github.com/grafana/loki/v3/pkg/engine/internal/scheduler/wire"
)

// streamSource 通过 stateMut 保证 Bind 与 Close 互斥，bound channel 同步绑定完成。
// streamSource handles incoming data for a stream, forwarding it to a bound
// [nodeSource] for processing.
type streamSource struct {
	// stateMut ensures that we don't call Bind and Close concurrently.
	stateMut   sync.Mutex
	nodeSource *nodeSource // Node source to forward data to.
	closed     chan struct{}
	bound      chan struct{}

	initOnce  sync.Once
	closeOnce sync.Once
}

// Write 等待 Bind 完成后调用 nodeSource.Write；已关闭则返回 ErrConnClosed。
// Write forwards a record to the bound [nodeSource]. Write blocks until a
// nodeSource is bound and accepts the write, or the provided context is
// canceled.
func (src *streamSource) Write(ctx context.Context, rec arrow.RecordBatch) error {
	src.lazyInit()

	select {
	case <-ctx.Done():
		return ctx.Err()
	case <-src.closed:
		return wire.ErrConnClosed
	case <-src.bound:
	}

	return src.nodeSource.Write(ctx, rec)
}

func (src *streamSource) lazyInit() {
	src.initOnce.Do(func() {
		src.closed = make(chan struct{})
		src.bound = make(chan struct{})
	})
}

// Bind 增加 nodeSource 引用计数并 close(bound)；重复绑定或已关闭则报错。
// Bind binds the streamSource to a nodeSource. Calls to Bind after the first
// will return an error.
func (src *streamSource) Bind(nodeSource *nodeSource) error {
	src.lazyInit()

	src.stateMut.Lock()
	defer src.stateMut.Unlock()

	// If the stream source was closed, don't permit binding.
	select {
	case <-src.closed:
		return wire.ErrConnClosed
	default:
	}

	if src.nodeSource != nil {
		return errors.New("stream already bound")
	}

	nodeSource.Add(1)
	src.nodeSource = nodeSource
	close(src.bound)
	return nil
}

// Close 递减 nodeSource 引用并关闭 closed，阻止后续 Write。
// Close closes the source. All future Reads and Write calls will return
// [executor.EOF].
func (src *streamSource) Close() {
	src.lazyInit()

	src.stateMut.Lock()
	defer src.stateMut.Unlock()

	src.closeOnce.Do(func() {
		if src.nodeSource != nil {
			src.nodeSource.Add(-1) // Remove ourselves from the nodeSource.
		}

		close(src.closed)
	})
}
// 调度器通过 StreamStatusMessage Closed 触发 source.Close 完成流生命周期。
