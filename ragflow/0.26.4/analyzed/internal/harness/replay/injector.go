package replay

import (
	"encoding/json"
	"fmt"

	"ragflow/internal/harness/events"
)

// ---- 通用覆盖策略 ----

// ReplayExactTools 返回使用录制结果不变的工具覆盖函数，为确定性重放默认行为。
func ReplayExactTools() ToolOverrideFunc {
	return func(toolName string, args map[string]any, recordedResult any) (any, error) {
		return recordedResult, nil
	}
}

// ReplayLiveTools 返回始终 nil 的工具覆盖函数，指示重放使用真实工具实现。
func ReplayLiveTools() ToolOverrideFunc {
	return func(toolName string, args map[string]any, recordedResult any) (any, error) {
		// 返回 nil 表示「实时执行」。
		return nil, nil
	}
}

// ReplaySubstituteModel 用固定字符串替换录制的 LLM 响应，
// 在冻结工具结果时对比不同模型的行为变化。
//
// 回调接收原始录制响应并返回替换响应；返回空串可抑制响应。
type ReplayModelCallback func(recordedResponse string) string

// ReplaySubstituteModel 从回调创建 ModelOverrideFunc。
func ReplaySubstituteModel(fn ReplayModelCallback) ModelOverrideFunc {
	return func(_ []any, recordedResponse string) (*string, error) {
		substituted := fn(recordedResponse)
		return &substituted, nil
	}
}

// ---- 错误类型 ----

type replayError struct {
	msg string
}

func (e *replayError) Error() string { return e.msg }

func errorf(format string, args ...any) error {
	return &replayError{msg: fmt.Sprintf(format, args...)}
}

// ---- 辅助函数 ----

func jsonUnmarshal(data []byte, target any) error {
	return json.Unmarshal(data, target)
}

func jsonMarshal(v any) ([]byte, error) {
	return json.Marshal(v)
}

// copyEvent 浅拷贝 Event，深拷贝 Payload 与 Metadata。
func copyEvent(ev *events.Event) *events.Event {
	cp := *ev
	if ev.Payload != nil {
		cp.Payload = make([]byte, len(ev.Payload))
		copy(cp.Payload, ev.Payload)
	}
	if ev.Metadata != nil {
		cp.Metadata = make(map[string]any, len(ev.Metadata))
		for k, v := range ev.Metadata {
			cp.Metadata[k] = v
		}
	}
	if ev.CausedBy != nil {
		cp.CausedBy = make([]events.EventID, len(ev.CausedBy))
		copy(cp.CausedBy, ev.CausedBy)
	}
	return &cp
}

// ---- 测试断言用事件辅助 ----

// FindEventsOfType 按类型过滤事件。
func FindEventsOfType(evts []*events.Event, typ events.EventType) []*events.Event {
	var result []*events.Event
	for _, ev := range evts {
		if ev.Type == typ {
			result = append(result, ev)
		}
	}
	return result
}

// EventsContains 检查是否存在指定类型事件。
func EventsContains(evts []*events.Event, typ events.EventType) bool {
	for _, ev := range evts {
		if ev.Type == typ {
			return true
		}
	}
	return false
}

// EventCount 统计指定类型事件数量。
func EventCount(evts []*events.Event, typ events.EventType) int {
	count := 0
	for _, ev := range evts {
		if ev.Type == typ {
			count++
		}
	}
	return count
}
