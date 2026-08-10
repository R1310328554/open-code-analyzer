package scheduler

// workerConn 封装调度器到单个 worker 的 wire.Peer，区分控制面与数据面连接并跟踪已分配任务。

import (
	"errors"
	"fmt"
	"maps"
	"slices"
	"sync"

	"github.com/grafana/loki/v3/pkg/engine/internal/scheduler/wire"
)

// connectionType represents the purpose of a worker's connection.
type connectionType int

const (
	// connectionTypeInitial represents a new connection from a worker with
	// unknown purpose.
	connectionTypeInitial connectionType = iota

	// connectionTypeControlPlane represents a connection from a worker where
	// the connection will be used to assign tasks and communicate state.
	//
	// The connection type is set to connectionTypeControlPlane when the worker
	// sends a [wire.WorkerHelloMessage].
	connectionTypeControlPlane

	// connectionTypeDataPlane represents a connection from a worker where
	// the connection will be used to communicate stream data.
	//
	// The connection type is set to connectionTypeDataPlane when the worker
	// sends a [wire.WorkerDataPlaneMessage].
	connectionTypeDataPlane
)

var connectionTypeNames = [...]string{
	connectionTypeInitial:      "initial",
	connectionTypeControlPlane: "control-plane",
	connectionTypeDataPlane:    "data-plane",
}

// String returns the string representation of the connection type.
func (t connectionType) String() string {
	if t < connectionTypeInitial || int(t) >= len(connectionTypeNames) {
		return fmt.Sprintf("connectionType(%d)", t)
	}
	name := connectionTypeNames[t]
	if name == "" {
		return fmt.Sprintf("connectionType(%d)", t)
	}
	return name
}

// A workerConn represents a connection to a worker.
// workerConn 内嵌 Peer，用 RWMutex 保护连接类型与 tasks 集合。
type workerConn struct {
	// Peer connection to the worker.
	*wire.Peer

	// mutex of the worker. Protects all fields.
	mut sync.RWMutex

	// ty represents the type of worker connection. Messages sent by the worker
	// that are incompatible with the connection type are rejected.
	ty connectionType

	// tasks hold the collection of tasks currently assigned to the worker.
	tasks map[*task]struct{}

	// done is closed when the worker connection is closed. It is used to signal
	// worker goroutines to exit.
	done chan struct{}
}

// Type returns the type of the worker connection.
func (wc *workerConn) Type() connectionType {
	wc.mut.RLock()
	defer wc.mut.RUnlock()

	return wc.ty
}

// HandleHello 仅在 initial 状态接受，成功后标记为 control-plane 连接。
// HandleHello handles a WorkerHelloMessage. Returns an error if the worker is
// not in a valid state for a HelloMessage, or if the message is invalid.
//
// After HandleHello is called, the worker connection is marked as a control
// plane connection.
func (wc *workerConn) HandleHello(msg wire.WorkerHelloMessage) error {
	wc.mut.Lock()
	defer wc.mut.Unlock()

	if got, want := wc.ty, connectionTypeInitial; got != want {
		return fmt.Errorf("worker connection must be in state %q, got %q", want, got)
	} else if msg.Threads <= 0 {
		return errors.New("worker must advertise at least one thread")
	}

	wc.ty = connectionTypeControlPlane
	return nil
}

// MarkReady 确认 worker 为控制面且可接收新任务分配。
// MarkReady marks the worker as ready to receive tasks. Returns an error if the
// worker is not a control plane connection, or if the worker is at full
// capacity.
func (wc *workerConn) MarkReady() error {
	wc.mut.Lock()
	defer wc.mut.Unlock()

	if got, want := wc.ty, connectionTypeControlPlane; got != want {
		return fmt.Errorf("worker connection must be in state %q, got %q", want, got)
	}
	return nil
}

// MarkDataPlane 将连接标记为 data-plane；控制面连接不可再发送流数据。
// MarkDataPlane marks the worker as a data plane connection. Returns an error
// if the worker is not in a valid state. MarkDataPlane is a no-op if the worker
// is already marked as a data plane connection.
func (wc *workerConn) MarkDataPlane() error {
	wc.mut.Lock()
	defer wc.mut.Unlock()

	switch wc.ty {
	case connectionTypeInitial:
		// Flag the connection as a data plane connection.
		wc.ty = connectionTypeDataPlane
	case connectionTypeControlPlane:
		return fmt.Errorf("workers in state %s can not send stream data messages", wc.ty)
	}

	return nil
}

// Assigned returns a copy of the assigned tasks in an undefined order.
func (wc *workerConn) Assigned() []*task {
	wc.mut.RLock()
	defer wc.mut.RUnlock()

	return slices.Collect(maps.Keys(wc.tasks))
}

// Assign 将 task 登记到 worker 并设置 owner 指针，便于回收与状态查询。
// Assign assigns a task to the worker.
func (wc *workerConn) Assign(assigned *task) {
	wc.mut.Lock()
	defer wc.mut.Unlock()

	assigned.owner = wc

	if wc.tasks == nil {
		wc.tasks = make(map[*task]struct{})
	}
	wc.tasks[assigned] = struct{}{}
}

// Unassign removes a task from the worker.
func (wc *workerConn) Unassign(assigned *task) {
	wc.mut.Lock()
	defer wc.mut.Unlock()

	delete(wc.tasks, assigned)
}
// Unassign 从 tasks 映射移除任务，Assigned 返回当前分配任务的副本切片。
