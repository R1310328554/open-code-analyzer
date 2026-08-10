package workflow

// admissionControl 按任务类型（scan/other）划分准入车道，使用加权信号量限制工作流内并发 scan 与非 scan 任务数量。

import (
	"math"

	"golang.org/x/sync/semaphore"

	"github.com/grafana/loki/v3/pkg/engine/internal/planner/physical"
)

type taskType string

const (
	taskTypeScan  taskType = "scan"
	taskTypeOther taskType = "other"
)

// admissionLane 包装 semaphore.Weighted，记录车道类型与容量上限。
type admissionLane struct {
	*semaphore.Weighted
	capacity int64
	lane     taskType
}

func newAdmissionLane(lane taskType, capacity int64) *admissionLane {
	return &admissionLane{
		Weighted: semaphore.NewWeighted(capacity),
		capacity: capacity,
		lane:     lane,
	}
}

// admissionControl 维护 taskType 到 admissionLane 的映射表。
// admissionControl is a control structure to lookup "admission lanes" for different types of tasks.
// It is a lightweight wrapper around a mapping of task type to admission lane.
type admissionControl struct {
	mapping map[taskType]*admissionLane
}

// newAdmissionControl 将小于 1 的上限视为无限制（MaxInt64）。
func newAdmissionControl(maxScanTasks, maxOtherTasks int64) *admissionControl {
	if maxScanTasks < 1 {
		maxScanTasks = math.MaxInt64
	}
	if maxOtherTasks < 1 {
		maxOtherTasks = math.MaxInt64
	}

	return &admissionControl{
		mapping: map[taskType]*admissionLane{
			taskTypeScan:  newAdmissionLane(taskTypeScan, maxScanTasks),
			taskTypeOther: newAdmissionLane(taskTypeOther, maxOtherTasks),
		},
	}
}

// groupByType 将任务切片按 isScanTask 结果分到 scan/other 两组。
// groupByBucket categorizes a slice of tasks into groups based on their characteristics (scan, other, ...).
func (ac *admissionControl) groupByType(tasks []*Task) map[taskType][]*Task {
	groups := map[taskType][]*Task{
		taskTypeScan:  make([]*Task, 0, len(tasks)),
		taskTypeOther: make([]*Task, 0, len(tasks)),
	}

	for _, t := range tasks {
		ty := ac.typeFor(t)
		groups[ty] = append(groups[ty], t)
	}

	return groups
}

// typeFor 根据计划片段是否含扫描节点决定车道类型。
func (ac *admissionControl) typeFor(task *Task) taskType {
	if isScanTask(task) {
		return taskTypeScan
	}
	return taskTypeOther
}

func (ac *admissionControl) laneFor(task *Task) *admissionLane {
	return ac.mapping[ac.typeFor(task)]
}

func (ac *admissionControl) get(ty taskType) *admissionLane {
	return ac.mapping[ty]
}

// isScanTask 遍历 Fragment 图节点，存在 DataObjScan 或 PointersScan 即为 scan。
func isScanTask(task *Task) bool {
	for node := range task.Fragment.Graph().Nodes() {
		if node.Type() == physical.NodeTypeDataObjScan || node.Type() == physical.NodeTypePointersScan {
			return true
		}
	}
	return false
}
// Workflow.dispatchTasks 按车道 Acquire 后批量 Start 任务，完成时 Release 令牌。
