// Package pregel 为 Pregel 执行提供流式事件支持。
//
// StreamManager 按 StreamMode 过滤并广播检查点、任务、状态更新等事件。
package pregel

import (
	"context"
	"encoding/json"
	"fmt"
	"sync"
	"time"

	"ragflow/internal/harness/graph/types"
)

// StreamEventType 流式事件类型枚举。
type StreamEventType string

const (
	// EventTypeCheckpoint 超步开始时发出检查点快照
	EventTypeCheckpoint StreamEventType = "checkpoint"
	// EventTypeTaskStart 任务开始执行
	EventTypeTaskStart StreamEventType = "task_start"
	// EventTypeTaskEnd 任务执行结束
	EventTypeTaskEnd StreamEventType = "task_end"
	// EventTypeUpdate 节点更新状态
	EventTypeUpdate StreamEventType = "update"
	// EventTypeValues 发出当前通道值快照
	EventTypeValues StreamEventType = "values"
	// EventTypeInterrupt 执行被中断
	EventTypeInterrupt StreamEventType = "interrupt"
	// EventTypeError 执行出错
	EventTypeError StreamEventType = "error"
	// EventTypeFinal 执行完成并携带最终状态
	EventTypeFinal StreamEventType = "final"
	// EventTypeDebug 调试信息
	EventTypeDebug StreamEventType = "debug"
)

// StreamEvent 流式事件记录。
type StreamEvent struct {
	// Type is the event type
	Type StreamEventType
	// Timestamp is when the event occurred
	Timestamp time.Time
	// Step is the current step number
	Step int
	// Node is the node name (for task events)
	Node string
	// TaskID is the task ID (for task events)
	TaskID string
	// Data is the event-specific data
	Data any
	// Error is the error (for error events)
	Error error
}

// NewStreamEvent 创建带时间戳的流式事件。
func NewStreamEvent(eventType StreamEventType, step int) *StreamEvent {
	return &StreamEvent{
		Type:      eventType,
		Timestamp: time.Now(),
		Step:      step,
		Data:      make(map[string]any),
	}
}

// ToJSON 序列化为 JSON。
func (e *StreamEvent) ToJSON() ([]byte, error) {
	return json.Marshal(e)
}

// StreamManager 管理 Pregel 流式事件的发射与过滤。
type StreamManager struct {
	mode          types.StreamMode
	eventCh       chan *StreamEvent
	errorCh       chan error
	bufferSize    int
	includeFilter map[StreamEventType]bool
	mu            struct {
		sync.RWMutex
		closed bool
	}
}

// NewStreamManager 创建流管理器，按 mode 配置过滤。
func NewStreamManager(mode types.StreamMode, bufferSize int) *StreamManager {
	if bufferSize <= 0 {
		bufferSize = 100
	}

	sm := &StreamManager{
		mode:          mode,
		eventCh:       make(chan *StreamEvent, bufferSize),
		errorCh:       make(chan error, 10),
		bufferSize:    bufferSize,
		includeFilter: make(map[StreamEventType]bool),
	}

	// Set up include filter based on stream mode
	sm.setupIncludeFilter()

	return sm
}

// setupIncludeFilter 根据 StreamMode 配置事件白名单。
func (sm *StreamManager) setupIncludeFilter() {
	switch sm.mode {
	case types.StreamModeValues:
		sm.includeFilter[EventTypeValues] = true
		sm.includeFilter[EventTypeFinal] = true

	case types.StreamModeUpdates:
		sm.includeFilter[EventTypeUpdate] = true
		sm.includeFilter[EventTypeFinal] = true

	case types.StreamModeTasks:
		sm.includeFilter[EventTypeTaskStart] = true
		sm.includeFilter[EventTypeTaskEnd] = true
		sm.includeFilter[EventTypeError] = true
		sm.includeFilter[EventTypeFinal] = true

	case types.StreamModeCheckpoints:
		sm.includeFilter[EventTypeCheckpoint] = true
		sm.includeFilter[EventTypeFinal] = true

	case types.StreamModeDebug:
		// Include all events in debug mode
		sm.includeFilter[EventTypeCheckpoint] = true
		sm.includeFilter[EventTypeTaskStart] = true
		sm.includeFilter[EventTypeTaskEnd] = true
		sm.includeFilter[EventTypeUpdate] = true
		sm.includeFilter[EventTypeValues] = true
		sm.includeFilter[EventTypeInterrupt] = true
		sm.includeFilter[EventTypeError] = true
		sm.includeFilter[EventTypeDebug] = true
		sm.includeFilter[EventTypeFinal] = true

	default:
		// Default to values mode
		sm.includeFilter[EventTypeValues] = true
		sm.includeFilter[EventTypeFinal] = true
	}
}

// shouldEmit 检查事件类型是否应发射。
func (sm *StreamManager) shouldEmit(eventType StreamEventType) bool {
	sm.mu.RLock()
	defer sm.mu.RUnlock()
	return sm.includeFilter[eventType]
}

// EmitCheckpoint 发射检查点事件。
func (sm *StreamManager) EmitCheckpoint(step int, checkpoint map[string]any) {
	if !sm.shouldEmit(EventTypeCheckpoint) {
		return
	}

	event := NewStreamEvent(EventTypeCheckpoint, step)
	event.Data = map[string]any{
		"checkpoint": checkpoint,
	}

	sm.emit(event)
}

// EmitTaskStart 发射任务开始事件。
func (sm *StreamManager) EmitTaskStart(step int, node string, taskID string) {
	if !sm.shouldEmit(EventTypeTaskStart) {
		return
	}

	event := NewStreamEvent(EventTypeTaskStart, step)
	event.Node = node
	event.TaskID = taskID
	event.Data = map[string]any{
		"node":    node,
		"task_id": taskID,
	}

	sm.emit(event)
}

// EmitTaskEnd 发射任务结束事件（含耗时）。
func (sm *StreamManager) EmitTaskEnd(step int, node string, taskID string, output any, duration time.Duration, err error) {
	if !sm.shouldEmit(EventTypeTaskEnd) {
		return
	}

	event := NewStreamEvent(EventTypeTaskEnd, step)
	event.Node = node
	event.TaskID = taskID
	event.Error = err
	event.Data = map[string]any{
		"node":     node,
		"task_id":  taskID,
		"output":   output,
		"duration": duration.String(),
	}

	sm.emit(event)
}

// EmitUpdate 发射状态更新事件。
func (sm *StreamManager) EmitUpdate(step int, node string, output any) {
	if !sm.shouldEmit(EventTypeUpdate) {
		return
	}

	event := NewStreamEvent(EventTypeUpdate, step)
	event.Node = node
	event.Data = map[string]any{
		"node":   node,
		"output": output,
	}

	sm.emit(event)
}

// EmitValues 发射通道值快照事件。
func (sm *StreamManager) EmitValues(step int, values map[string]any) {
	if !sm.shouldEmit(EventTypeValues) {
		return
	}

	event := NewStreamEvent(EventTypeValues, step)
	event.Data = map[string]any{
		"values": values,
	}

	sm.emit(event)
}

// EmitInterrupt 发射中断事件。
func (sm *StreamManager) EmitInterrupt(step int, interrupts []string) {
	if !sm.shouldEmit(EventTypeInterrupt) {
		return
	}

	event := NewStreamEvent(EventTypeInterrupt, step)
	event.Data = map[string]any{
		"interrupts": interrupts,
	}

	sm.emit(event)
}

// EmitError 发射错误事件。
func (sm *StreamManager) EmitError(step int, err error, node string) {
	if !sm.shouldEmit(EventTypeError) {
		return
	}

	event := NewStreamEvent(EventTypeError, step)
	event.Node = node
	event.Error = err
	event.Data = map[string]any{
		"node":  node,
		"error": err.Error(),
	}

	sm.emit(event)
}

// EmitDebug 发射调试事件。
func (sm *StreamManager) EmitDebug(step int, message string, data any) {
	if !sm.shouldEmit(EventTypeDebug) {
		return
	}

	event := NewStreamEvent(EventTypeDebug, step)
	event.Data = map[string]any{
		"message": message,
		"data":    data,
	}

	sm.emit(event)
}

// EmitFinal 阻塞发送最终状态事件（保证 RunSync 不丢数据）。
// Uses blocking channel send to guarantee delivery — dropping the final event
// would cause RunSync to return nil state (silent data loss).
func (sm *StreamManager) EmitFinal(step int, state any) {
	if !sm.shouldEmit(EventTypeFinal) {
		return
	}

	event := NewStreamEvent(EventTypeFinal, step)
	event.Data = map[string]any{
		"state": state,
	}
	sm.eventCh <- event
}

// emit 非阻塞发送事件；通道满时丢弃。
func (sm *StreamManager) emit(event *StreamEvent) {
	sm.mu.RLock()
	defer sm.mu.RUnlock()

	if sm.mu.closed {
		return
	}

	select {
	case sm.eventCh <- event:
		// Event sent
	default:
		// Channel full, drop event
	}
}

// Events 返回事件通道（只读）。
func (sm *StreamManager) Events() <-chan *StreamEvent {
	return sm.eventCh
}

// Errors 返回错误通道（只读）。
func (sm *StreamManager) Errors() <-chan error {
	return sm.errorCh
}

// Close 关闭流管理器并关闭通道。
func (sm *StreamManager) Close() {
	sm.mu.Lock()
	defer sm.mu.Unlock()

	if !sm.mu.closed {
		sm.mu.closed = true
		close(sm.eventCh)
		close(sm.errorCh)
	}
}

// StreamWriter 为节点函数提供流式调试输出。
type StreamWriter struct {
	streamManager *StreamManager
	step          int
	node          string
}

// NewStreamWriter 创建流式写入器。
func NewStreamWriter(sm *StreamManager, step int, node string) *StreamWriter {
	return &StreamWriter{
		streamManager: sm,
		step:          step,
		node:          node,
	}
}

// Write 将数据作为调试事件写入流。
func (w *StreamWriter) Write(data any) error {
	w.streamManager.EmitDebug(w.step, fmt.Sprintf("custom output from node %s", w.node), data)
	return nil
}

// WriteJSON 序列化后写入流。
func (w *StreamWriter) WriteJSON(data any) error {
	jsonData, err := json.Marshal(data)
	if err != nil {
		return err
	}
	return w.Write(jsonData)
}

// EventStream 独立的事件流（含 context 生命周期）。
type EventStream struct {
	ctx          context.Context
	cancel       context.CancelFunc
	streamEvents chan *StreamEvent
	streamErrors chan error
}

// NewEventStream 创建可取消的事件流。
func NewEventStream(ctx context.Context) *EventStream {
	ctx, cancel := context.WithCancel(ctx)
	return &EventStream{
		ctx:          ctx,
		cancel:       cancel,
		streamEvents: make(chan *StreamEvent, 100),
		streamErrors: make(chan error, 10),
	}
}

// Context 返回流的 context。
func (es *EventStream) Context() context.Context {
	return es.ctx
}

// Cancel 取消流并关闭通道。
func (es *EventStream) Cancel() {
	es.cancel()
	close(es.streamEvents)
	close(es.streamErrors)
}

// Emit 向流发送事件。
func (es *EventStream) Emit(event *StreamEvent) {
	select {
	case es.streamEvents <- event:
	case <-es.ctx.Done():
	}
}

// EmitError 向流发送错误。
func (es *EventStream) EmitError(err error) {
	select {
	case es.streamErrors <- err:
	case <-es.ctx.Done():
	}
}

// Stream 返回事件与错误通道。
func (es *EventStream) Stream() (<-chan *StreamEvent, <-chan error) {
	return es.streamEvents, es.streamErrors
}

// StreamProcessor 流事件处理器（过滤/变换/处理链）。
type StreamProcessor struct {
	filter    func(*StreamEvent) bool
	transform func(*StreamEvent) (*StreamEvent, error)
	handler   func(*StreamEvent)
}

// NewStreamProcessor 创建默认流处理器。
func NewStreamProcessor() *StreamProcessor {
	return &StreamProcessor{
		filter:    func(e *StreamEvent) bool { return true },
		transform: func(e *StreamEvent) (*StreamEvent, error) { return e, nil },
		handler:   func(e *StreamEvent) {},
	}
}

// WithFilter 设置事件过滤器。
func (sp *StreamProcessor) WithFilter(filter func(*StreamEvent) bool) *StreamProcessor {
	sp.filter = filter
	return sp
}

// WithTransform 设置事件变换器。
func (sp *StreamProcessor) WithTransform(transform func(*StreamEvent) (*StreamEvent, error)) *StreamProcessor {
	sp.transform = transform
	return sp
}

// WithHandler 设置事件处理函数。
func (sp *StreamProcessor) WithHandler(handler func(*StreamEvent)) *StreamProcessor {
	sp.handler = handler
	return sp
}

// Process 过滤 → 变换 → 处理单条事件。
func (sp *StreamProcessor) Process(event *StreamEvent) error {
	if !sp.filter(event) {
		return nil
	}

	transformed, err := sp.transform(event)
	if err != nil {
		return err
	}

	sp.handler(transformed)
	return nil
}

// ProcessStream 消费事件通道直至关闭或 ctx 取消。
func (sp *StreamProcessor) ProcessStream(ctx context.Context, eventCh <-chan *StreamEvent) error {
	for {
		select {
		case <-ctx.Done():
			return ctx.Err()
		case event, ok := <-eventCh:
			if !ok {
				return nil
			}
			if err := sp.Process(event); err != nil {
				return err
			}
		}
	}
}
