// agent_loop_buffer.go — turnBuffer：AgentLoop 内部线程安全阻塞队列。

package core

import "sync"

// turnBuffer AgentLoop 内部使用的线程安全阻塞缓冲队列。
type turnBuffer[T any] struct {
	buf      []T
	mu       sync.Mutex
	notEmpty *sync.Cond
	closed   bool
	woken    bool
}

// newTurnBuffer 创建空 turnBuffer。
func newTurnBuffer[T any]() *turnBuffer[T] {
	tb := &turnBuffer[T]{}
	tb.notEmpty = sync.NewCond(&tb.mu)
	return tb
}

// TrySend 入队；buffer 已关闭时返回 false，不 panic。
func (tb *turnBuffer[T]) TrySend(value T) bool {
	tb.mu.Lock()
	defer tb.mu.Unlock()

	if tb.closed {
		return false
	}

	tb.buf = append(tb.buf, value)
	tb.notEmpty.Signal()
	return true
}

// Receive 阻塞直至有元素、被 Wakeup 或关闭；空且关闭返回 false。
func (tb *turnBuffer[T]) Receive() (T, bool) {
	tb.mu.Lock()
	defer tb.mu.Unlock()

	for len(tb.buf) == 0 && !tb.closed && !tb.woken {
		tb.notEmpty.Wait()
	}

	tb.woken = false

	if len(tb.buf) == 0 {
		var zero T
		return zero, false
	}

	val := tb.buf[0]
	tb.buf = tb.buf[1:]
	return val, true
}

// Close 标记关闭并 Broadcast 唤醒所有等待者。
func (tb *turnBuffer[T]) Close() {
	tb.mu.Lock()
	defer tb.mu.Unlock()

	if !tb.closed {
		tb.closed = true
		tb.notEmpty.Broadcast()
	}
}

// IsClosed 查询是否已关闭。
func (tb *turnBuffer[T]) IsClosed() bool {
	tb.mu.Lock()
	defer tb.mu.Unlock()
	return tb.closed
}

// TakeAll 取出并清空当前队列。
func (tb *turnBuffer[T]) TakeAll() []T {
	tb.mu.Lock()
	defer tb.mu.Unlock()

	if len(tb.buf) == 0 {
		return nil
	}

	values := tb.buf
	tb.buf = nil
	return values
}

// PushFront 将元素插入队首（用于回退未处理项）。
func (tb *turnBuffer[T]) PushFront(values []T) {
	if len(values) == 0 {
		return
	}

	tb.mu.Lock()
	defer tb.mu.Unlock()

	tb.buf = append(append([]T{}, values...), tb.buf...)
	tb.notEmpty.Signal()
}

// Wakeup 唤醒阻塞在 Receive 上的 goroutine（如 idle 停止）。
func (tb *turnBuffer[T]) Wakeup() {
	tb.mu.Lock()
	defer tb.mu.Unlock()

	tb.woken = true
	tb.notEmpty.Broadcast()
}

// ClearWakeup 清除 woken 标志。
func (tb *turnBuffer[T]) ClearWakeup() {
	tb.mu.Lock()
	defer tb.mu.Unlock()

	tb.woken = false
}
