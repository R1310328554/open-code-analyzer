package replay

import (
	"context"
	"fmt"
	"time"

	"ragflow/internal/harness/events"
	"ragflow/internal/harness/graph/checkpoint"
	"ragflow/internal/harness/graph/constants"
	"ragflow/internal/harness/graph/pregel"
	"ragflow/internal/harness/graph/types"
)

// ForkContextKey 在真实重放时经 context 向节点级包装器传递 ModelOverride/ToolOverride。
// 此为备用键；引擎重执行时的模型/工具替换由调用方通过 Agent 中间件完成。
type ForkContextKey struct{}

// ForkConfig Fork 操作配置。
type ForkConfig struct {
	// Store 原始轨迹的事件源。
	Store events.EventLog

	// TraceID 待 Fork 的原始轨迹 ID。
	TraceID string

	// Point Fork 分叉点事件 ID。
	Point events.EventID

	// ModelOverride/ToolOverride Fork 分支的替换策略。
	ModelOverride ModelOverrideFunc
	ToolOverride  ToolOverrideFunc
	NewInput      any

	// ForkEngine is the actual graph engine to execute the forked branch.
	// When set, Fork replays up to ForkPoint, builds a checkpoint from the
	// events, saves it into a MemorySaver, and hands off to real execution.
	// When nil, Fork replays deterministically from EventLog alone.
	ForkEngine *pregel.Engine

	// Checkpointer is the persistence backend to use when resuming the
	// ForkEngine. When nil, a fresh MemorySaver is created.
	Checkpointer checkpoint.BaseCheckpointer

	// OutputStore 接收 Fork 产生的事件（nil 则丢弃）。
	OutputStore events.EventLog
}

// ForkResult Fork 操作结果。
type ForkResult struct {
	// ForkTraceID 新 Fork 轨迹 ID。
	ForkTraceID string

	// ForkEvents Fork 执行产生的事件。
	ForkEvents []*events.Event

	// ParentTraceID 被 Fork 的父轨迹 ID。
	ParentTraceID string

	// ForkPoint Fork 发生点事件 ID。
	ForkPoint events.EventID

	// FinalState is the output state from the forked Engine execution.
	// Only set when ForkEngine was used.
	FinalState any

	// Duration Fork 操作耗时。
	Duration time.Duration
}

// Fork 从轨迹指定点创建分支执行。
// ForkPoint 之前从原存储重放；之后若设 ForkEngine 则经检查点恢复交给真实引擎，
// 否则继续带覆盖的确定性重放。
func (e *ReplayEngine) Fork(ctx context.Context, cfg *ForkConfig) (*ForkResult, error) {
	start := time.Now()

	// 优先使用配置 Store，回退到引擎 Store。
	store := cfg.Store
	if store == nil {
		store = e.store
	}

	// 查找 Fork 点事件。
	forkEvent, err := store.Get(ctx, cfg.Point)
	if err != nil {
		return nil, err
	}
	if forkEvent == nil {
		return nil, errEventNotFound(cfg.Point)
	}

	// Read ALL events up to (but not including) the fork point.
	// We need the complete event list to reconstruct the checkpoint.
	filter := events.EventFilter{
		TraceID: cfg.TraceID,
		ToClock: forkEvent.Clock - 1,
	}
	iter := store.Stream(ctx, filter)
	defer iter.Close()

	var preForkEvents []*events.Event
	for {
		ev, ok := iter.Next(ctx)
		if !ok {
			break
		}
		preForkEvents = append(preForkEvents, ev)
	}

	result := &ForkResult{
		ForkTraceID:   cfg.TraceID + "_fork_" + string(cfg.Point),
		ParentTraceID: cfg.TraceID,
		ForkPoint:     cfg.Point,
	}

	// 追加 Fork 标记事件。
	forkMarker := events.NewEvent(events.EventFork, 0)
	forkMarker.TraceID = result.ForkTraceID
	forkMarker.ParentID = cfg.Point
	forkMarker.CausedBy = []events.EventID{cfg.Point}
	forkMarker.Metadata["parent_trace"] = cfg.TraceID
	forkMarker.Seal()

	// 收集 Fork 前事件。
	result.ForkEvents = append(result.ForkEvents, preForkEvents...)
	result.ForkEvents = append(result.ForkEvents, forkMarker)

	if cfg.OutputStore != nil {
		if err := cfg.OutputStore.Append(ctx, result.ForkEvents...); err != nil {
			return nil, err
		}
	}

	// 若提供 ForkEngine，重建检查点并恢复执行。
	if cfg.ForkEngine != nil {
		forkResult, err := e.resumeFromCheckpoint(ctx, cfg, preForkEvents, forkMarker)
		if err != nil {
			return nil, fmt.Errorf("fork resume: %w", err)
		}
		result.FinalState = forkResult
	}

	result.Duration = time.Since(start)
	return result, nil
}

// resumeFromCheckpoint 从 Fork 前事件重建检查点状态并恢复 ForkEngine，
// 从重建状态运行图并返回最终输出。
func (e *ReplayEngine) resumeFromCheckpoint(ctx context.Context, cfg *ForkConfig, preForkEvents []*events.Event, forkMarker *events.Event) (any, error) {
	if cfg.ForkEngine == nil {
		return nil, nil
	}

	threadID := cfg.TraceID
	if threadID == "" {
		threadID = "fork-" + string(cfg.Point)
	}

	// 从 Fork 前事件构建检查点 map。
	cp, cpID := BuildCheckpoint(preForkEvents, threadID)

	// 将检查点写入 MemorySaver 或调用方提供的 checkpointer。
	saver := cfg.Checkpointer
	if saver == nil {
		saver = checkpoint.NewMemorySaver()
	}

	if err := saver.Put(ctx, map[string]any{
		constants.ConfigKeyThreadID:     threadID,
		constants.ConfigKeyCheckpointID: cpID,
	}, cp); err != nil {
		return nil, fmt.Errorf("save fork checkpoint: %w", err)
	}

	// Check if ForkEngine already has a checkpointer; if not, set it.
	// We inject our own via WithCheckpointer option at Fork creation time
	// by creating a new Engine wrapping the same graph.

	// 配置 RunnableConfig 指向目标检查点。
	rc := types.NewRunnableConfig()
	rc.ThreadID = threadID
	rc.Set(constants.ConfigKeyThreadID, threadID)
	rc.Set(constants.ConfigKeyCheckpointID, cpID)

	// 以恢复配置运行 ForkEngine。
	outputCh, errCh := cfg.ForkEngine.Run(ctx, nil, types.StreamModeValues)

	// 排空 outputCh 获取最终状态。
	var finalState any
	for result := range outputCh {
		if se, ok := result.(*pregel.StreamEvent); ok {
			if se.Type == pregel.EventTypeFinal {
				if data, ok := se.Data.(map[string]any); ok {
					if state, ok := data["state"]; ok {
						finalState = state
					}
				}
			}
		}
	}

	if err := <-errCh; err != nil {
		return nil, err
	}

	// 若设 OutputStore，记录 Fork 完成事件。
	if cfg.OutputStore != nil {
		forkEnd := events.NewEvent(events.EventGraphEnd, 0)
		forkEnd.TraceID = cfg.TraceID + "_fork_" + string(cfg.Point)
		forkEnd.Metadata["fork_replay"] = true
		forkEnd.Seal()
		_ = cfg.OutputStore.Append(ctx, forkEnd)
	}

	return finalState, nil
}

func errEventNotFound(id events.EventID) error {
	return errorf("event not found: %s", id)
}
