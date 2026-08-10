package worker

// streamSink 将任务输出 RecordBatch 经 wire 协议发送到远程 worker 或调度器，支持 Bind 绑定目标地址、断线重连与指数退避重试。

import (
	"context"
	"errors"
	"fmt"
	"net"
	"sync"
	"time"

	"github.com/apache/arrow-go/v18/arrow"
	"github.com/go-kit/log"
	"github.com/go-kit/log/level"
	"github.com/grafana/dskit/backoff"

	"github.com/grafana/loki/v3/pkg/engine/internal/scheduler/wire"
	"github.com/grafana/loki/v3/pkg/engine/internal/workflow"
)

// streamSink 维护 destConn 对等连接，Send 阻塞直到对端确认或 ctx 取消。
// streamSink allows for sending records remotely across a stream.
type streamSink struct {
	Logger      log.Logger
	WireMetrics *wire.Metrics
	Scheduler   *wire.Peer
	Stream      *workflow.Stream
	Dialer      func(ctx context.Context, addr net.Addr) (wire.Conn, error)

	initOnce  sync.Once
	ctx       context.Context    // Context used for peer connections.
	cancel    context.CancelFunc // Cancel function for peer connections.
	bound     chan struct{}
	closeOnce sync.Once

	bindOnce    sync.Once
	destination net.Addr

	destConnMut sync.Mutex
	destConn    *wire.Peer
}

// Bind 一次性设置 destination 并通知调度器 StreamStateOpen，唤醒等待中的 Send。
// Bind informs the sink about the address to send stream data to. Calls to Bind
// after the first will return an error.
func (sink *streamSink) Bind(ctx context.Context, destination net.Addr) error {
	sink.lazyInit()

	var bound bool
	sink.bindOnce.Do(func() {
		bound = true

		// Best-effort inform the scheduler that we're ready to send data.
		_ = sink.Scheduler.SendMessageAsync(ctx, wire.StreamStatusMessage{
			StreamID: sink.Stream.ULID,
			State:    workflow.StreamStateOpen,
		})

		sink.destination = destination
		close(sink.bound) // Wake up any Send goroutines
	})

	if !bound {
		return errors.New("stream destination already bound")
	}
	return nil
}

// lazyInit 创建 peer 连接用的 Background ctx 与 bound 同步 channel。
func (sink *streamSink) lazyInit() {
	sink.initOnce.Do(func() {
		sink.ctx, sink.cancel = context.WithCancel(context.Background())

		sink.bound = make(chan struct{})
	})
}

// Send 在可重试错误（连接关闭）时使用 backoff 重试，载荷被拒则立即失败。
// Send sends a record to the remote side of the stream.
//
// Calls to Send block until:
//
// - There is a bound address for the destination.
// - The record has been sent successfully to the destination.
//
// Send will attempt to re-establish connection to the destination if the
// connection is lost.
//
// Send can be aborted by cancelling the provided context.
func (sink *streamSink) Send(ctx context.Context, rec arrow.RecordBatch) error {
	sink.lazyInit()

	bo := backoff.New(ctx, backoff.Config{
		MinBackoff: 100 * time.Millisecond,
		MaxBackoff: 1 * time.Second,
	})

	for bo.Ongoing() {
		// We only want to retry on errors about the connection closing; errors
		// where the peer rejected our payload should be considered
		// nonretryable.
		err := sink.send(ctx, rec)
		if err == nil {
			break
		}

		if !sink.isRetryable(err) {
			level.Error(sink.Logger).Log("msg", "failed to send data to peer. Encountered non-retryable error", "err", err)
			return err
		}

		level.Warn(sink.Logger).Log("msg", "failed to send data to peer", "err", err)
		bo.Wait()
	}

	return bo.Err()
}

// send 经 getPeer 获取连接后发送 StreamDataMessage。
func (sink *streamSink) send(ctx context.Context, rec arrow.RecordBatch) error {
	peer, err := sink.getPeer(ctx)
	if err != nil {
		return fmt.Errorf("connecting to peer: %w", err)
	}

	// TODO(rfratto): We should send a Blocked status update to the scheduler if
	// SendMessage doesn't finish quickly enough.
	//
	// We need to find a way to efficiently do that here that doesn't cancel the
	// send.
	err = peer.SendMessage(ctx, wire.StreamDataMessage{
		StreamID: sink.Stream.ULID,
		Data:     rec,
	})
	if err != nil {
		return fmt.Errorf("sending data to peer: %w", err)
	}

	return nil
}

// getPeer 等待 Bind 完成，按需 Dial 并后台 Serve；连接关闭时清空 destConn 供重连。
func (sink *streamSink) getPeer(ctx context.Context) (*wire.Peer, error) {
	// Wait for destination.
	select {
	case <-ctx.Done():
		return nil, ctx.Err()
	case <-sink.ctx.Done():
		return nil, wire.ErrConnClosed
	case <-sink.bound:
	}

	sink.destConnMut.Lock()
	defer sink.destConnMut.Unlock()

	if sink.destConn != nil {
		return sink.destConn, nil
	}

	conn, err := sink.Dialer(ctx, sink.destination)
	if err != nil {
		return nil, err
	}

	peer := &wire.Peer{
		Logger:  sink.Logger,
		Metrics: sink.WireMetrics,
		Conn:    conn,
		Handler: nil, // This is a send-only connection.
	}

	go func() {
		if err := peer.Serve(sink.ctx); err != nil && errors.Is(err, context.Canceled) {
			level.Warn(sink.Logger).Log("msg", "stream sink peer closed", "err", err)
		}

		// Clear out the cached connection so the next call to getPeer can
		// create a new one.
		sink.destConnMut.Lock()
		defer sink.destConnMut.Unlock()

		sink.destConn = nil
	}()

	sink.destConn = peer
	return peer, nil
}

// isRetryable 目前仅 wire.ErrConnClosed 可重试，其余错误直接上报。
// isRetryable checks if the error is retryable:
//
//   - Connections closed to the peer can be retried
func (sink *streamSink) isRetryable(err error) bool {
	return errors.Is(err, wire.ErrConnClosed)
}

// Close 取消 peer ctx 并异步通知调度器 StreamStateClosed。
// Close closes the sink.
func (sink *streamSink) Close(ctx context.Context) error {
	sink.lazyInit()

	var err error

	sink.closeOnce.Do(func() {
		sink.cancel()

		// Best-effort inform the scheduler that we're done sending data.
		err = sink.Scheduler.SendMessageAsync(ctx, wire.StreamStatusMessage{
			StreamID: sink.Stream.ULID,
			State:    workflow.StreamStateClosed,
		})
	})

	return err
}
// Dialer 由 Worker 注入，与 listener 地址配对建立 worker 间数据通道。
