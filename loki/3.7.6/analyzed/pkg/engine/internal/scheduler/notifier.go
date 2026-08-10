package scheduler

// notifier 延迟批量调用 workflow 事件处理器，避免在持锁路径内同步回调导致死锁。

import (
	"context"

	"github.com/grafana/loki/v3/pkg/engine/internal/workflow"
)

// streamNotification 缓存流状态变更及对应 StreamEventHandler，Notify 时统一派发。
// streamNotification is a deferred call to a stream event handler.
type streamNotification struct {
	Handler  workflow.StreamEventHandler
	Stream   *workflow.Stream
	NewState workflow.StreamState
}

// taskNotification is a deferred call to a task event handler.
type taskNotification struct {
	Handler   workflow.TaskEventHandler
	Task      *workflow.Task
	NewStatus workflow.TaskStatus
}

// notifier 先缓冲再 Notify，使 scheduler 可在释放 resourcesMut 后安全触发上层逻辑。
// A notifier is responsible for invoking [workflow.StreamEventHandler] and
// [workflow.TaskEventHandler].
//
// Notifier is used to avoid deadlocks so notifications can be held without any
// mutexes held.
type notifier struct {
	streamNotifications []streamNotification
	taskNotifications   []taskNotification
}

// AddStreamEvent buffers a stream event notification.
func (n *notifier) AddStreamEvent(notification streamNotification) {
	n.streamNotifications = append(n.streamNotifications, notification)
}

// AddTaskEvent buffers a task event notification.
func (n *notifier) AddTaskEvent(notification taskNotification) {
	n.taskNotifications = append(n.taskNotifications, notification)
}

// Notify 按追加顺序依次处理流与任务通知，不保证跨类型之间的优先级。
// Notify handles all pending notifications.
func (n *notifier) Notify(ctx context.Context) {
	for _, ev := range n.streamNotifications {
		ev.Handler(ctx, ev.Stream, ev.NewState)
	}

	for _, ev := range n.taskNotifications {
		ev.Handler(ctx, ev.Task, ev.NewStatus)
	}
}
// AddStreamEvent/AddTaskEvent 仅追加切片，不做去重。
