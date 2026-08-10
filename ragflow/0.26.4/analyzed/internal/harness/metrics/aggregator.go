package metrics

// aggregator.go — MetricsAggregator：多 trace 指标聚合与滑动时间窗口。


import (
	"math"
	"sort"
	"sync"
	"time"
)

// MetricsAggregator 跨多次 Agent 执行 trace 聚合指标。
type MetricsAggregator struct {
	mu      sync.Mutex
	metrics []*AgentMetrics
}

// NewMetricsAggregator 创建空聚合器。
func NewMetricsAggregator() *MetricsAggregator {
	return &MetricsAggregator{}
}

// Add 追加一条 AgentMetrics 快照。
func (a *MetricsAggregator) Add(m *AgentMetrics) {
	a.mu.Lock()
	defer a.mu.Unlock()
	a.metrics = append(a.metrics, m)
}

// AggregatedMetrics 多 trace 汇总统计（均值与分位数）。
type AggregatedMetrics struct {
	TotalTraces   int
	TotalDuration time.Duration

	// 工具调用指标（均值）
	AvgToolCalls       float64
	AvgToolSuccessRate float64
	AvgToolRetryRate   float64
	P50ToolLatencyMs   float64
	P95ToolLatencyMs   float64
	P99ToolLatencyMs   float64

	// 检查点指标
	AvgCheckpointSaves          float64
	AvgCheckpointRestores       float64
	AvgCheckpointRestoreSuccess float64

	// 执行步数/节点/中断指标
	AvgSteps           float64
	AvgNodesExecuted   float64
	AvgRecoveredErrors float64
	AvgInterrupts      float64

	// 成本与 fork replay 通过率
	AvgCostPerTask        float64
	AvgForkReplayPassRate float64
}

// Aggregate 计算均值并汇总工具延迟计算 P50/P95/P99。
func (a *MetricsAggregator) Aggregate() *AggregatedMetrics {
	a.mu.Lock()
	defer a.mu.Unlock()

	result := &AggregatedMetrics{
		TotalTraces: len(a.metrics),
	}

	if len(a.metrics) == 0 {
		return result
	}

	var allLatencies []float64

	for _, m := range a.metrics {
		result.TotalDuration += m.Duration
		result.AvgToolCalls += float64(m.ToolCalls)
		result.AvgToolSuccessRate += m.ToolSuccessRate
		result.AvgToolRetryRate += m.ToolRetryRate
		result.AvgCheckpointSaves += float64(m.CheckpointSaves)
		result.AvgCheckpointRestores += float64(m.CheckpointRestores)
		result.AvgCheckpointRestoreSuccess += m.CheckpointRestoreSuccess
		result.AvgSteps += float64(m.Steps)
		result.AvgNodesExecuted += float64(m.NodesExecuted)
		result.AvgRecoveredErrors += float64(m.RecoveredErrors)
		result.AvgInterrupts += float64(m.InterruptCount)
		result.AvgCostPerTask += m.CostPerTask
		result.AvgForkReplayPassRate += m.ForkReplayPassRate

		for _, latencies := range m.ToolLatencyMs {
			for _, l := range latencies {
				allLatencies = append(allLatencies, float64(l))
			}
		}
	}

	n := float64(len(a.metrics))
	result.AvgToolCalls /= n
	result.AvgToolSuccessRate /= n
	result.AvgToolRetryRate /= n
	result.AvgCheckpointSaves /= n
	result.AvgCheckpointRestores /= n
	result.AvgCheckpointRestoreSuccess /= n
	result.AvgSteps /= n
	result.AvgNodesExecuted /= n
	result.AvgRecoveredErrors /= n
	result.AvgInterrupts /= n
	result.AvgCostPerTask /= n
	result.AvgForkReplayPassRate /= n

	// Compute latency percentiles.
	if len(allLatencies) > 0 {
		sort.Float64s(allLatencies)
		result.P50ToolLatencyMs = percentile(allLatencies, 50)
		result.P95ToolLatencyMs = percentile(allLatencies, 95)
		result.P99ToolLatencyMs = percentile(allLatencies, 99)
	}

	return result
}

// Reset 清空已收集快照。
func (a *MetricsAggregator) Reset() {
	a.mu.Lock()
	defer a.mu.Unlock()
	a.metrics = nil
}

// MetricsWindow 滑动时间窗口内的指标跟踪。
type MetricsWindow struct {
	mu      sync.Mutex
	window  time.Duration
	entries []windowEntry
}

type windowEntry struct {
	timestamp time.Time
	metrics   *AgentMetrics
}

// NewMetricsWindow 创建指定窗口时长的 MetricsWindow。
func NewMetricsWindow(window time.Duration) *MetricsWindow {
	return &MetricsWindow{window: window}
}

// Add 以当前时间戳追加指标。
func (w *MetricsWindow) Add(m *AgentMetrics) {
	w.mu.Lock()
	defer w.mu.Unlock()
	w.entries = append(w.entries, windowEntry{
		timestamp: time.Now(),
		metrics:   m,
	})
	w.prune()
}

// Aggregate 修剪过期条目后聚合窗口内指标。
func (w *MetricsWindow) Aggregate() *AggregatedMetrics {
	w.mu.Lock()
	defer w.mu.Unlock()
	w.prune()

	agg := NewMetricsAggregator()
	for _, entry := range w.entries {
		agg.Add(entry.metrics)
	}
	return agg.Aggregate()
}

// prune 删除窗口外的 windowEntry。
func (w *MetricsWindow) prune() {
	cutoff := time.Now().Add(-w.window)
	keep := make([]windowEntry, 0, len(w.entries))
	for _, e := range w.entries {
		if e.timestamp.After(cutoff) {
			keep = append(keep, e)
		}
	}
	w.entries = keep
}

// percentile 从已排序切片计算 p 分位数。
func percentile(sorted []float64, p int) float64 {
	if len(sorted) == 0 {
		return 0
	}
	idx := int(math.Ceil(float64(p)/100.0*float64(len(sorted))) - 1)
	if idx < 0 {
		idx = 0
	}
	if idx >= len(sorted) {
		idx = len(sorted) - 1
	}
	return sorted[idx]
}

// 工具延迟从 AgentMetrics.ToolLatencyMs 扁平化后统一计算分位数。
