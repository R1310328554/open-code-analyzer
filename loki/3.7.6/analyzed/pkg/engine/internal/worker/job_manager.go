package worker

// jobManager 是调度器与 worker 线程之间的任务分发桥梁：线程通过 Recv 阻塞等待任务，worker 通过 Send 投递已分配的作业。

import (
	"context"
	"errors"
	"sync"

	"github.com/oklog/ulid/v2"

	"github.com/grafana/loki/v3/pkg/engine/internal/scheduler/wire"
	"github.com/grafana/loki/v3/pkg/engine/internal/workflow"
)

// errNoReadyThreads 表示 Send 时没有线程在 Recv 上等待，任务无法立即下发。
// errNoReadyThreads is returned by [jobManager.Send] when there are no ready
// threads waiting to receive a task.
var errNoReadyThreads = errors.New("no ready threads")

// threadJob 封装单次任务执行所需的上下文、调度器连接、源流/汇流及清理回调。
// threadJob is an individual task to run.
type threadJob struct {
	Context context.Context
	Cancel  context.CancelFunc

	Scheduler *wire.Peer     // Scheduler which owns the task.
	Task      *workflow.Task // Task to execute.

	Sources map[ulid.ULID]*streamSource // Sources to read task data from.
	Sinks   map[ulid.ULID]*streamSink   // Sinks to write task data to.

	Close func() // Close function to clean up resources for the job.
}

// jobManager 协调 worker 与线程：Recv 表示线程就绪，Send 分配任务，WaitReady 通知调度器。
// jobManager is the bridge between the worker's communication to a scheduler
// and worker threads.
//
//   - Worker threads call [jobManager.Recv] when they are ready for a new job.
//
//   - The worker calls [jobManager.Send] when it is assigned a task from the
//     scheduler, which fails if there are no ready threads.
//
//   - The worker calls [jobManager.WaitReady] to inform a scheduler when
//     there is at least one ready worker thread.
type jobManager struct {
	mut        sync.RWMutex
	waiting    int
	waitCond   chan struct{} // Channel-based condition variable to permit cancellation
	cancelCond chan struct{} // Channel-based condition variable to cancel Recv

	jobCh chan *threadJob
}

// newJobManager 初始化基于 channel 的条件变量与无缓冲 job 通道。
// newJobManager returns a new jobManager.
func newJobManager() *jobManager {
	return &jobManager{
		waitCond:   make(chan struct{}),
		cancelCond: make(chan struct{}),

		jobCh: make(chan *threadJob),
	}
}

// Recv 阻塞直到 Send 投递任务或 ctx 取消；取消时会唤醒等待中的 Send 避免死锁。
// Recv retrieves the next job to execute, blocking until a job is sent or
// ctx is done. Jobs are sent by a call to [jobManager.Send].
//
// Recv returns an error if ctx completes before a job is sent.
func (jm *jobManager) Recv(ctx context.Context) (*threadJob, error) {
	// Notify all waiters that there's a call to Recv.
	jm.broadcast()

	select {
	case <-ctx.Done():
		// The caller gave up on waiting for a job. Before we return, we want to
		// inform any waiting Send calls that they should try again, otherwise
		// they would be blocked forever.
		jm.cancelWaiting()

		return nil, ctx.Err()
	case job := <-jm.jobCh:
		return job, nil
	}
}

// broadcast 递增 waiting 计数并关闭 waitCond，唤醒所有 WaitReady 调用者。
// broadcast wakes all blocked calls to WaitReady.
func (jm *jobManager) broadcast() {
	jm.mut.Lock()
	defer jm.mut.Unlock()

	jm.waiting++

	close(jm.waitCond) // Wakes all blocked calls to WaitReady
	jm.waitCond = make(chan struct{})
}

// cancelWaiting 在 Recv 被取消时递减 waiting 并关闭 cancelCond，解除 Send 阻塞。
// cancelWaiting wakes all blocked calls to Send.
func (jm *jobManager) cancelWaiting() {
	jm.mut.Lock()
	defer jm.mut.Unlock()

	if jm.waiting == 0 {
		panic("jobManager.cancelWaiting called with no waiting calls")
	}
	jm.waiting--

	close(jm.cancelCond)
	jm.cancelCond = make(chan struct{})
}

// Send 在 waiting>0 时向 jobCh 投递任务；无就绪线程或 ctx 取消则返回相应错误。
// Send delivers a job to a waiting call to [jobManager.Recv]. If there are no
// waiting calls to Recv, Send returns an error.
func (jm *jobManager) Send(ctx context.Context, job *threadJob) error {
	jm.mut.Lock()
	defer jm.mut.Unlock()

	var sent bool

	for ctx.Err() == nil && !sent && jm.waiting != 0 {
		// Get the current Send cancellation condition variable channel.
		//
		// cancelCh will be closed if *any* waiting call to Recv is cancelled.
		// This may cause spurious wake ups of calls to Send, but since
		// cancelling Recv is rare (only on shutdown), it should have minimal
		// overhead.
		cancelCh := jm.cancelCond

		// Unlock the mutex while waiting to make progress.
		jm.mut.Unlock()

		select {
		case <-ctx.Done():
		case <-cancelCh:
		case jm.jobCh <- job:
			sent = true
		}

		// Re-lock the mutex so the condition can be safely checked again on the
		// next loop.
		jm.mut.Lock()
	}

	switch {
	case sent:
		jm.waiting--
		return nil
	case !sent && ctx.Err() != nil:
		return ctx.Err()
	default:
		return errNoReadyThreads
	}
}

// WaitReady 阻塞直到至少一个线程调用 Recv，使调度器知晓可下发新任务。
// WaitReady blocks until there is at least one waiting call to
// [jobManager.Recv], indicating that [jobManager.Send] can be called.
//
// Note that other goroutines may consume all Send slots between this call
// returning and calling Send, causing Send to return an error.
func (jm *jobManager) WaitReady(ctx context.Context) error {
	jm.mut.RLock()
	defer jm.mut.RUnlock()

	for ctx.Err() == nil && jm.waiting == 0 {
		// Get the current condition variable channel.
		waitCh := jm.waitCond

		// Unlock the mutex while waiting for waitCh to close.
		jm.mut.RUnlock()

		select {
		case <-ctx.Done():
		case <-waitCh:
		}

		// Re-lock the mutex so the condition can be safely checked again on
		// the next loop.
		jm.mut.RLock()
	}

	return ctx.Err()
}
// Send 与 Recv 通过 mutex 与双 channel 条件变量实现无丢失的任务握手。
