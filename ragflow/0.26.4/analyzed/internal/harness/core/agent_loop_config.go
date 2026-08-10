// agent_loop_config.go — AgentLoop 配置、停止/抢占/推送选项及检查点相关类型。

package core

import (
	"context"
	"fmt"
	"time"
)

// stopPhase 停止承诺生命周期阶段。
type stopPhase uint8

const (
	stopOpen stopPhase = iota
	stopIdleWaiting
	stopCommitted
)

// preemptTurnPhase 抢占轮次生命周期阶段。
type preemptTurnPhase uint8

const (
	preemptTurnIdle preemptTurnPhase = iota
	preemptTurnPlanning
	preemptTurnActive
)

func (p preemptTurnPhase) String() string {
	switch p {
	case preemptTurnIdle:
		return "idle"
	case preemptTurnPlanning:
		return "planning"
	case preemptTurnActive:
		return "active"
	default:
		return "unknown"
	}
}

// preemptTurnSnapshot Push 时刻的目标轮次快照。
type preemptTurnSnapshot struct {
	hasTargetTurn bool
	turnID        uint64
	ctx           context.Context
	tc            any
}

// cancelRequestState 取消请求配置，含可选超时截止。
type cancelRequestState struct {
	cfg             cancelConfig
	timeoutDeadline *time.Time
}

// parseCancelOptions 解析 CancelOption 列表为 cancelConfig。
func parseCancelOptions(opts ...CancelOption) cancelConfig {
	cfg := cancelConfig{Mode: CancelImmediate}
	for _, opt := range opts {
		opt(&cfg)
	}
	return cfg
}

// newCancelRequestState 构造带截止时间的取消请求状态。
func newCancelRequestState(opts []CancelOption, now time.Time) cancelRequestState {
	cfg := parseCancelOptions(opts...)
	var deadline *time.Time
	if cfg.Timeout != nil && *cfg.Timeout > 0 && cfg.Mode != CancelImmediate {
		d := now.Add(*cfg.Timeout)
		deadline = &d
	}
	cfg.Timeout = nil

	return cancelRequestState{
		cfg:             cfg,
		timeoutDeadline: deadline,
	}
}

// merge 合并后续取消选项（Immediate 优先，取最早 deadline）。
func (s *cancelRequestState) merge(opts []CancelOption, now time.Time) {
	if opts == nil {
		return
	}

	next := newCancelRequestState(opts, now)
	if s.cfg.Mode == CancelImmediate || next.cfg.Mode == CancelImmediate {
		s.cfg.Mode = CancelImmediate
		s.timeoutDeadline = nil
	} else {
		s.cfg.Mode |= next.cfg.Mode
		if next.timeoutDeadline != nil {
			if s.timeoutDeadline == nil || next.timeoutDeadline.Before(*s.timeoutDeadline) {
				deadline := *next.timeoutDeadline
				s.timeoutDeadline = &deadline
			}
		}
	}
	if next.cfg.Recursive {
		s.cfg.Recursive = true
	}
}

// cancelOptions 将内部状态转为 CancelOption 切片供 Agent 取消。
func (s cancelRequestState) cancelOptions(now time.Time) []CancelOption {
	cfg := s.cfg
	if cfg.Mode != CancelImmediate && s.timeoutDeadline != nil {
		remaining := s.timeoutDeadline.Sub(now)
		if remaining <= 0 {
			cfg.Mode = CancelImmediate
			cfg.Timeout = nil
		} else {
			cfg.Timeout = &remaining
		}
	}

	opts := []CancelOption{WithCancelMode(cfg.Mode)}
	if cfg.Recursive {
		opts = append(opts, WithRecursiveCancel())
	}
	if cfg.Timeout != nil {
		opts = append(opts, WithCancelTimeout(*cfg.Timeout))
	}
	return opts
}

// AgentLoopConfig 创建 AgentLoop 的配置。
type AgentLoopConfig[T any] struct {
	GenInput func(ctx context.Context, loop *AgentLoop[T], items []T) (*GenInputResult[T], error)

	GenResume func(ctx context.Context, loop *AgentLoop[T], interruptedItems, unhandledItems, newItems []T) (*GenResumeResult[T], error)

	PrepareAgent func(ctx context.Context, loop *AgentLoop[T], consumed []T) (Agent, error)

	OnAgentEvents func(ctx context.Context, tc *TurnContext[T], events *AsyncIterator[*AgentEvent]) error

	Store CheckPointStore

	CheckpointID string
}

// GenInputResult GenInput 回调的返回结果。
type GenInputResult[T any] struct {
	RunCtx    context.Context
	Input     *AgentInput
	RunOpts   []RunOption
	Consumed  []T
	Remaining []T
}

// GenResumeResult GenResume 回调的返回结果。
type GenResumeResult[T any] struct {
	RunCtx       context.Context
	RunOpts      []RunOption
	ResumeParams *ResumeParams
	Consumed     []T
	Remaining    []T
}

type turnRunSpec[T any] struct {
	runCtx       context.Context
	input        *AgentInput
	runOpts      []RunOption
	resumeParams *ResumeParams
	isResume     bool
	consumed     []T
	resumeBytes  []byte
}

type turnPlan[T any] struct {
	turnCtx   context.Context
	remaining []T
	spec      *turnRunSpec[T]
}

// AgentLoopState AgentLoop 退出时的最终状态。
type AgentLoopState[T any] struct {
	ExitReason          error
	UnhandledItems      []T
	InterruptedItems    []T
	StopCause           string
	CheckpointAttempted bool
	CheckpointErr       error
	TakeLateItems       func() []T
}

// TurnContext 为 OnAgentEvents 提供单轮上下文（Consumed、Preempted、Stopped 等）。
type TurnContext[T any] struct {
	Loop      *AgentLoop[T]
	Consumed  []T
	Preempted <-chan struct{}
	Stopped   <-chan struct{}
	StopCause func() string
}

type agentLoopCheckpoint[T any] struct {
	RunnerCheckpoint []byte
	HasRunnerState   bool
	UnhandledItems   []T
	CanceledItems    []T
}

type agentLoopPendingResume[T any] struct {
	interrupted []T
	unhandled   []T
	newItems    []T
	resumeBytes []byte
}

// SafePoint 描述 Agent 可被取消的安全边界。
type SafePoint int

const (
	AfterChatModel SafePoint = 1 << iota
	AfterToolCalls
	AnySafePoint = AfterChatModel | AfterToolCalls
)

func (sp SafePoint) toCancelMode() CancelMode {
	var mode CancelMode
	if sp&AfterToolCalls != 0 {
		mode |= CancelAfterToolCalls
	}
	if sp&AfterChatModel != 0 {
		mode |= CancelAfterChatModel
	}
	return mode
}

type stopConfig struct {
	agentCancelOpts []CancelOption
	skipCheckpoint  bool
	stopCause       string
	idleFor         time.Duration
	timeout         *time.Duration
}

type pushConfig[T any] struct {
	preempt         bool
	preemptDelay    time.Duration
	agentCancelOpts []CancelOption
	pushStrategy    func(context.Context, *TurnContext[T]) []PushOption[T]
}

// StopOption Stop() 的可选参数。
type StopOption func(*stopConfig)

// PushOption Push() 的可选参数。
type PushOption[T any] func(*pushConfig[T])

// InterruptError 表示轮次内业务中断。
type InterruptError struct {
	InterruptContexts []*InterruptCtx
}

func (e *InterruptError) Error() string {
	return fmt.Sprintf("agent interrupted: %d context(s)", len(e.InterruptContexts))
}

// stopDecision Stop 请求的处理结果（是否 commit、是否唤醒 idle）。
type stopDecision struct {
	commit   bool
	wakeIdle bool
}

type stopCancelRequest struct {
	cancel cancelRequestState
}

// newStopCancelRequest 构造 Stop 路径的取消请求。
func newStopCancelRequest(opts []CancelOption, now time.Time) *stopCancelRequest {
	return &stopCancelRequest{cancel: newCancelRequestState(opts, now)}
}

func (r *stopCancelRequest) merge(opts []CancelOption, now time.Time) {
	if r == nil {
		return
	}
	r.cancel.merge(opts, now)
}

func (r *stopCancelRequest) cancelOptions(now time.Time) []CancelOption {
	if r == nil {
		return nil
	}
	return r.cancel.cancelOptions(now)
}

// preemptRequest 待处理的抢占请求（含 ack 通道）。
type preemptRequest struct {
	cancel   cancelRequestState
	ackChans []chan struct{}
}

// newPreemptRequest 构造抢占请求并注册 ack 通道。
func newPreemptRequest(ack chan struct{}, opts []CancelOption, now time.Time) *preemptRequest {
	req := &preemptRequest{cancel: newCancelRequestState(opts, now)}
	if ack != nil {
		req.ackChans = append(req.ackChans, ack)
	}
	return req
}

// ack 关闭所有 ack 通道，通知 Push 调用方抢占已处理。
func (r *preemptRequest) ack() {
	if r == nil {
		return
	}
	for _, ack := range r.ackChans {
		close(ack)
	}
	r.ackChans = nil
}

func (r *preemptRequest) merge(ack chan struct{}, opts []CancelOption, now time.Time) {
	if ack != nil {
		r.ackChans = append(r.ackChans, ack)
	}
	r.cancel.merge(opts, now)
}

func (r *preemptRequest) cancelOptions(now time.Time) []CancelOption {
	if r == nil {
		return nil
	}
	return r.cancel.cancelOptions(now)
}

// SafePoint.toCancelMode 映射为 CancelAfterChatModel/AfterToolCalls；agentLoopPendingResume 在恢复轮合并 interrupted/unhandled/newItems。
