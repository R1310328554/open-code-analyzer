package core

// session.go — 运行会话与上下文：事件历史、并行分支合并、Session 值与 RunPath 管理。


import (
	"bytes"
	"context"
	"encoding/gob"
	"fmt"
	"reflect"
	"sort"
	"sync"
	"time"

	"ragflow/internal/harness/core/schema"
)

func init() {
	schema.RegisterType("_harness_event_wrap_entry", func() any { return &eventWrapEntry{} })
}

// eventWrapEntry 为检查点持久化包装事件及时间戳。
type eventWrapEntry struct {
	Event     any
	Timestamp int64
}

// consumeStream 若事件含流式消息则在检查点前完整消费，
// 避免检查点中残留不完整流数据。
func (e *eventWrapEntry) consumeStream() {
	if e.Event == nil {
		return
	}
	ev, ok := e.Event.(*AgentEvent)
	if !ok || ev.Output == nil || ev.Output.MessageOutput == nil {
		return
	}
	mv := ev.Output.MessageOutput
	if !mv.IsStreaming || mv.MessageStream == nil {
		return
	}
	merged, err := schema.ConcatMessageStream(mv.MessageStream)
	if err == nil {
		mv.Message = merged
		mv.IsStreaming = false
		mv.MessageStream = nil
	}
}

func (e *eventWrapEntry) GobEncode() ([]byte, error) {
	var buf bytes.Buffer
	enc := gob.NewEncoder(&buf)
	if err := enc.Encode(e.Timestamp); err != nil {
		return nil, err
	}
	if e.Event == nil {
		if err := enc.Encode(false); err != nil {
			return nil, err
		}
	} else {
		if err := enc.Encode(true); err != nil {
			return nil, err
		}
		typeName := reflect.TypeOf(e.Event).String()
		// Gob-registered types use their registered name; try direct encode first.
		if err := enc.Encode(&typeName); err != nil {
			return nil, err
		}
		if err := enc.Encode(e.Event); err != nil {
			return nil, fmt.Errorf("gob encode event (%s): %w", typeName, err)
		}
	}
	return buf.Bytes(), nil
}

func (e *eventWrapEntry) GobDecode(data []byte) error {
	buf := bytes.NewBuffer(data)
	dec := gob.NewDecoder(buf)
	if err := dec.Decode(&e.Timestamp); err != nil {
		return err
	}
	var nonNil bool
	if err := dec.Decode(&nonNil); err != nil {
		return err
	}
	if nonNil {
		var typeName string
		if err := dec.Decode(&typeName); err != nil {
			return err
		}
		// Decode into generic interface{} — gob will reconstruct registered types.
		e.Event = new(any)
		if err := dec.Decode(e.Event); err != nil {
			return fmt.Errorf("gob decode event: type %q may not be registered; wrap with schema.RegisterName: %w", typeName, err)
		}
		// Decode into interface{} wraps in a *any; unwrap.
		if p, ok := e.Event.(*any); ok {
			e.Event = *p
		}
	}
	return nil
}

// branchEvents 并行工作流中每条 lane 的事件历史，
// 各并行分支拥有独立 branchEvents，通过 Parent 形成链表；
// join 时按时间戳合并为全局有序事件序列。
type branchEvents struct {
	Events []*eventWrapEntry
	Parent *branchEvents
}

// runSession 单次智能体运行的可变状态（Values、事件、分支）。
type runSession struct {
	mu           sync.Mutex
	Values       map[string]any
	valuesMx     *sync.Mutex
	events       []*eventWrapEntry
	BranchEvents *branchEvents
	TypedEvents  any // *[]*typedAgentEventWrapper[M] for AgenticMessage path (gob-encodable)
}

func newRunSession() *runSession {
	return &runSession{Values: make(map[string]any), valuesMx: &sync.Mutex{}}
}

func (s *runSession) addEvent(event any) {
	entry := &eventWrapEntry{Event: event, Timestamp: time.Now().UnixNano()}
	entry.consumeStream()

	// If in a parallel lane, append to the lane's local event slice (lock-free).
	if s.BranchEvents != nil {
		s.BranchEvents.Events = append(s.BranchEvents.Events, entry)
		return
	}

	// Otherwise, on the main path. Append to shared Events slice (with lock).
	s.mu.Lock()
	s.events = append(s.events, entry)
	s.mu.Unlock()
}

func (s *runSession) getEvents() []any {
	// If there are no in-flight lane events, return the main slice directly.
	if s.BranchEvents == nil {
		s.mu.Lock()
		r := unwrapEvents(s.events)
		s.mu.Unlock()
		return r
	}

	// Collect committed events from main slice.
	s.mu.Lock()
	committed := make([]*eventWrapEntry, len(s.events))
	copy(committed, s.events)
	s.mu.Unlock()

	// Traverse the lane linked list to collect in-flight events.
	var all []*eventWrapEntry
	all = append(all, committed...)
	for lane := s.BranchEvents; lane != nil; lane = lane.Parent {
		all = append(all, lane.Events...)
	}

	// Sort all events by timestamp for chronological order.
	sort.Slice(all, func(i, j int) bool {
		return all[i].Timestamp < all[j].Timestamp
	})

	return unwrapEvents(all)
}

// unwrapEvents 从 eventWrapEntry 切片提取内部 Event。
func unwrapEvents(entries []*eventWrapEntry) []any {
	r := make([]any, 0, len(entries))
	for _, e := range entries {
		if e != nil {
			r = append(r, e.Event)
		}
	}
	return r
}

// runContext 运行期元数据：根输入、RunPath 与会话引用。
type runContext struct {
	mu        sync.Mutex
	RootInput any
	RunPath   []RunStep
	Session   *runSession
}

// getRunPath 加锁复制 RunPath 后返回。
func (rc *runContext) getRunPath() []RunStep {
	if rc == nil {
		return nil
	}
	rc.mu.Lock()
	defer rc.mu.Unlock()
	cp := make([]RunStep, len(rc.RunPath))
	copy(cp, rc.RunPath)
	return cp
}

// setRunPath 加锁替换 RunPath。
func (rc *runContext) setRunPath(v []RunStep) {
	if rc == nil {
		return
	}
	rc.mu.Lock()
	rc.RunPath = v
	rc.mu.Unlock()
}

// appendRunPath 加锁追加 RunPath 步骤。
func (rc *runContext) appendRunPath(v RunStep) {
	if rc == nil {
		return
	}
	rc.mu.Lock()
	rc.RunPath = append(rc.RunPath, v)
	rc.mu.Unlock()
}

type runContextKey struct{}

// ctxWithNewTypedRunCtx 创建隔离的新 runContext 并注入 ctx。
func ctxWithNewTypedRunCtx[M MessageType](ctx context.Context, input *TypedAgentInput[M], _ bool) context.Context {
	// sharedParentSession parameter is reserved for future use.
	// Currently a new isolated session is always created.
	rc := &runContext{RootInput: input, RunPath: make([]RunStep, 0), Session: newRunSession()}
	return context.WithValue(ctx, runContextKey{}, rc)
}

// initRunCtx 初始化或扩展运行上下文并将智能体名追加到 RunPath。
// 若 ctx 中已有 runContext 则复用——嵌套调用共享 Session 与 Values，
// RunPath 在整条调用链上累积。
// RunPath accumulates across all agents in the call chain.
func initRunCtx(ctx context.Context, agentName string, input *AgentInput) (context.Context, *runContext) {
	rc := getRunCtx(ctx)
	if rc == nil {
		rc = &runContext{RootInput: input, RunPath: make([]RunStep, 0), Session: newRunSession()}
		ctx = context.WithValue(ctx, runContextKey{}, rc)
	}
	rc.appendRunPath(RunStep{agentName: agentName})
	return ctx, rc
}

func getRunCtx(ctx context.Context) *runContext {
	if v := ctx.Value(runContextKey{}); v != nil {
		return v.(*runContext)
	}
	return nil
}

func setRunCtx(ctx context.Context, rc *runContext) context.Context {
	return context.WithValue(ctx, runContextKey{}, rc)
}

// forkRunCtx 为并行子 lane fork 运行上下文，共享 Values 但独立 BranchEvents。
func forkRunCtx(ctx context.Context) context.Context {
	parent := getRunCtx(ctx)
	if parent == nil || parent.Session == nil {
		return ctx
	}

	// Create a new session for the child lane.
	// Share committed history (Events) and values, but give the child its own BranchEvents.
	parent.Session.mu.Lock()
	eventsCopy := make([]*eventWrapEntry, len(parent.Session.events))
	copy(eventsCopy, parent.Session.events)
	parent.Session.mu.Unlock()

	childSession := &runSession{
		events:   eventsCopy,
		Values:   parent.Session.Values, // Share values map
		valuesMx: parent.Session.valuesMx,
	}

	childSession.BranchEvents = &branchEvents{
		Parent: parent.Session.BranchEvents,
		Events: make([]*eventWrapEntry, 0),
	}

	// Create a new runContext for the child, pointing to the new session.
	child := &runContext{
		RootInput: parent.RootInput,
		RunPath:   parent.getRunPath(),
		Session:   childSession,
	}
	return context.WithValue(ctx, runContextKey{}, child)
}

func updateRunPathOnly(ctx context.Context, steps ...string) context.Context {
	rc := getRunCtx(ctx)
	if rc == nil {
		return ctx
	}
	newPath := make([]RunStep, 0, len(steps))
	for _, s := range steps {
		newPath = append(newPath, RunStep{agentName: s})
	}
	rc.setRunPath(newPath)
	return ctx
}

// joinRunCtxs 合并子 lane 事件并按时间戳排序写回父 Session。
func joinRunCtxs(ctx context.Context, childCtxs ...context.Context) {
	parent := getRunCtx(ctx)
	if parent == nil || parent.Session == nil {
		return
	}

	switch len(childCtxs) {
	case 0:
		return
	case 1:
		// Optimization: single branch, no sorting needed.
		newEvents := unwindLaneEvents(childCtxs...)
		commitEvents(parent, newEvents)
		return
	}

	// Collect events from all child lanes.
	newEvents := unwindLaneEvents(childCtxs...)

	// Sort by timestamp for chronological order.
	sort.Slice(newEvents, func(i, j int) bool {
		return newEvents[i].Timestamp < newEvents[j].Timestamp
	})

	commitEvents(parent, newEvents)
}

// commitEvents 将事件提交到父 lane 或主事件日志。
func commitEvents(rc *runContext, entries []*eventWrapEntry) {
	if rc == nil || rc.Session == nil {
		return
	}
	if rc.Session.BranchEvents != nil {
		// If committing to a lane, append to its event slice.
		rc.Session.BranchEvents.Events = append(rc.Session.BranchEvents.Events, entries...)
	} else {
		// Otherwise, commit to main shared Events slice with lock.
		rc.Session.mu.Lock()
		rc.Session.events = append(rc.Session.events, entries...)
		rc.Session.mu.Unlock()
	}
}

// unwindLaneEvents 遍历 BranchEvents 链表收集各 lane 事件。
// 沿 Parent 链捕获深层 fork 分支的全部事件。
func unwindLaneEvents(ctxs ...context.Context) []*eventWrapEntry {
	var all []*eventWrapEntry
	for _, ctx := range ctxs {
		rc := getRunCtx(ctx)
		if rc == nil || rc.Session == nil {
			continue
		}
		for lane := rc.Session.BranchEvents; lane != nil; lane = lane.Parent {
			all = append(all, lane.Events...)
		}
	}
	return all
}

func getSession(ctx context.Context) *runSession {
	if rc := getRunCtx(ctx); rc != nil {
		return rc.Session
	}
	return nil
}

// AddSessionValues 向当前 Session 批量写入键值。
func AddSessionValues(ctx context.Context, values map[string]any) {
	rc := getRunCtx(ctx)
	if rc == nil || rc.Session == nil || values == nil {
		return
	}
	rc.Session.valuesMx.Lock()
	defer rc.Session.valuesMx.Unlock()
	for k, v := range values {
		rc.Session.Values[k] = v
	}
}

// GobEncode/Decode 使 eventWrapEntry 可跨检查点序列化。
