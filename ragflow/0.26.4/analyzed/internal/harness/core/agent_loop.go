// agent_loop.go — AgentLoop 核心：结构体、生命周期（Run/Stop/Wait）与退出清理。
// 配置见 agent_loop_config.go；执行逻辑分散在 run/agent/push/checkpoint 等文件。

package core

import (
	"context"
	"errors"
	"sync"
	"sync/atomic"
	"time"
)

// ---- AgentLoop 核心：结构体、生命周期与清理 ----
//
// 配置类型（AgentLoopConfig、preemptController、stopController 等）
// 定义于 agent_loop_config.go、agent_loop_preempt.go、agent_loop_stop.go。
// 执行逻辑拆分于：
//   - agent_loop_run.go     （planTurn、run、defaultTurnLoopOnAgentEvents）
//   - agent_loop_agent.go   （runAgentAndHandleEvents、watchPreempt、watchStop、setupBridgeStore）
//   - agent_loop_push.go    （Push、pushWithStrategy、pushWithConfig、appendLate）
//   - agent_loop_checkpoint.go （检查点序列化、tryLoadCheckpoint）

// AgentLoop 在 push 驱动的循环中执行 Agent 轮次。
// 配置见 AgentLoopConfig；退出结果见 AgentLoopState。
type AgentLoop[T any] struct {
	config AgentLoopConfig[T]

	buffer *turnBuffer[T]

	stopped int32
	started int32

	done chan struct{}

	result *AgentLoopState[T]

	runOnce sync.Once

	stopCtrl *stopController

	preemptCtrl *preemptController

	runErr error

	interruptedItems []T

	checkPointRunnerBytes []byte
	interruptContexts     []*InterruptCtx
	capturedCancelErr     *CancelError

	pendingResume *agentLoopPendingResume[T]

	loadCheckpointID string

	onAgentEvents func(ctx context.Context, tc *TurnContext[T], events *AsyncIterator[*AgentEvent]) error

	lateMu     sync.Mutex
	lateItems  []T
	lateSealed bool
}

// NewAgentLoop 创建 AgentLoop 实例，不自动启动。
func NewAgentLoop[T any](cfg AgentLoopConfig[T]) *AgentLoop[T] {
	if cfg.GenInput == nil {
		panic("agentcore: NewAgentLoop: GenInput is required")
	}
	if cfg.PrepareAgent == nil {
		panic("agentcore: NewAgentLoop: PrepareAgent is required")
	}

	l := &AgentLoop[T]{
		config:      cfg,
		buffer:      newTurnBuffer[T](),
		done:        make(chan struct{}),
		stopCtrl:    newStopController(),
		preemptCtrl: newPreemptController(),
	}
	if cfg.OnAgentEvents != nil {
		l.onAgentEvents = cfg.OnAgentEvents
	} else {
		l.onAgentEvents = defaultTurnLoopOnAgentEvents[T]
	}
	return l
}

// start 通过 sync.Once 保证只启动一次 run goroutine。
func (l *AgentLoop[T]) start(ctx context.Context) {
	l.runOnce.Do(func() {
		atomic.StoreInt32(&l.started, 1)
		go l.run(ctx)
	})
}

// Run 启动后台处理 goroutine，非阻塞。
func (l *AgentLoop[T]) Run(ctx context.Context) {
	l.start(ctx)
}

// Stop 发出停止信号并立即返回（非阻塞）。
func (l *AgentLoop[T]) Stop(opts ...StopOption) {
	cfg := &stopConfig{}
	for _, opt := range opts {
		opt(cfg)
	}

	if cfg.idleFor > 0 {
		cfg.agentCancelOpts = nil
	}

	decision := l.stopCtrl.requestStop(cfg)
	if decision.wakeIdle {
		l.buffer.Wakeup()
	}
	if decision.commit {
		l.finishStopCommit()
	}

	// 若配置了停止超时，超时后强制 commitStop
	if cfg.timeout != nil && *cfg.timeout > 0 {
		go func() {
			select {
			case <-time.After(*cfg.timeout):
				l.commitStop()
			case <-l.done:
			}
		}()
	}
}

// commitStop 尝试 commit 停止并关闭 buffer。
func (l *AgentLoop[T]) commitStop() {
	if !l.stopCtrl.commit() {
		return
	}
	l.finishStopCommit()
}

// finishStopCommit 标记 stopped 并关闭 turnBuffer。
func (l *AgentLoop[T]) finishStopCommit() {
	atomic.StoreInt32(&l.stopped, 1)
	l.buffer.Close()
}

// Wait 阻塞至循环退出并返回 AgentLoopState。
func (l *AgentLoop[T]) Wait() *AgentLoopState[T] {
	<-l.done
	return l.result
}

// shouldSaveCheckpoint 判断是否应保存轮次循环检查点。
// 满足以下任一条件时保存检查点：
//  1. 已 commit 停止且退出由停止引起（runErr==nil、CancelError 或 capturedCancelErr）。
//  2. 发生业务中断（InterruptError 或 interruptContexts）。
//  3. 未跳过检查点、非空闲且 Store 可用。
//
// 正常完成（runErr==nil 且未 commit 停止）时不保存检查点。
func (l *AgentLoop[T]) shouldSaveCheckpoint() bool {
	if l.config.Store == nil || l.config.CheckpointID == "" {
		return false
	}
	if l.stopCtrl.skipCheckpointEnabled() {
		return false
	}
	isIdle := len(l.checkPointRunnerBytes) == 0 && len(l.interruptedItems) == 0
	if isIdle {
		return false
	}
	exitCausedByStop := l.runErr == nil || errors.As(l.runErr, new(*CancelError)) || l.capturedCancelErr != nil
	businessInterrupt := errors.As(l.runErr, new(*InterruptError)) || l.interruptContexts != nil
	return (l.stopCtrl.isCommitted() && exitCausedByStop) || businessInterrupt
}

// cleanup 在 run 退出时组装 AgentLoopState、保存/删除检查点并关闭 done。
func (l *AgentLoop[T]) cleanup(ctx context.Context) {
	atomic.StoreInt32(&l.stopped, 1)

	unhandled := l.buffer.TakeAll()
	checkpointID := l.config.CheckpointID
	shouldSaveCheckpoint := l.shouldSaveCheckpoint()

	var checkpointed bool
	var checkpointErr error

	if shouldSaveCheckpoint {
		cp := &agentLoopCheckpoint[T]{
			RunnerCheckpoint: l.checkPointRunnerBytes,
			HasRunnerState:   len(l.checkPointRunnerBytes) > 0,
			UnhandledItems:   unhandled,
			CanceledItems:    l.interruptedItems,
		}
		checkpointed = true
		checkpointErr = l.saveTurnLoopCheckpoint(ctx, checkpointID, cp)
	} else if l.loadCheckpointID != "" {
		_ = l.deleteTurnLoopCheckpoint(ctx, l.loadCheckpointID)
	}

	var takeLateOnce sync.Once
	var takeLateResult []T

	l.result = &AgentLoopState[T]{
		ExitReason:          l.runErr,
		UnhandledItems:      unhandled,
		InterruptedItems:    l.interruptedItems,
		StopCause:           l.stopCtrl.cause(),
		CheckpointAttempted: checkpointed,
		CheckpointErr:       checkpointErr,
		TakeLateItems: func() []T {
			takeLateOnce.Do(func() {
				l.lateMu.Lock()
				takeLateResult = append([]T{}, l.lateItems...)
				l.lateSealed = true
				l.lateMu.Unlock()
			})
			return takeLateResult
		},
	}

	l.stopCtrl.closeForLoopExit()
	l.preemptCtrl.closeForLoopExit()
	l.buffer.Close()
	close(l.done)
}

// lateItems 在 TakeLateItems 调用后密封；Stop 超时 goroutine 与 done 竞争以避免泄漏。
