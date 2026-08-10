package loki

// delete_store_listener 注册 dskit 服务生命周期钩子，在 Loki 停止或失败时关闭 DeleteRequestsClient 连接。

import (
	"github.com/grafana/dskit/services"

	"github.com/grafana/loki/v3/pkg/compactor/deletion"
)

func deleteRequestsStoreListener(d deletion.DeleteRequestsClient) *listener {
	return &listener{d}
}

// listener 持有 DeleteRequestsClient，在 Stopping/Terminated/Failed 时调用 Stop。
type listener struct {
	deleteRequestsClient deletion.DeleteRequestsClient
}

// Starting 在服务进入 STARTING 状态时调用，此处无需额外初始化。
// Starting is called when the service transitions from NEW to STARTING.
func (l *listener) Starting() {}

// Running is called when the service transitions from STARTING to RUNNING.
func (l *listener) Running() {}

// Stopping 在优雅停机路径上停止删除客户端，避免重复 Stop 已终止状态。
// Stopping is called when the service transitions to the STOPPING state.
func (l *listener) Stopping(from services.State) {
	if from == services.Stopping || from == services.Terminated || from == services.Failed {
		// no need to do anything
		return
	}
	l.deleteRequestsClient.Stop()
}

// Terminated 处理非正常路径终止，同样确保 delete client 已关闭。
// Terminated is called when the service transitions to the TERMINATED state.
func (l *listener) Terminated(from services.State) {
	if from == services.Stopping || from == services.Terminated || from == services.Failed {
		// no need to do anything
		return
	}
	l.deleteRequestsClient.Stop()
}

// Failed 在服务失败时清理删除客户端资源，防止 goroutine 泄漏。
// Failed is called when the service transitions to the FAILED state.
func (l *listener) Failed(from services.State, _ error) {
	if from == services.Stopping || from == services.Terminated || from == services.Failed {
		// no need to do anything
		return
	}
	l.deleteRequestsClient.Stop()
}
// 自 Stopping/Terminated/Failed 进入时跳过 Stop，防止对已关闭客户端重复调用。
