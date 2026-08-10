package scheduler

// task 包装 workflow.Task，跟踪排队/分配/执行时间戳、公平队列 scope 与 worker 归属。

import (
	"context"
	"fmt"
	"net/http"
	"slices"
	"time"

	"github.com/grafana/loki/v3/pkg/engine/internal/util/queue/fair"
	"github.com/grafana/loki/v3/pkg/engine/internal/workflow"
	"github.com/grafana/loki/v3/pkg/xcap"
)

// task 的 metadata 承载 trace 与 HTTP 头，owner 指向当前执行该任务的 workerConn。
// task wraps a [workflow.Task] with its handler.
type task struct {
	createTime time.Time // Time when task was created.
	assignTime time.Time // Time when task was assigned to a worker.
	queueTime  time.Time // Time when task was enqueued.

	inner   *workflow.Task
	handler workflow.TaskEventHandler
	scope   fair.Scope // Queue scope this task belongs to.

	// metadata holds additional metadata associated with the task.
	// This can be used to stortracing and other information that
	// should be propagated to workers.
	metadata http.Header

	owner  *workerConn
	status workflow.TaskStatus

	// wfRegion is the region associated with the parent workflow of this task.
	wfRegion        *xcap.Region
	runtimeTraceCtx context.Context
}

// validTaskTransitions 定义任务状态机：CREATED 可直达 RUNNING，终态不可再迁移。
var validTaskTransitions = map[workflow.TaskState][]workflow.TaskState{
	workflow.TaskStateCreated: {workflow.TaskStatePending, workflow.TaskStateRunning, workflow.TaskStateCancelled},
	workflow.TaskStatePending: {workflow.TaskStateRunning, workflow.TaskStateCancelled, workflow.TaskStateFailed},
	workflow.TaskStateRunning: {workflow.TaskStateCompleted, workflow.TaskStateCancelled, workflow.TaskStateFailed},

	workflow.TaskStateCompleted: {}, // Terminal state, can't transition
	workflow.TaskStateCancelled: {}, // Terminal state, can't transition
	workflow.TaskStateFailed:    {}, // Terminal state, can't transition
}

// setState 允许同状态但 payload 变化（如 Capture 更新），并更新 tasksTotal 计数。
// setState updates the state of the task. setState returns an error if the
// transition is invalid.
//
// Returns true if the state was updated, false otherwise (such as if the task
// is already in the desired state).
func (t *task) setState(m *metrics, newStatus workflow.TaskStatus) (bool, error) {
	oldState, newState := t.status.State, newStatus.State

	switch {
	case newStatus != t.status && newState == oldState:
		// State is the same (so we don't have to validate transitions), but
		// there's a new payload about the status, so we should store it.
		t.status = newStatus
		return true, nil

	case newState == oldState:
		// Status is the exact same, no need to update.
		return false, nil

	default:
		validStates := validTaskTransitions[oldState]
		if !slices.Contains(validStates, newState) {
			return false, fmt.Errorf("invalid state transition from %s to %s", oldState, newState)
		}

		t.status = newStatus
		m.tasksTotal.WithLabelValues(newState.String()).Inc()
		return true, nil
	}

}
// wfRegion 用于记录 xcap 队列与分配尾延迟统计。
