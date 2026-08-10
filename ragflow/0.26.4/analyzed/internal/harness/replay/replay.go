// Package replay 为 events 包录制的 Agent 执行轨迹提供确定性重放、Fork 与 Diff。
//
// ReplayEngine 从 EventLog 重放事件，可替换模型响应或工具结果；
// Fork 从任意点创建分支执行；Diff 对比两条轨迹以检测回归或行为变化。
package replay

import (
	"context"
	"time"

	"ragflow/internal/harness/events"
)

// ReplayConfig 确定性重放配置。
type ReplayConfig struct {
	// Store 重放事件源。
	Store events.EventLog

	// TraceID 待重放轨迹 ID。
	TraceID string

	// Start 起始逻辑时钟（0 表示从头）。
	Start uint64

	// End 结束逻辑时钟（0 表示至末尾）。
	End uint64

	// 替换策略（Model/Tool/State Override）。
	ModelOverride ModelOverrideFunc
	ToolOverride  ToolOverrideFunc
	StateOverride StateOverrideFunc

	// OutputStore 接收重放产生的事件（nil 则丢弃）。
	OutputStore events.EventLog

	// DiffEnabled 是否对比重放与原始轨迹。
	DiffEnabled bool
}

// ModelOverrideFunc 重放时替换 LLM 响应；非 nil *string 使用替换值，nil 使用录制值。
type ModelOverrideFunc func(messages []any, recordedResponse string) (*string, error)

// ToolOverrideFunc 重放时替换工具结果；非 nil 使用替换值，nil 使用录制值。
type ToolOverrideFunc func(toolName string, args map[string]any, recordedResult any) (any, error)

// StateOverrideFunc 重放时替换初始状态；nil 保留录制状态。
type StateOverrideFunc func(recordedState map[string]any) (map[string]any, error)

// ReplayResult 确定性重放结果。
type ReplayResult struct {
	// Events 重放产生的事件（设 OutputStore 时）。
	Events []*events.Event

	// OriginalLen 原始轨迹事件数。
	OriginalLen int

	// ReplayLen 重放产生的事件数。
	ReplayLen int

	// Divergences 重放与原始事件差异（DiffEnabled 时）。
	Divergences []EventDivergence

	// ReplayMetrics 重放操作指标。
	ReplayMetrics ReplayMetrics

	// Duration 重放操作耗时。
	Duration time.Duration
}

// EventDivergence 原始与重放事件的差异描述。
type EventDivergence struct {
	// Clock 事件日志中的逻辑时钟位置。
	Clock uint64

	// OriginalEvent 原始事件（重放新增时为 nil）。
	OriginalEvent *events.Event

	// ReplayEvent 重放事件（原始被跳过时为 nil）。
	ReplayEvent *events.Event

	// Type 差异类型。
	Type DivergenceType

	// Description 差异说明。
	Description string
}

// DivergenceType 事件差异分类。
type DivergenceType string

const (
	// DivergenceMissing 原始事件在重放中缺失。
	DivergenceMissing DivergenceType = "missing"
	// DivergenceExtra 重放产生了原始不存在的事件。
	DivergenceExtra DivergenceType = "extra"
	// DivergenceMismatch 两侧均存在但内容不同。
	DivergenceMismatch DivergenceType = "mismatch"
)

// ReplayMetrics 重放操作统计指标。
type ReplayMetrics struct {
	TotalEvents     int
	DivergenceCount int
	MatchCount      int
}

// ReplayEngine 从 EventLog 重放执行轨迹。
type ReplayEngine struct {
	store events.EventLog
}

// NewReplayEngine 创建绑定事件存储的重放引擎。
func NewReplayEngine(store events.EventLog) *ReplayEngine {
	return &ReplayEngine{store: store}
}

// Replay 对指定轨迹执行确定性重放，
// 顺序读取 EventLog 并可调用 ModelOverride/ToolOverride 替换非确定性操作。
func (e *ReplayEngine) Replay(ctx context.Context, cfg *ReplayConfig) (*ReplayResult, error) {
	start := time.Now()

	// 未设覆盖时使用精确重放默认值。
	modelOverride := cfg.ModelOverride
	if modelOverride == nil {
		modelOverride = func(_ []any, recorded string) (*string, error) {
			return &recorded, nil
		}
	}
	toolOverride := cfg.ToolOverride
	if toolOverride == nil {
		toolOverride = func(_ string, _ map[string]any, recorded any) (any, error) {
			return recorded, nil
		}
	}

	// Use config store, falling back to engine store.
	store := cfg.Store
	if store == nil {
		store = e.store
	}

	filter := events.EventFilter{
		TraceID:   cfg.TraceID,
		FromClock: cfg.Start,
		ToClock:   cfg.End,
	}

	iter := store.Stream(ctx, filter)
	defer iter.Close()

	result := &ReplayResult{}
	var originalEvents []*events.Event
	var replayEvents []*events.Event

	// 阶段 1：读取原始事件。
	for {
		ev, ok := iter.Next(ctx)
		if !ok {
			break
		}
		originalEvents = append(originalEvents, ev)
	}
	result.OriginalLen = len(originalEvents)

	// Apply StateOverride to the first EventStateWrite event (initial state).
	// Work on a copy to preserve the original for diff.
	if cfg.StateOverride != nil {
		for i, ev := range originalEvents {
			if ev.Type == events.EventStateWrite {
				var st events.StateTransitionPayload
				if ev.Payload != nil {
					_ = jsonUnmarshal(ev.Payload, &st)
				}
				recorded := map[string]any{st.Channel: st.NewValue}
				modified, err := cfg.StateOverride(recorded)
				if err != nil {
					return nil, err
				}
				if modified != nil {
					if val, ok := modified[st.Channel]; ok {
						st.NewValue = val
						repl := copyEvent(ev)
						repl.Payload, _ = jsonMarshal(st)
						repl.Seal()
						originalEvents[i] = repl
					}
				}
				break
			}
		}
	}

	// 阶段 2：带覆盖重放。
	// Copy each event before modifying so the original list is preserved
	// for accurate diff comparison.
	for _, original := range originalEvents {
		replayEv := copyEvent(original)

		switch original.Type {
		case events.EventLLMCallStart, events.EventLLMCallEnd:
			// 应用模型覆盖。
			if original.Type == events.EventLLMCallEnd {
				var payload events.LLMCallPayload
				_ = parsePayload(original, &payload)
				substituted, err := modelOverride(payload.Messages, payload.Content)
				if err != nil {
					return nil, err
				}
				if substituted != nil {
					payload.Content = *substituted
					replayEv.Payload, _ = jsonMarshal(payload)
					replayEv.Seal()
				}
			}
			replayEvents = append(replayEvents, replayEv)

		case events.EventToolCallStart, events.EventToolCallResult:
			// 应用工具覆盖。
			if original.Type == events.EventToolCallResult {
				var payload events.ToolCallPayload
				_ = parsePayload(original, &payload)
				substituted, err := toolOverride(payload.ToolName, payload.Arguments, payload.Result)
				if err != nil {
					return nil, err
				}
				if substituted != nil {
					payload.Result = substituted
					replayEv.Payload, _ = jsonMarshal(payload)
					replayEv.Seal()
				}
			}
			replayEvents = append(replayEvents, replayEv)

		default:
			replayEvents = append(replayEvents, replayEv)
		}
	}

	result.ReplayLen = len(replayEvents)

	// 阶段 3：可选 diff。
	var divergences []EventDivergence
	if cfg.DiffEnabled {
		divergences = diffEventLists(originalEvents, replayEvents)
		result.Divergences = divergences
	}

	// 填充 ReplayMetrics。
	divergenceCount := len(divergences)
	replayMetrics := ReplayMetrics{
		TotalEvents:     result.ReplayLen,
		DivergenceCount: divergenceCount,
		MatchCount:      result.ReplayLen - divergenceCount,
	}
	result.ReplayMetrics = replayMetrics

	// 阶段 4：可选写入 OutputStore。
	if cfg.OutputStore != nil {
		if err := cfg.OutputStore.Append(ctx, replayEvents...); err != nil {
			return nil, err
		}
		result.Events = replayEvents
	}

	result.Duration = time.Since(start)
	return result, nil
}

// parsePayload 从事件反序列化类型化载荷。
func parsePayload(ev *events.Event, target any) error {
	if ev.Payload == nil {
		return nil
	}
	return jsonUnmarshal(ev.Payload, target)
}

// diffEventLists 对比原始与重放事件列表。
func diffEventLists(original, replayed []*events.Event) []EventDivergence {
	var divergences []EventDivergence
	maxLen := len(original)
	if len(replayed) > maxLen {
		maxLen = len(replayed)
	}

	for i := 0; i < maxLen; i++ {
		var orig *events.Event
		var replay *events.Event

		if i < len(original) {
			orig = original[i]
		}
		if i < len(replayed) {
			replay = replayed[i]
		}

		if orig == nil && replay != nil {
			divergences = append(divergences, EventDivergence{
				Clock:       replay.Clock,
				ReplayEvent: replay,
				Type:        DivergenceExtra,
				Description: "replay produced extra event",
			})
			continue
		}
		if orig != nil && replay == nil {
			divergences = append(divergences, EventDivergence{
				Clock:         orig.Clock,
				OriginalEvent: orig,
				Type:          DivergenceMissing,
				Description:   "original event missing in replay",
			})
			continue
		}

		// 两侧均存在——比较类型与哈希。
		if orig.Type != replay.Type {
			divergences = append(divergences, EventDivergence{
				Clock:         orig.Clock,
				OriginalEvent: orig,
				ReplayEvent:   replay,
				Type:          DivergenceMismatch,
				Description:   "event type mismatch",
			})
		}
		if orig.Hash != replay.Hash {
			divergences = append(divergences, EventDivergence{
				Clock:         orig.Clock,
				OriginalEvent: orig,
				ReplayEvent:   replay,
				Type:          DivergenceMismatch,
				Description:   "payload mismatch (hash differs)",
			})
		}
	}

	return divergences
}
