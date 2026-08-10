package replay

import (
	"context"

	"ragflow/internal/harness/events"
)

// DiffResult 两条执行轨迹的对比结果。
type DiffResult struct {
	// LeftTraceID 左侧（参考）轨迹 ID。
	LeftTraceID string

	// RightTraceID 右侧（候选）轨迹 ID。
	RightTraceID string

	// MissingInRight 左有右无的事件。
	MissingInRight []*events.Event

	// MissingInLeft 右有左无的事件。
	MissingInLeft []*events.Event

	// Mismatched 两侧均存在但载荷不同的事件。
	Mismatched []EventMismatch

	// StateDiff 状态转换差异。
	StateDiff map[string]StateDiff

	// ToolCallDiff 工具调用差异。
	ToolCallDiff []ToolCallDiff

	// LLMResponseDiff LLM 响应差异。
	LLMResponseDiff []LLMResponseDiff

	// FinalOutputDiff 最终输出差异（相同时为空）。
	FinalOutputDiff string
}

// EventMismatch 单条事件级差异描述。
type EventMismatch struct {
	Clock      uint64
	LeftEvent  *events.Event
	RightEvent *events.Event
	Field      string
	LeftValue  string
	RightValue string
}

// StateDiff 特定时刻的状态差异。
type StateDiff struct {
	Clock      uint64
	Key        string
	LeftValue  any
	RightValue any
}

// ToolCallDiff 两条轨迹间工具调用差异。
type ToolCallDiff struct {
	Index       int
	ToolName    string
	LeftResult  any
	RightResult any
	LeftError   string
	RightError  string
}

// LLMResponseDiff 两条轨迹间 LLM 响应差异。
type LLMResponseDiff struct {
	Index        int
	LeftContent  string
	RightContent string
}

// Diff 对比同一事件存储中的两条执行轨迹，
// 识别仅一侧存在的事件及两侧内容不一致的事件。
func Diff(ctx context.Context, left, right events.EventLog, leftTraceID, rightTraceID string) (*DiffResult, error) {
	result := &DiffResult{
		LeftTraceID:  leftTraceID,
		RightTraceID: rightTraceID,
		StateDiff:    make(map[string]StateDiff),
	}

	// 收集两侧轨迹的全部事件。
	leftEvents, err := readAllEvents(ctx, left, leftTraceID)
	if err != nil {
		return nil, err
	}
	rightEvents, err := readAllEvents(ctx, right, rightTraceID)
	if err != nil {
		return nil, err
	}

	// 按逻辑时钟构建查找表。
	leftByClock := make(map[uint64]*events.Event)
	for _, ev := range leftEvents {
		leftByClock[ev.Clock] = ev
	}
	rightByClock := make(map[uint64]*events.Event)
	for _, ev := range rightEvents {
		rightByClock[ev.Clock] = ev
	}

	// 收集全部 clock 值。
	allClocks := make(map[uint64]bool)
	for _, ev := range leftEvents {
		allClocks[ev.Clock] = true
	}
	for _, ev := range rightEvents {
		allClocks[ev.Clock] = true
	}

	// 逐 clock 对比事件。
	for clock := range allClocks {
		leftEv, leftOk := leftByClock[clock]
		rightEv, rightOk := rightByClock[clock]

		switch {
		case leftOk && !rightOk:
			result.MissingInRight = append(result.MissingInRight, leftEv)
		case !leftOk && rightOk:
			result.MissingInLeft = append(result.MissingInLeft, rightEv)
		case leftOk && rightOk:
			// 两侧均存在——比较类型与哈希。
			if leftEv.Type != rightEv.Type {
				result.Mismatched = append(result.Mismatched, EventMismatch{
					Clock:      clock,
					LeftEvent:  leftEv,
					RightEvent: rightEv,
					Field:      "type",
					LeftValue:  string(leftEv.Type),
					RightValue: string(rightEv.Type),
				})
			}
			if leftEv.Hash != rightEv.Hash {
				result.Mismatched = append(result.Mismatched, EventMismatch{
					Clock:      clock,
					LeftEvent:  leftEv,
					RightEvent: rightEv,
					Field:      "payload",
					LeftValue:  leftEv.Hash[:16],
					RightValue: rightEv.Hash[:16],
				})
			}

			// 按事件类型分类差异。
			switch leftEv.Type {
			case events.EventLLMCallEnd:
				result.LLMResponseDiff = append(result.LLMResponseDiff, LLMResponseDiff{
					Index:        len(result.LLMResponseDiff),
					LeftContent:  extractContent(leftEv),
					RightContent: extractContent(rightEv),
				})
			case events.EventToolCallResult:
				result.ToolCallDiff = append(result.ToolCallDiff, ToolCallDiff{
					Index:    len(result.ToolCallDiff),
					ToolName: extractToolName(leftEv),
				})
			case events.EventStateWrite:
				if leftEv.Node != "" {
					result.StateDiff[leftEv.Node] = StateDiff{
						Clock: clock,
						Key:   leftEv.Node,
					}
				}
			}
		}
	}

	return result, nil
}

// readAllEvents 从存储读取指定轨迹的全部事件。
func readAllEvents(ctx context.Context, store events.EventLog, traceID string) ([]*events.Event, error) {
	iter := store.Stream(ctx, events.EventFilter{TraceID: traceID})
	defer iter.Close()

	var result []*events.Event
	for {
		ev, ok := iter.Next(ctx)
		if !ok {
			break
		}
		result = append(result, ev)
	}
	return result, nil
}

// extractContent 从 LLMCallPayload 事件提取 Content。
func extractContent(ev *events.Event) string {
	if ev.Payload == nil {
		return ""
	}
	var payload events.LLMCallPayload
	if err := jsonUnmarshal(ev.Payload, &payload); err != nil {
		return ""
	}
	return payload.Content
}

// extractToolName 从 ToolCallPayload 事件提取 ToolName。
func extractToolName(ev *events.Event) string {
	if ev.Payload == nil {
		return ""
	}
	var payload events.ToolCallPayload
	if err := jsonUnmarshal(ev.Payload, &payload); err != nil {
		return ""
	}
	return payload.ToolName
}
