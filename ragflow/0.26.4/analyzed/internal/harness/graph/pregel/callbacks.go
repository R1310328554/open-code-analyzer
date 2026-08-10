// Package pregel 提供图执行生命周期回调。
//
// 回调贯穿 Pregel 全流程：运行起止、超步、节点、检查点、中断/恢复，
// 用于埋点、日志、事件记录与自定义扩展。
package pregel

import (
	"context"
	"sync"
)

// ---- 回调接口类型 ----

// RunCallback 图完整运行开始/结束时触发。
type RunCallback interface {
	// OnRunStart is called when a graph run begins.
	OnRunStart(ctx context.Context, graphName string, threadID string)
	// OnRunEnd is called when a graph run completes (or errors).
	OnRunEnd(ctx context.Context, graphName string, threadID string, err error)
}

// StepCallback 每个 Pregel 超步开始/结束时触发。
type StepCallback interface {
	// OnStepStart is called before a superstep begins.
	OnStepStart(ctx context.Context, step int, taskCount int)
	// OnStepEnd is called after a superstep completes.
	OnStepEnd(ctx context.Context, step int, err error)
}

// NodeCallback 每个节点执行前/后触发。
type NodeCallback interface {
	// OnNodeStart is called before a node executes.
	OnNodeStart(ctx context.Context, nodeName string, step int)
	// OnNodeEnd is called after a node completes.
	OnNodeEnd(ctx context.Context, nodeName string, step int, output interface{}, err error)
}

// CheckpointCallback 检查点保存/加载/手动更新时触发。
type CheckpointCallback interface {
	// OnCheckpointSave is called after a checkpoint is saved.
	OnCheckpointSave(ctx context.Context, threadID, checkpointID string, step int)
	// OnCheckpointLoad is called after a checkpoint is loaded.
	OnCheckpointLoad(ctx context.Context, threadID, checkpointID string, step int)
	// OnCheckpointUpdate is called when state is manually updated (UpdateState).
	OnCheckpointUpdate(ctx context.Context, threadID string, asNode string)
}

// InterruptCallback 图中断或恢复时触发。
type InterruptCallback interface {
	// OnInterrupt is called when the graph is interrupted.
	OnInterrupt(ctx context.Context, nodeNames []string, step int)
	// OnResume is called when the graph resumes from an interrupt.
	OnResume(ctx context.Context, threadID string)
}

// GraphCallback 聚合全部回调接口，便于一次注册。
type GraphCallback interface {
	RunCallback
	StepCallback
	NodeCallback
	CheckpointCallback
	InterruptCallback
}

// ---- 回调管理器 ----

// CallbackManager 管理多组回调，并发安全；按类型分派到注册的观察者。
type CallbackManager struct {
	mu                  sync.RWMutex
	runCallbacks        []RunCallback
	stepCallbacks       []StepCallback
	nodeCallbacks       []NodeCallback
	checkpointCallbacks []CheckpointCallback
	interruptCallbacks  []InterruptCallback
}

// NewCallbackManager 创建空回调管理器。
func NewCallbackManager() *CallbackManager {
	return &CallbackManager{}
}

// AddRunCallback 注册运行级回调。
func (m *CallbackManager) AddRunCallback(cb RunCallback) {
	m.mu.Lock()
	defer m.mu.Unlock()
	m.runCallbacks = append(m.runCallbacks, cb)
}

// AddStepCallback 注册超步级回调。
func (m *CallbackManager) AddStepCallback(cb StepCallback) {
	m.mu.Lock()
	defer m.mu.Unlock()
	m.stepCallbacks = append(m.stepCallbacks, cb)
}

// AddNodeCallback 注册节点级回调。
func (m *CallbackManager) AddNodeCallback(cb NodeCallback) {
	m.mu.Lock()
	defer m.mu.Unlock()
	m.nodeCallbacks = append(m.nodeCallbacks, cb)
}

// AddCheckpointCallback 注册检查点回调。
func (m *CallbackManager) AddCheckpointCallback(cb CheckpointCallback) {
	m.mu.Lock()
	defer m.mu.Unlock()
	m.checkpointCallbacks = append(m.checkpointCallbacks, cb)
}

// AddInterruptCallback 注册中断/恢复回调。
func (m *CallbackManager) AddInterruptCallback(cb InterruptCallback) {
	m.mu.Lock()
	defer m.mu.Unlock()
	m.interruptCallbacks = append(m.interruptCallbacks, cb)
}

// AddCallback 注册实现 GraphCallback 的全量回调。
func (m *CallbackManager) AddCallback(cb GraphCallback) {
	m.mu.Lock()
	defer m.mu.Unlock()
	m.runCallbacks = append(m.runCallbacks, cb)
	m.stepCallbacks = append(m.stepCallbacks, cb)
	m.nodeCallbacks = append(m.nodeCallbacks, cb)
	m.checkpointCallbacks = append(m.checkpointCallbacks, cb)
	m.interruptCallbacks = append(m.interruptCallbacks, cb)
}

// ---- 分派方法 ----

// RunStart 分派 OnRunStart 到所有运行回调。
func (m *CallbackManager) RunStart(ctx context.Context, graphName, threadID string) {
	m.mu.RLock()
	cbs := m.runCallbacks
	m.mu.RUnlock()
	for _, cb := range cbs {
		cb.OnRunStart(ctx, graphName, threadID)
	}
}

// RunEnd 分派 OnRunEnd 到所有运行回调。
func (m *CallbackManager) RunEnd(ctx context.Context, graphName, threadID string, err error) {
	m.mu.RLock()
	cbs := m.runCallbacks
	m.mu.RUnlock()
	for _, cb := range cbs {
		cb.OnRunEnd(ctx, graphName, threadID, err)
	}
}

// StepStart 分派 OnStepStart 到所有超步回调。
func (m *CallbackManager) StepStart(ctx context.Context, step, taskCount int) {
	m.mu.RLock()
	cbs := m.stepCallbacks
	m.mu.RUnlock()
	for _, cb := range cbs {
		cb.OnStepStart(ctx, step, taskCount)
	}
}

// StepEnd 分派 OnStepEnd 到所有超步回调。
func (m *CallbackManager) StepEnd(ctx context.Context, step int, err error) {
	m.mu.RLock()
	cbs := m.stepCallbacks
	m.mu.RUnlock()
	for _, cb := range cbs {
		cb.OnStepEnd(ctx, step, err)
	}
}

// NodeStart 分派 OnNodeStart 到所有节点回调。
func (m *CallbackManager) NodeStart(ctx context.Context, nodeName string, step int) {
	m.mu.RLock()
	cbs := m.nodeCallbacks
	m.mu.RUnlock()
	for _, cb := range cbs {
		cb.OnNodeStart(ctx, nodeName, step)
	}
}

// NodeEnd 分派 OnNodeEnd 到所有节点回调。
func (m *CallbackManager) NodeEnd(ctx context.Context, nodeName string, step int, output interface{}, err error) {
	m.mu.RLock()
	cbs := m.nodeCallbacks
	m.mu.RUnlock()
	for _, cb := range cbs {
		cb.OnNodeEnd(ctx, nodeName, step, output, err)
	}
}

// CheckpointSave 分派 OnCheckpointSave。
func (m *CallbackManager) CheckpointSave(ctx context.Context, threadID, checkpointID string, step int) {
	m.mu.RLock()
	cbs := m.checkpointCallbacks
	m.mu.RUnlock()
	for _, cb := range cbs {
		cb.OnCheckpointSave(ctx, threadID, checkpointID, step)
	}
}

// CheckpointLoad 分派 OnCheckpointLoad。
func (m *CallbackManager) CheckpointLoad(ctx context.Context, threadID, checkpointID string, step int) {
	m.mu.RLock()
	cbs := m.checkpointCallbacks
	m.mu.RUnlock()
	for _, cb := range cbs {
		cb.OnCheckpointLoad(ctx, threadID, checkpointID, step)
	}
}

// CheckpointUpdate 分派 OnCheckpointUpdate（UpdateState）。
func (m *CallbackManager) CheckpointUpdate(ctx context.Context, threadID, asNode string) {
	m.mu.RLock()
	cbs := m.checkpointCallbacks
	m.mu.RUnlock()
	for _, cb := range cbs {
		cb.OnCheckpointUpdate(ctx, threadID, asNode)
	}
}

// Interrupt 分派 OnInterrupt。
func (m *CallbackManager) Interrupt(ctx context.Context, nodeNames []string, step int) {
	m.mu.RLock()
	cbs := m.interruptCallbacks
	m.mu.RUnlock()
	for _, cb := range cbs {
		cb.OnInterrupt(ctx, nodeNames, step)
	}
}

// Resume 分派 OnResume。
func (m *CallbackManager) Resume(ctx context.Context, threadID string) {
	m.mu.RLock()
	cbs := m.interruptCallbacks
	m.mu.RUnlock()
	for _, cb := range cbs {
		cb.OnResume(ctx, threadID)
	}
}

// ---- NoopCallback 空实现占位 ----

// NoopCallback 实现 GraphCallback 全部方法为空操作。
type NoopCallback struct{}

func (NoopCallback) OnRunStart(_ context.Context, _, _ string)                            {}
func (NoopCallback) OnRunEnd(_ context.Context, _, _ string, _ error)                     {}
func (NoopCallback) OnStepStart(_ context.Context, _ int, _ int)                          {}
func (NoopCallback) OnStepEnd(_ context.Context, _ int, _ error)                          {}
func (NoopCallback) OnNodeStart(_ context.Context, _ string, _ int)                       {}
func (NoopCallback) OnNodeEnd(_ context.Context, _ string, _ int, _ interface{}, _ error) {}
func (NoopCallback) OnCheckpointSave(_ context.Context, _, _ string, _ int)               {}
func (NoopCallback) OnCheckpointLoad(_ context.Context, _, _ string, _ int)               {}
func (NoopCallback) OnCheckpointUpdate(_ context.Context, _, _ string)                    {}
func (NoopCallback) OnInterrupt(_ context.Context, _ []string, _ int)                     {}
func (NoopCallback) OnResume(_ context.Context, _ string)                                 {}

// 编译期接口检查。
var (
	_ RunCallback        = NoopCallback{}
	_ StepCallback       = NoopCallback{}
	_ NodeCallback       = NoopCallback{}
	_ CheckpointCallback = NoopCallback{}
	_ InterruptCallback  = NoopCallback{}
	_ GraphCallback      = NoopCallback{}
)
