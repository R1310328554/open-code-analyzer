package workflow

// streamPipe 实现 executor.Pipeline，将调度器侧最终结果流缓冲为 channel，供 Workflow.Run 返回的 pipeline Read 消费。

import (
	"context"
	"sync"

	"github.com/apache/arrow-go/v18/arrow"

	"github.com/grafana/loki/v3/pkg/engine/internal/executor"
)

// streamPipe 用于工作流根任务 sink 到本地 resultsStream 的数据通路。
// A streamPipe connects data for a stream to a local listener (via
// [executor.Pipeline]). Stream data is received by the scheduler, and enqueued
// for reading by calls to [streamPipe.Write].
//
// streamPipes are used for final task results, for which the reading end is the
// scheduler rather than another task.
type streamPipe struct {
	closeOnce sync.Once

	err      error
	errCond  chan struct{}
	failOnce sync.Once

	closed  chan struct{}
	results chan arrow.RecordBatch
}

var _ executor.Pipeline = (*streamPipe)(nil)

// newStreamPipe 初始化 results/errCond/closed 三个同步 channel。
// newStreamPipe creates a new streamPipe.
func newStreamPipe() *streamPipe {
	return &streamPipe{
		closed:  make(chan struct{}),
		results: make(chan arrow.RecordBatch),
		errCond: make(chan struct{}),
	}
}

// Open 空实现：管道无需额外打开步骤。
func (pipe *streamPipe) Open(_ context.Context) error { return nil }

// Read 优先返回 SetError 设置的 err，否则从 results 通道取批次。
// Read returns the next record of the stream data. Blocks until results are
// available or until the provided ctx is canceled.
func (pipe *streamPipe) Read(ctx context.Context) (arrow.RecordBatch, error) {
	select {
	case <-ctx.Done():
		return nil, ctx.Err()
	case <-pipe.errCond:
		return nil, pipe.err
	case <-pipe.closed:
		// Check to see if the pipeline has a more specific error before falling
		// back to EOF.
		return nil, pipe.checkError(executor.EOF)
	case rec := <-pipe.results:
		return rec, nil
	}
}

// checkError 非阻塞检查 errCond 是否已关闭以返回管道级错误。
// checkError checks to see if the pipeline has its own error before falling
// back to the provided error.
func (pipe *streamPipe) checkError(err error) error {
	select {
	case <-pipe.errCond:
		return pipe.err
	default:
		return err
	}
}

// Write 由远程 worker 经调度器转发写入，阻塞直到 Read 消费。
// Write writes a record to the read end of the pipe. Write blocks until the
// record has been read or the context is canceled.
func (pipe *streamPipe) Write(ctx context.Context, rec arrow.RecordBatch) error {
	select {
	case <-ctx.Done():
		return ctx.Err()
	case <-pipe.closed:
		return executor.EOF
	case pipe.results <- rec:
		return nil
	}
}

// SetError 仅首次非 nil 错误生效，关闭 errCond 唤醒阻塞的 Read。
// SetError sets the error for the pipeline. Calls to SetError with a non-nil
// error after the first are ignored.
func (pipe *streamPipe) SetError(err error) {
	if err == nil {
		return
	}

	pipe.failOnce.Do(func() {
		pipe.err = err
		close(pipe.errCond)
	})
}

// Close 关闭 closed 通道，后续 Write 返回 EOF。
func (pipe *streamPipe) Close() {
	pipe.closeOnce.Do(func() {
		close(pipe.closed)
	})
}
// resultsPipeline 在 resultsStream 关闭时由 onStreamChange 触发 Close。
