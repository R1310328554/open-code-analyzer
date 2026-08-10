package workflow

// Task 与 Stream 是工作流的基本单元：Task 为物理计划分区，Stream 描述跨 Task 边界的 Arrow 数据流（单发送方、单接收方）。

import (
	"github.com/oklog/ulid/v2"

	"github.com/grafana/loki/v3/pkg/engine/internal/planner/physical"
)

// Task 含 ULID、租户、Fragment 计划及按物理节点索引的 Sources/Sinks 映射。
// A Task is a single unit of work within a workflow. Each Task is a partition
// of a local physical plan.
type Task struct {
	// ULID 在调度与 wire 消息中唯一标识任务实例。
// ULID is a unique identifier of the Task.
	ULID ulid.ULID

	// TenantID is a tenant associated with this task.
	TenantID string

	// Fragment is the local physical plan that this Task represents.
	Fragment *physical.Plan

	// Sources defines which Streams physical nodes read from. Sources are only
	// defined for nodes in the Fragment which read data across task boundaries.
	Sources map[physical.Node][]*Stream

	// Sinks defines which Streams physical nodes write to. Sinks are only
	// defined for nodes in the Fragment which write data across task boundaries.
	Sinks map[physical.Node][]*Stream

	// The maximum boundary of timestamps that the task can possibly emit.
	// Does not account for predicates.
	// MaxTimeRange 为元数据，供工作流短路逻辑判断子任务时间范围是否仍相交。
// MaxTimeRange is not read when executing a task fragment. It can be used
	// as metadata to control execution (such as cancelling ongoing tasks based
	// on their maximum time range).
	MaxTimeRange physical.TimeRange
}

// ID 返回任务 ULID，满足 dag.Node 等接口需求。
// ID returns the Task's ULID.
func (t *Task) ID() ulid.ULID { return t.ULID }

// Stream 仅含 ULID 与 TenantID；发送方与接收方由 Task.Sinks/Sources 图结构隐含。
// A Stream is an abstract representation of how data flows across Task
// boundaries. Each Stream has exactly one sender (a Task), and one receiver
// (either another Task or the owning [Workflow]).
type Stream struct {
	// ULID is a unique identifier of the Stream.
	ULID ulid.ULID

	// TenantID is a tenant associated with this stream.
	TenantID string
}
// Sources/Sinks 键为 physical.Node，值为该节点关联的 Stream 列表。
