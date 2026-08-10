package core

// utils.go — 异步迭代器/生成器对与事件拷贝、Session 事件追加辅助。


import (
	"context"
	"sync"
)

// AsyncIterator 对类型化流提供阻塞式迭代，多读者各得一项。
// Multiple goroutines reading from the same iterator will receive each item
// exactly once.
type AsyncIterator[T any] struct {
	ch   chan iterationItem[T]
	done bool
}

type iterationItem[T any] struct {
	value T
	ok    bool
}

// NewAsyncIterator 创建带 64 缓冲的异步迭代器。
func NewAsyncIterator[T any]() *AsyncIterator[T] {
	return &AsyncIterator[T]{ch: make(chan iterationItem[T], 64)}
}

func (it *AsyncIterator[T]) Next() (T, bool) {
	item, ok := <-it.ch
	if !ok {
		it.done = true
		var zero T
		return zero, false
	}
	return item.value, item.ok
}

func (it *AsyncIterator[T]) Close() {
	if !it.done {
		it.done = true
		close(it.ch)
	}
}

// AsyncGenerator 向配对的 AsyncIterator 生产数据项。
// Multiple goroutines can safely Send to the same generator.
type AsyncGenerator[T any] struct {
	ch     chan iterationItem[T]
	closed bool
	mu     sync.Mutex
}

// NewAsyncIteratorPair 创建共享通道的迭代器/生成器对。
func NewAsyncIteratorPair[T any]() (*AsyncIterator[T], *AsyncGenerator[T]) {
	ch := make(chan iterationItem[T], 64)
	return &AsyncIterator[T]{ch: ch}, &AsyncGenerator[T]{ch: ch}
}

// Send 线程安全地向迭代器发送一项。
func (g *AsyncGenerator[T]) Send(value T) {
	g.mu.Lock()
	defer g.mu.Unlock()
	if !g.closed {
		g.ch <- iterationItem[T]{value: value, ok: true}
	}
}

func (g *AsyncGenerator[T]) trySend(value T) bool {
	g.mu.Lock()
	defer g.mu.Unlock()
	if g.closed {
		return false
	}
	select {
	case g.ch <- iterationItem[T]{value: value, ok: true}:
		return true
	default:
		return false
	}
}

// Close 关闭生成器并关闭底层通道。
func (g *AsyncGenerator[T]) Close() {
	g.mu.Lock()
	defer g.mu.Unlock()
	if !g.closed {
		g.closed = true
		close(g.ch)
	}
}

func (g *AsyncGenerator[T]) IsClosed() bool {
	g.mu.Lock()
	defer g.mu.Unlock()
	return g.closed
}

// SendCtx 发送时尊重 ctx 取消，避免消费者停止读取时 goroutine 泄漏。
// when the consumer stops reading from the iterator.
func (g *AsyncGenerator[T]) SendCtx(ctx context.Context, value T) bool {
	g.mu.Lock()
	if g.closed {
		g.mu.Unlock()
		return false
	}
	g.mu.Unlock()

	select {
	case g.ch <- iterationItem[T]{value: value, ok: true}:
		return true
	case <-ctx.Done():
		return false
	}
}

// ===== 拷贝辅助 =====

// copyTypedAgentEvent 深拷贝 TypedAgentEvent（RunPath/Output/Action）。
func copyTypedAgentEvent[M MessageType](event *TypedAgentEvent[M]) *TypedAgentEvent[M] {
	if event == nil {
		return nil
	}
	cp := &TypedAgentEvent[M]{
		AgentName: event.AgentName,
		Err:       event.Err,
	}
	if event.RunPath != nil {
		cp.RunPath = make([]RunStep, len(event.RunPath))
		for i, s := range event.RunPath {
			cp.RunPath[i] = RunStep{agentName: s.agentName}
		}
	}
	if event.Output != nil {
		cp.Output = &TypedAgentOutput[M]{CustomizedOutput: event.Output.CustomizedOutput}
		if event.Output.MessageOutput != nil {
			cp.Output.MessageOutput = &TypedMessageVariant[M]{
				IsStreaming: event.Output.MessageOutput.IsStreaming,
				Message:     event.Output.MessageOutput.Message,
				Role:        event.Output.MessageOutput.Role,
				AgenticRole: event.Output.MessageOutput.AgenticRole,
				ToolName:    event.Output.MessageOutput.ToolName,
			}
		}
	}
	if event.Action != nil {
		cp.Action = &AgentAction{
			Exit: event.Action.Exit, Interrupted: event.Action.Interrupted,
			TransferToAgent: event.Action.TransferToAgent, BreakLoop: event.Action.BreakLoop,
			CustomizedAction:    event.Action.CustomizedAction,
			internalInterrupted: event.Action.internalInterrupted,
		}
	}
	return cp
}

func setAutomaticClose[M MessageType](event *TypedAgentEvent[M]) {
	if event == nil {
		return
	}
	if event.Output != nil && event.Output.MessageOutput != nil {
		if event.Output.MessageOutput.MessageStream != nil {
			event.Output.MessageOutput.MessageStream.Close()
		}
	}
}
func typedSetAutomaticClose[M MessageType](event *TypedAgentEvent[M]) { setAutomaticClose(event) }
// addTypedEvent 向 Session 的 TypedEvents 追加类型化事件。
func addTypedEvent[M MessageType](s *runSession, event *TypedAgentEvent[M]) {
	if s == nil {
		return
	}
	s.mu.Lock()
	defer s.mu.Unlock()
	if s.TypedEvents == nil {
		events := make([]*TypedAgentEvent[M], 0)
		s.TypedEvents = &events
	}
	if te, ok := s.TypedEvents.(*[]*TypedAgentEvent[M]); ok {
		*te = append(*te, event)
	}
}

func copyMap[K comparable, V any](src map[K]V) map[K]V {
	if src == nil {
		return nil
	}
	dst := make(map[K]V, len(src))
	for k, v := range src {
		dst[k] = v
	}
	return dst
}

func cloneSlice[T any](src []T) []T {
	if src == nil {
		return nil
	}
	dst := make([]T, len(src))
	copy(dst, src)
	return dst
}

// copyMap/cloneSlice 提供浅拷贝 map 与 slice 的通用辅助。
