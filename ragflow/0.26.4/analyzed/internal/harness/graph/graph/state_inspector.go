// state_inspector.go — 图状态检视 API：get_state/update_state/history/fork。

// Package graph 提供已编译图的状态检视与手动注入 API。
//
// This corresponds to Python LangGraph's get_state() / update_state() /
// get_state_history() on PregelProtocol.
package graph

import (
	"context"
	"fmt"
	"time"

	"ragflow/internal/harness/graph/channels"
	"ragflow/internal/harness/graph/checkpoint"
	"ragflow/internal/harness/graph/constants"
	"ragflow/internal/harness/graph/types"
)

// StateSnapshot 某检查点处的图状态快照（对标 Python StateSnapshot）。
// This mirrors Python's langgraph.types.StateSnapshot.
type StateSnapshot struct {
	// Values 各通道当前值（即图状态）
	Values map[string]interface{} `json:"values"`
	// Next 下一步待执行节点名
	Next []string `json:"next,omitempty"`
	// Config is the RunnableConfig used to fetch this snapshot.
	Config *types.RunnableConfig `json:"config"`
	// Metadata associated with this snapshot.
	Metadata map[string]interface{} `json:"metadata,omitempty"`
	// CreatedAt is the timestamp of snapshot creation.
	CreatedAt time.Time `json:"created_at"`
	// ParentConfig is the config that can fetch the parent snapshot, if any.
	ParentConfig *types.RunnableConfig `json:"parent_config,omitempty"`
	// Tasks are the pending tasks at this snapshot.
	Tasks []Task `json:"tasks,omitempty"`
	// Interrupts that were pending at this checkpoint.
	Interrupts []*types.Interrupt `json:"interrupts,omitempty"`
}

// StateUpdate 手动状态更新描述（对标 Python StateUpdate）。
// This mirrors Python's StateUpdate tuple.
type StateUpdate struct {
	Values   map[string]interface{} // state values to write
	AsNode   string                 // node name to write as (empty = "resume")
	CheckID  string                 // checkpoint ID to target (empty = latest)
	ThreadID string                 // thread ID to target
}

// StateInspector 状态检视接口，compiledGraph 与 CSG 均实现。
// Implemented by compiledGraph and CompiledStateGraph.
type StateInspector interface {
	// GetState 按 thread_id/checkpoint_id 获取状态快照。
	// When config contains only thread_id, returns the latest state.
	// When config also contains checkpoint_id, returns that specific state.
	GetState(ctx context.Context, config *types.RunnableConfig) (*StateSnapshot, error)

	// GetStateHistory 倒序返回检查点历史快照列表。
	// starting from the most recent and going backward.
	GetStateHistory(ctx context.Context, config *types.RunnableConfig, limit int, before *types.RunnableConfig) ([]*StateSnapshot, error)

	// UpdateState 手动注入状态并创建新检查点（时光旅行/中断解决）。
	// This enables manual state injection (time travel, interrupt resolution).
	// Returns the config for the new checkpoint created by the update.
	UpdateState(ctx context.Context, config *types.RunnableConfig, update *StateUpdate) (*types.RunnableConfig, error)

	// ForkThread 将源线程检查点克隆到新线程（时间旅行分叉）。
	// sourceCheckpointID: empty = latest checkpoint in source thread.
	ForkThread(ctx context.Context, sourceThreadID, newThreadID string, sourceCheckpointID string) (*types.RunnableConfig, error)
}

// Ensure compiledGraph implements StateInspector.
var _ StateInspector = (*compiledGraph)(nil)

// GetState retrieves the graph state at the given configuration point.
func (cg *compiledGraph) GetState(ctx context.Context, config *types.RunnableConfig) (*StateSnapshot, error) {
	if cg.checkpointer == nil {
		return nil, fmt.Errorf("checkpointer is required for GetState, configure with WithCheckpointer during Compile")
	}

	cp, err := cg.getCheckpointer()
	if err != nil {
		return nil, err
	}
	cpConfig := buildCheckpointerConfig(config)
	cpData, err := cp.Get(ctx, cpConfig)
	if err != nil {
		return nil, fmt.Errorf("failed to get checkpoint: %w", err)
	}
	if cpData == nil {
		return nil, nil
	}

	// Build channel registry from graph channels and restore from checkpoint data.
	registry := channels.NewRegistry()
	for name, ch := range cg.graph.GetChannels() {
		if chImpl, ok := ch.(channels.Channel); ok {
			registry.Register(name, chImpl.Copy())
		}
	}
	filtered := make(map[string]interface{})
	for key, val := range cpData {
		if _, ok := registry.Get(key); ok {
			filtered[key] = val
		}
	}
	if len(filtered) > 0 {
		if err := registry.RestoreFromCheckpoint(filtered); err != nil {
			return nil, fmt.Errorf("failed to restore from checkpoint: %w", err)
		}
	}

	// Build current values.
	values, _ := registry.GetValues()

	// Determine next tasks.
	nextNodes := cg.determineNextFromCheckpoint(cpData)

	return &StateSnapshot{
		Values:    values,
		Next:      nextNodes,
		Config:    config,
		Metadata:  extractMeta(cpData),
		CreatedAt: time.Now(),
	}, nil
}

// GetStateHistory returns the sequence of state snapshots for the thread.
func (cg *compiledGraph) GetStateHistory(ctx context.Context, config *types.RunnableConfig, limit int, before *types.RunnableConfig) ([]*StateSnapshot, error) {
	if cg.checkpointer == nil {
		return nil, fmt.Errorf("checkpointer is required for GetStateHistory")
	}

	cp, err := cg.getCheckpointer()
	if err != nil {
		return nil, err
	}
	cpConfig := buildCheckpointerConfig(config)
	entries, err := cp.List(ctx, cpConfig, limit)
	if err != nil {
		return nil, fmt.Errorf("failed to list checkpoints: %w", err)
	}

	snapshots := make([]*StateSnapshot, 0, len(entries))
	for _, entry := range entries {
		// Build a config that points to this specific checkpoint.
		cpID, _ := entry[constants.ConfigKeyCheckpointID].(string)
		snapConfig := &types.RunnableConfig{}
		if config != nil {
			snapConfig = &types.RunnableConfig{}
			if config.Configurable != nil {
				snapConfig.Configurable = make(map[string]interface{}, len(config.Configurable))
				for k, v := range config.Configurable {
					snapConfig.Configurable[k] = v
				}
			}
		}
		if snapConfig.Configurable == nil {
			snapConfig.Configurable = make(map[string]interface{})
		}
		if cpID != "" {
			snapConfig.Configurable[constants.ConfigKeyCheckpointID] = cpID
		}

		// Get full state for this checkpoint.
		snap, err := cg.GetState(ctx, snapConfig)
		if err != nil {
			// Skip entries we can't parse.
			continue
		}
		if snap != nil {
			if createdAt, ok := entry["created_at"].(time.Time); ok {
				snap.CreatedAt = createdAt
			}
			if meta, ok := entry["metadata"].(map[string]interface{}); ok {
				snap.Metadata = meta
			}
			snapshots = append(snapshots, snap)
		}
	}
	return snapshots, nil
}

// UpdateState applies state updates at the given checkpoint/thread and creates a new checkpoint.
func (cg *compiledGraph) UpdateState(ctx context.Context, config *types.RunnableConfig, update *StateUpdate) (*types.RunnableConfig, error) {
	if cg.checkpointer == nil {
		return nil, fmt.Errorf("checkpointer is required for UpdateState")
	}

	cp, err := cg.getCheckpointer()
	if err != nil {
		return nil, err
	}
	// 1. Get the current checkpoint at the target config.
	cpConfig := buildCheckpointerConfig(config)
	if update.CheckID != "" {
		cpConfig[constants.ConfigKeyCheckpointID] = update.CheckID
	}
	cpData, err := cp.Get(ctx, cpConfig)
	if err != nil {
		return nil, fmt.Errorf("failed to get checkpoint for update: %w", err)
	}
	if cpData == nil {
		return nil, fmt.Errorf("no checkpoint found for the given config")
	}

	// 2. Apply the update values to the checkpoint data.
	asNode := update.AsNode
	if asNode == "" {
		asNode = "resume"
	}
	for key, val := range update.Values {
		cpData[key] = val
	}

	// 3. Determine new thread ID and parent checkpoint ID.
	//    Note: Do NOT inject metadata keys (like __update_as_node__) into cpData,
	//    because inline Pregel will try to restore them as channels.
	newThreadID := update.ThreadID
	if newThreadID == "" {
		if id, ok := cpConfig[constants.ConfigKeyThreadID].(string); ok {
			newThreadID = id
		}
	}
	if newThreadID == "" {
		return nil, fmt.Errorf("thread_id is required for UpdateState")
	}
	parentID, _ := cpConfig[constants.ConfigKeyCheckpointID].(string)

	newConfig := map[string]interface{}{
		constants.ConfigKeyThreadID:     newThreadID,
		"parent_checkpoint_id":          parentID,
		constants.ConfigKeyCheckpointID: "",
	}
	if err := cp.Put(ctx, newConfig, cpData); err != nil {
		return nil, fmt.Errorf("failed to save updated checkpoint: %w", err)
	}

	// 4. Return the config for the new checkpoint.
	listCfg := map[string]interface{}{
		constants.ConfigKeyThreadID: newThreadID,
	}
	entries, err := cp.List(ctx, listCfg, 1)
	if err == nil && len(entries) > 0 {
		newCPID, _ := entries[0][constants.ConfigKeyCheckpointID].(string)
		result := types.NewRunnableConfig()
		if config != nil && config.Configurable != nil {
			result.Configurable = make(map[string]interface{}, len(config.Configurable))
			for k, v := range config.Configurable {
				result.Configurable[k] = v
			}
		}
		if result.Configurable == nil {
			result.Configurable = make(map[string]interface{})
		}
		result.Configurable[constants.ConfigKeyCheckpointID] = newCPID
		result.Configurable[constants.ConfigKeyThreadID] = newThreadID
		return result, nil
	}

	return config, nil
}

// ---- helpers ----

// getCheckpointer 安全断言 checkpointer 为 BaseCheckpointer。
// Returns a BaseCheckpointer or a descriptive error if the cast fails.
func (cg *compiledGraph) getCheckpointer() (checkpoint.BaseCheckpointer, error) {
	cp, ok := cg.checkpointer.(checkpoint.BaseCheckpointer)
	if !ok {
		return nil, fmt.Errorf("checkpointer is not a BaseCheckpointer (got %T)", cg.checkpointer)
	}
	return cp, nil
}

// buildCheckpointerConfig 从 RunnableConfig 提取 checkpointer 配置 map。
func buildCheckpointerConfig(config *types.RunnableConfig) map[string]interface{} {
	cpConfig := make(map[string]interface{})
	if config != nil && config.Configurable != nil {
		if tid, ok := config.Configurable[constants.ConfigKeyThreadID]; ok {
			cpConfig[constants.ConfigKeyThreadID] = tid
		}
		if cpid, ok := config.Configurable[constants.ConfigKeyCheckpointID]; ok {
			cpConfig[constants.ConfigKeyCheckpointID] = cpid
		}
		if ns, ok := config.Configurable[constants.ConfigKeyCheckpointNS]; ok {
			cpConfig[constants.ConfigKeyCheckpointNS] = ns
		}
	}
	return cpConfig
}

// extractMeta 从检查点数据提取 step/last_completed_node 等元信息。
func extractMeta(cpData map[string]interface{}) map[string]interface{} {
	meta := make(map[string]interface{})
	if v, ok := cpData["__step__"]; ok {
		meta["step"] = v
	}
	if v, ok := cpData["__last_completed_node__"]; ok {
		meta["last_completed_node"] = v
	}
	return meta
}

// determineNextFromCheckpoint 推断下一批待执行节点（当前返回 nil）。
// to determine which nodes would run next.
func (cg *compiledGraph) determineNextFromCheckpoint(cpData map[string]interface{}) []string {
	// Return nil because the stored __last_completed_node__ is already
	// finished, not pending. Full edge replay is not yet implemented.
	return nil
}

// ---- CompiledStateGraph also implements StateInspector ----

var _ StateInspector = (*CompiledStateGraph)(nil)

// GetState delegates to the underlying compiledGraph.
func (csg *CompiledStateGraph) GetState(ctx context.Context, config *types.RunnableConfig) (*StateSnapshot, error) {
	return csg.compiledGraph.GetState(ctx, config)
}

// GetStateHistory delegates to the underlying compiledGraph.
func (csg *CompiledStateGraph) GetStateHistory(ctx context.Context, config *types.RunnableConfig, limit int, before *types.RunnableConfig) ([]*StateSnapshot, error) {
	return csg.compiledGraph.GetStateHistory(ctx, config, limit, before)
}

// UpdateState delegates to the underlying compiledGraph.
func (csg *CompiledStateGraph) UpdateState(ctx context.Context, config *types.RunnableConfig, update *StateUpdate) (*types.RunnableConfig, error) {
	return csg.compiledGraph.UpdateState(ctx, config, update)
}

// ---- Task type used in StateSnapshot ----

// Task 状态快照中的待办/已完成任务描述。
type Task struct {
	ID        string      `json:"id"`
	Name      string      `json:"name"`
	Error     error       `json:"error,omitempty"`
	Interrupt interface{} `json:"interrupt,omitempty"`
	State     interface{} `json:"state,omitempty"`
	Result    interface{} `json:"result,omitempty"`
}

// CheckpointConflictError UpdateState 时检查点版本冲突。
type CheckpointConflictError struct {
	Message string
}

func (e *CheckpointConflictError) Error() string {
	return e.Message
}

// ForkThread clones a checkpoint from one thread to another.
// This enables "time travel fork": creating a new thread whose initial
// state is a copy of the given checkpoint. Returns the RunnableConfig
// for the new thread.
//
// To use: invoke on a compiledGraph with a checkpointer configured.
// sourceCheckpointID: empty = latest checkpoint in source thread.
func (cg *compiledGraph) ForkThread(ctx context.Context, sourceThreadID, newThreadID string, sourceCheckpointID string) (*types.RunnableConfig, error) {
	if cg.checkpointer == nil {
		return nil, fmt.Errorf("checkpointer is required for ForkThread")
	}

	cp, err := cg.getCheckpointer()
	if err != nil {
		return nil, err
	}

	// 1. Read source checkpoint.
	cpConfig := map[string]interface{}{
		constants.ConfigKeyThreadID: sourceThreadID,
	}
	if sourceCheckpointID != "" {
		cpConfig[constants.ConfigKeyCheckpointID] = sourceCheckpointID
	}
	cpData, err := cp.Get(ctx, cpConfig)
	if err != nil {
		return nil, fmt.Errorf("failed to get source checkpoint: %w", err)
	}
	if cpData == nil {
		return nil, fmt.Errorf("no checkpoint found for thread %s", sourceThreadID)
	}

	// 2. Write to new thread as a fresh checkpoint (no parent).
	newConfig := map[string]interface{}{
		constants.ConfigKeyThreadID: newThreadID,
	}
	if err := cp.Put(ctx, newConfig, cpData); err != nil {
		return nil, fmt.Errorf("failed to write forked checkpoint: %w", err)
	}

	// 3. Return config pointing to the new thread.
	entries, err := cp.List(ctx, newConfig, 1)
	if err != nil || len(entries) == 0 {
		return &types.RunnableConfig{
			Configurable: map[string]interface{}{
				constants.ConfigKeyThreadID: newThreadID,
			},
		}, nil
	}

	newCPID, _ := entries[0][constants.ConfigKeyCheckpointID].(string)
	result := types.NewRunnableConfig()
	result.Configurable = make(map[string]interface{})
	result.Configurable[constants.ConfigKeyThreadID] = newThreadID
	if newCPID != "" {
		result.Configurable[constants.ConfigKeyCheckpointID] = newCPID
	}
	return result, nil
}

func init() {
	_ = (*CheckpointConflictError)(nil)
}

// UpdateState 勿将元数据键写入 cpData，否则 inline Pregel 会误当作通道恢复。
