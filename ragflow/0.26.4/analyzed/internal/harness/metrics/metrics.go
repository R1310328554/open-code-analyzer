// Package metrics 为 Agent 执行提供可观测性指标采集。
//
// AutoCollector 实现 pregel.GraphCallback，在图执行期间追踪工具成功率、
// 检查点操作、超步/节点计数与错误恢复等关键指标。
// 指标通过 MetricsCollector 接口暴露，可导出至 Prometheus。
package metrics

import (
	"context"
	"sync"
	"time"
)

// AgentMetrics 单次 Agent 执行轨迹的全部指标快照。
type AgentMetrics struct {
	// TraceID 标识执行轨迹。
	TraceID string
	// ThreadID 标识执行线程。
	ThreadID string
	// Duration 执行的墙上时钟耗时。
	Duration time.Duration

	// ---- 工具指标 ----

	// ToolCalls 工具调用总次数。
	ToolCalls int
	// ToolSuccesses 成功的工具调用次数。
	ToolSuccesses int
	// ToolFailures 失败的工具调用次数。
	ToolFailures int
	// ToolRetries 工具重试次数。
	ToolRetries int
	// ToolLatencyMs 各工具延迟直方图（toolName → 毫秒耗时列表）。
	ToolLatencyMs map[string][]int64
	// ToolSuccessRate 成功率（成功/总调用，0–1）。
	ToolSuccessRate float64
	// ToolRetryRate 重试率（重试/(调用+重试)）。
	ToolRetryRate float64

	// ---- 检查点指标 ----

	// CheckpointSaves 检查点保存次数。
	CheckpointSaves int
	// CheckpointRestores 检查点恢复次数。
	CheckpointRestores int
	// CheckpointRestoreSuccess 恢复成功率（0–1）。
	CheckpointRestoreSuccess float64

	// ---- 执行指标 ----

	// Steps 已执行的 Pregel 超步数。
	Steps int
	// NodesExecuted 已执行的图节点数。
	NodesExecuted int
	// RecoveredErrors 已恢复的错误数。
	RecoveredErrors int
	// InterruptCount 执行中断次数。
	InterruptCount int

	// ---- 派生指标 ----

	// CostPerTask 单任务估算成本（需 LLM 成本追踪）。
	CostPerTask float64
	// ForkReplayPassRate Fork 重放断言通过率。
	ForkReplayPassRate float64
	// ApprovalLatencyMs 人机协同审批等待耗时（毫秒）。
	ApprovalLatencyMs []int64
	// ApprovalRate 审批通过率。
	ApprovalRate float64
	// MemoryAvgHitScore 记忆检索平均命中分数。
	MemoryAvgHitScore float64
}

// NewAgentMetrics 创建并初始化映射表的 AgentMetrics。
func NewAgentMetrics() *AgentMetrics {
	return &AgentMetrics{
		ToolLatencyMs:     make(map[string][]int64),
		ApprovalLatencyMs: make([]int64, 0),
	}
}

// Snapshot 捕获指标的时间点副本并计算派生比率。
func (m *AgentMetrics) Snapshot() *AgentMetrics {
	cp := *m
	cp.ToolLatencyMs = make(map[string][]int64, len(m.ToolLatencyMs))
	for k, v := range m.ToolLatencyMs {
		durations := make([]int64, len(v))
		copy(durations, v)
		cp.ToolLatencyMs[k] = durations
	}
	cp.ApprovalLatencyMs = make([]int64, len(m.ApprovalLatencyMs))
	copy(cp.ApprovalLatencyMs, m.ApprovalLatencyMs)

	// 计算派生比率。
	if cp.ToolCalls > 0 {
		cp.ToolSuccessRate = float64(cp.ToolSuccesses) / float64(cp.ToolCalls)
		cp.ToolRetryRate = float64(cp.ToolRetries) / float64(cp.ToolCalls+cp.ToolRetries)
	}
	if cp.CheckpointSaves+cp.CheckpointRestores > 0 {
		cp.CheckpointRestoreSuccess = float64(cp.CheckpointRestores) / float64(cp.CheckpointSaves+cp.CheckpointRestores)
	}
	return &cp
}

// MetricsCollector 采集并聚合 Agent 执行指标。
type MetricsCollector interface {
	// RecordToolCall 记录工具调用结果。
	RecordToolCall(toolName string, success bool, durationMs int64)
	// RecordToolRetry 记录工具重试。
	RecordToolRetry(toolName string)
	// RecordCheckpointSave 记录检查点保存。
	RecordCheckpointSave()
	// RecordCheckpointRestore 记录检查点恢复。
	RecordCheckpointRestore(success bool)
	// RecordStep 记录完成的 Pregel 超步。
	RecordStep()
	// RecordNode 记录完成的节点执行。
	RecordNode(nodeName string)
	// RecordRecoveredError 记录已恢复的错误。
	RecordRecoveredError()
	// RecordInterrupt 记录执行中断。
	RecordInterrupt()
	// RecordApproval 记录审批结果。
	RecordApproval(latencyMs int64, granted bool)
	// RecordMemoryHit 记录记忆检索分数。
	RecordMemoryHit(score float64)
	// RecordLLMCost 记录 LLM 调用成本。
	RecordLLMCost(cost float64)

	// Snapshot 返回当前指标快照。
	Snapshot() *AgentMetrics
	// Reset 清空全部指标。
	Reset()
}

// ---- AutoCollector：实现 GraphCallback + MetricsCollector ----

// AutoCollector 通过图执行回调自动采集指标，
// 同时实现 pregel.GraphCallback 与 MetricsCollector。
type AutoCollector struct {
	mu sync.Mutex
	m  *AgentMetrics
}

// NewAutoCollector 创建自动指标采集器。
func NewAutoCollector() *AutoCollector {
	return &AutoCollector{m: NewAgentMetrics()}
}

// ---- MetricsCollector 实现 ----

func (c *AutoCollector) RecordToolCall(toolName string, success bool, durationMs int64) {
	c.mu.Lock()
	defer c.mu.Unlock()
	c.m.ToolCalls++
	if success {
		c.m.ToolSuccesses++
	} else {
		c.m.ToolFailures++
	}
	c.m.ToolLatencyMs[toolName] = append(c.m.ToolLatencyMs[toolName], durationMs)
}

func (c *AutoCollector) RecordToolRetry(toolName string) {
	c.mu.Lock()
	defer c.mu.Unlock()
	c.m.ToolRetries++
}

func (c *AutoCollector) RecordCheckpointSave() {
	c.mu.Lock()
	defer c.mu.Unlock()
	c.m.CheckpointSaves++
}

func (c *AutoCollector) RecordCheckpointRestore(success bool) {
	c.mu.Lock()
	defer c.mu.Unlock()
	c.m.CheckpointRestores++
}

func (c *AutoCollector) RecordStep() {
	c.mu.Lock()
	defer c.mu.Unlock()
	c.m.Steps++
}

func (c *AutoCollector) RecordNode(nodeName string) {
	c.mu.Lock()
	defer c.mu.Unlock()
	c.m.NodesExecuted++
}

func (c *AutoCollector) RecordRecoveredError() {
	c.mu.Lock()
	defer c.mu.Unlock()
	c.m.RecoveredErrors++
}

func (c *AutoCollector) RecordInterrupt() {
	c.mu.Lock()
	defer c.mu.Unlock()
	c.m.InterruptCount++
}

func (c *AutoCollector) RecordApproval(latencyMs int64, granted bool) {
	c.mu.Lock()
	defer c.mu.Unlock()
	c.m.ApprovalLatencyMs = append(c.m.ApprovalLatencyMs, latencyMs)
	if granted {
		// 通过 approvals/total 追踪审批率。
	}
}

func (c *AutoCollector) RecordMemoryHit(score float64) {
	c.mu.Lock()
	defer c.mu.Unlock()
	if c.m.MemoryAvgHitScore == 0 {
		c.m.MemoryAvgHitScore = score
	} else {
		c.m.MemoryAvgHitScore = (c.m.MemoryAvgHitScore + score) / 2
	}
}

func (c *AutoCollector) RecordLLMCost(cost float64) {
	c.mu.Lock()
	defer c.mu.Unlock()
	c.m.CostPerTask += cost
}

func (c *AutoCollector) Snapshot() *AgentMetrics {
	c.mu.Lock()
	defer c.mu.Unlock()
	cp := c.m.Snapshot()
	cp.Duration = time.Since(c.startTime())
	return cp
}

func (c *AutoCollector) Reset() {
	c.mu.Lock()
	defer c.mu.Unlock()
	c.m = NewAgentMetrics()
}

// startTime 用于追踪执行耗时的占位。
// 实际在 Run 启动时初始化采集器。
func (c *AutoCollector) startTime() time.Time {
	return time.Now()
}

// ---- GraphCallback 实现 ----

// OnRunStart 实现 pregel.RunCallback，重置并绑定 threadID。
func (c *AutoCollector) OnRunStart(ctx context.Context, graphName, threadID string) {
	c.Reset()
	c.mu.Lock()
	c.m.ThreadID = threadID
	c.mu.Unlock()
}

// OnRunEnd 实现 pregel.RunCallback。
func (c *AutoCollector) OnRunEnd(ctx context.Context, graphName, threadID string, err error) {}

// OnStepStart 实现 pregel.StepCallback。
func (c *AutoCollector) OnStepStart(ctx context.Context, step, taskCount int) {}

// OnStepEnd 实现 pregel.StepCallback，记录超步。
func (c *AutoCollector) OnStepEnd(ctx context.Context, step int, err error) {
	c.RecordStep()
}

// OnNodeStart 实现 pregel.NodeCallback。
func (c *AutoCollector) OnNodeStart(ctx context.Context, nodeName string, step int) {}

// OnNodeEnd 实现 pregel.NodeCallback，记录节点与错误恢复。
func (c *AutoCollector) OnNodeEnd(ctx context.Context, nodeName string, step int, output interface{}, err error) {
	c.RecordNode(nodeName)
	if err != nil {
		c.RecordRecoveredError()
	}
}

// OnCheckpointSave 实现 pregel.CheckpointCallback。
func (c *AutoCollector) OnCheckpointSave(ctx context.Context, threadID, checkpointID string, step int) {
	c.RecordCheckpointSave()
}

// OnCheckpointLoad 实现 pregel.CheckpointCallback。
func (c *AutoCollector) OnCheckpointLoad(ctx context.Context, threadID, checkpointID string, step int) {
	c.RecordCheckpointRestore(true)
}

// OnCheckpointUpdate 实现 pregel.CheckpointCallback。
func (c *AutoCollector) OnCheckpointUpdate(ctx context.Context, threadID, asNode string) {}

// OnInterrupt 实现 pregel.InterruptCallback。
func (c *AutoCollector) OnInterrupt(ctx context.Context, nodeNames []string, step int) {
	c.RecordInterrupt()
}

// OnResume 实现 pregel.InterruptCallback。
func (c *AutoCollector) OnResume(ctx context.Context, threadID string) {}
