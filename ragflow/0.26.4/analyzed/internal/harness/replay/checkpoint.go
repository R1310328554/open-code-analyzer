// checkpoint.go — 从事件序列重建 Fork 检查点。
package replay

import (
	"encoding/json"
	"fmt"
	"time"

	"ragflow/internal/harness/events"
	"ragflow/internal/harness/graph/constants"
)

// BuildCheckpoint 从 Fork 点之前的事件序列重建 flat map[string]any 检查点，
// 使 Pregel 引擎可从该状态恢复执行，如同原运行中已保存检查点。
//
// 返回 map 包含：
//   - EventStateWrite 提取的通道值
//   - EventNodeEnd 汇总的 __completed_tasks__（NUL 分隔）
//   - 最后超步事件的 __step__
//   - JSON 序列化的 __last_state__
//   - 最后节点事件的 __last_completed_node__
//   - checkpoint_id 元数据
//
// 第二返回值是重建的 checkpoint_id。
func BuildCheckpoint(originalEvents []*events.Event, threadID string) (map[string]any, string) {
	cp := make(map[string]any)
	cp[constants.ConfigKeyThreadID] = threadID

	checkpointID := fmt.Sprintf("fork-cp-%s-%d", threadID, time.Now().UnixNano())
	cp[constants.ConfigKeyCheckpointID] = checkpointID
	cp["__pregel_checkpoint_id"] = checkpointID

	var completedTasks []string
	var lastCompletedNode string
	var lastStep int

	// 从状态写入收集通道值，追踪已完成节点。
	for _, ev := range originalEvents {
		switch ev.Type {
		case events.EventStateWrite:
			var st events.StateTransitionPayload
			if ev.Payload != nil {
				_ = json.Unmarshal(ev.Payload, &st)
			}
			if st.Channel != "" {
				cp[st.Channel] = st.NewValue
			}

		case events.EventNodeEnd:
			completedTasks = append(completedTasks, ev.Node)
			lastCompletedNode = ev.Node

		case events.EventStepEnd:
			if ev.Step > lastStep {
				lastStep = ev.Step
			}
		}
	}

	// 若有状态写入，将累积 map 序列化为 last_state。
	if len(cp) > 2 { // 除 thread_id 与 checkpoint_id 外还有通道值
		lastState := make(map[string]any)
		for k, v := range cp {
			if k != constants.ConfigKeyThreadID && k != constants.ConfigKeyCheckpointID && k != "__pregel_checkpoint_id" {
				lastState[k] = v
			}
		}
		if ls, err := json.Marshal(lastState); err == nil {
			cp["__last_state__"] = string(ls)
		}
	}

	// 将已完成任务序列化为 NUL 分隔字符串。
	if len(completedTasks) > 0 {
		var sb []byte
		for i, task := range completedTasks {
			if i > 0 {
				sb = append(sb, 0) // NUL 分隔符
			}
			sb = append(sb, task...)
		}
		cp["__completed_tasks__"] = string(sb)
	}

	if lastCompletedNode != "" {
		cp["__last_completed_node__"] = lastCompletedNode
	}

	cp["__step__"] = float64(lastStep)

	return cp, checkpointID
}
