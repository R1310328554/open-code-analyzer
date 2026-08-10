// agent_loop_stop.go — stopController 与 Stop/Push 选项构造函数。

package core

import (
	"context"
	"sync"
	"time"
)

// stopController 管理全局 Stop 状态与 active 轮取消请求。
type stopController struct {
	mu sync.Mutex

	phase stopPhase

	hasActiveCancelTarget bool
	pending               *stopCancelRequest
	notify                chan struct{}

	idleFor        time.Duration
	skipCheckpoint bool
	stopCause      string

	closed bool
}

// newStopController 创建 open 阶段的停止控制器。
func newStopController() *stopController {
	return &stopController{notify: make(chan struct{}, 1)}
}

// requestStop 处理 Stop 请求：idle 等待、commit 或合并 agent 取消。
func (c *stopController) requestStop(cfg *stopConfig) stopDecision {
	c.mu.Lock()
	defer c.mu.Unlock()

	if c.closed {
		return stopDecision{}
	}
	if cfg.skipCheckpoint {
		c.skipCheckpoint = true
	}
	if cfg.stopCause != "" && c.stopCause == "" {
		c.stopCause = cfg.stopCause
	}
	if cfg.idleFor > 0 {
		if c.phase != stopCommitted && c.idleFor == 0 {
			c.phase = stopIdleWaiting
			c.idleFor = cfg.idleFor
		}
		return stopDecision{wakeIdle: c.phase == stopIdleWaiting}
	}

	committed := c.commitLocked()
	if cfg.agentCancelOpts != nil {
		now := time.Now()
		if c.pending == nil {
			c.pending = newStopCancelRequest(cfg.agentCancelOpts, now)
		} else {
			c.pending.merge(cfg.agentCancelOpts, now)
		}
		if c.hasActiveCancelTarget {
			c.notifyWatcherLocked()
		}
	}
	return stopDecision{commit: committed}
}

// commit 将阶段设为 stopCommitted。
func (c *stopController) commit() bool {
	c.mu.Lock()
	defer c.mu.Unlock()
	return c.commitLocked()
}

func (c *stopController) commitLocked() bool {
	if c.closed || c.phase == stopCommitted {
		return false
	}
	c.phase = stopCommitted
	c.idleFor = 0
	return true
}

// isCommitted 是否已 commit 停止。
func (c *stopController) isCommitted() bool {
	c.mu.Lock()
	defer c.mu.Unlock()
	return c.phase == stopCommitted
}

// idleDuration 返回 UntilIdleFor 配置的等待时长。
func (c *stopController) idleDuration() time.Duration {
	c.mu.Lock()
	defer c.mu.Unlock()
	if c.phase != stopIdleWaiting {
		return 0
	}
	return c.idleFor
}

func (c *stopController) skipCheckpointEnabled() bool {
	c.mu.Lock()
	defer c.mu.Unlock()
	return c.skipCheckpoint
}

func (c *stopController) cause() string {
	c.mu.Lock()
	defer c.mu.Unlock()
	return c.stopCause
}

func (c *stopController) beginActiveTurn() {
	c.mu.Lock()
	defer c.mu.Unlock()
	if c.closed {
		return
	}
	c.hasActiveCancelTarget = true
	if c.pending != nil {
		c.notifyWatcherLocked()
	}
}

func (c *stopController) endActiveTurn() *stopCancelRequest {
	c.mu.Lock()
	defer c.mu.Unlock()
	c.hasActiveCancelTarget = false
	req := c.pending
	c.pending = nil
	return req
}

func (c *stopController) receiveCancel() (*stopCancelRequest, bool) {
	c.mu.Lock()
	defer c.mu.Unlock()
	if !c.hasActiveCancelTarget || c.pending == nil {
		return nil, false
	}
	req := c.pending
	c.pending = nil
	return req, true
}

func (c *stopController) closeForLoopExit() {
	c.mu.Lock()
	defer c.mu.Unlock()
	c.closed = true
	c.hasActiveCancelTarget = false
	c.pending = nil
	select {
	case <-c.notify:
	default:
	}
}

func (c *stopController) notifyWatcherLocked() {
	select {
	case c.notify <- struct{}{}:
	default:
	}
}

// ---- StopOption 构造函数 ----

// WithGraceful 在 ChatModel/ToolCalls 安全点后优雅取消。
func WithGraceful() StopOption {
	return func(cfg *stopConfig) {
		cfg.agentCancelOpts = []CancelOption{
			WithCancelMode(CancelAfterChatModel | CancelAfterToolCalls),
			WithRecursiveCancel(),
		}
	}
}

// WithImmediate 立即递归取消 Agent。
func WithImmediate() StopOption {
	return func(cfg *stopConfig) {
		cfg.agentCancelOpts = []CancelOption{
			WithRecursiveCancel(),
		}
	}
}

// WithGracefulTimeout 优雅取消并设 grace 超时。
func WithGracefulTimeout(gracePeriod time.Duration) StopOption {
	if gracePeriod <= 0 {
		panic("agentcore: WithGracefulTimeout: gracePeriod must be positive")
	}
	return func(cfg *stopConfig) {
		cfg.agentCancelOpts = []CancelOption{
			WithCancelMode(CancelAfterChatModel | CancelAfterToolCalls),
			WithRecursiveCancel(),
			WithCancelTimeout(gracePeriod),
		}
	}
}

// WithStopTimeout 设置 Stop 整体超时。
func WithStopTimeout(d time.Duration) StopOption {
	return func(cfg *stopConfig) { cfg.timeout = &d }
}

// WithSkipCheckpoint 退出时不保存检查点。
func WithSkipCheckpoint() StopOption {
	return func(cfg *stopConfig) {
		cfg.skipCheckpoint = true
	}
}

// WithStopCause 记录停止原因字符串。
func WithStopCause(cause string) StopOption {
	return func(cfg *stopConfig) {
		cfg.stopCause = cause
	}
}

// UntilIdleFor 空闲 duration 后自动 commit 停止。
func UntilIdleFor(duration time.Duration) StopOption {
	if duration <= 0 {
		panic("agentcore: UntilIdleFor: duration must be positive")
	}
	return func(cfg *stopConfig) {
		cfg.idleFor = duration
	}
}

// ---- PushOption 构造函数 ----

// WithPreempt 入队后按 SafePoint 抢占当前轮。
func WithPreempt[T any](safePoint SafePoint) PushOption[T] {
	if safePoint == 0 {
		panic("agentcore: SafePoint must not be zero; use AfterToolCalls, AfterChatModel, or AnySafePoint")
	}
	return func(cfg *pushConfig[T]) {
		cfg.preempt = true
		cfg.agentCancelOpts = []CancelOption{
			WithCancelMode(safePoint.toCancelMode()),
			WithRecursiveCancel(),
		}
	}
}

// WithPreemptTimeout 抢占并设 Agent 取消超时。
func WithPreemptTimeout[T any](safePoint SafePoint, timeout time.Duration) PushOption[T] {
	if safePoint == 0 {
		panic("agentcore: SafePoint must not be zero; use AfterToolCalls, AfterChatModel, or AnySafePoint")
	}
	return func(cfg *pushConfig[T]) {
		cfg.preempt = true
		cfg.agentCancelOpts = []CancelOption{
			WithCancelMode(safePoint.toCancelMode()),
			WithCancelTimeout(timeout),
			WithRecursiveCancel(),
		}
	}
}

// WithPreemptDelay 延迟发起抢占。
func WithPreemptDelay[T any](delay time.Duration) PushOption[T] {
	return func(cfg *pushConfig[T]) {
		cfg.preemptDelay = delay
	}
}

// WithPushStrategy 自定义 Push 策略函数。
func WithPushStrategy[T any](fn func(ctx context.Context, tc *TurnContext[T]) []PushOption[T]) PushOption[T] {
	return func(cfg *pushConfig[T]) {
		cfg.pushStrategy = fn
	}
}

// ---- 已弃用别名 ----

func WithImmediateStop() StopOption { return WithImmediate() }
func WithGracefulStop() StopOption  { return WithGraceful() }
