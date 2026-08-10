package metrics

// exporter.go — Exporter：轻量 Prometheus 文本/CSV 格式指标导出。


import (
	"fmt"
	"strings"
)

// Exporter 格式化 Agent 指标输出，轻量替代完整 Prometheus client。
// 避免引入 prometheus/client_golang 依赖。
type Exporter struct {
	namespace string
}

// NewExporter 创建带 namespace 前缀的导出器。
func NewExporter(namespace string) *Exporter {
	return &Exporter{namespace: namespace}
}

// ExportText 输出 Prometheus exposition 风格文本（HELP/TYPE/样本行）。
func (e *Exporter) ExportText(m *AgentMetrics) string {
	snap := m.Snapshot()
	var b strings.Builder

	ns := e.namespace
	if ns != "" {
		ns += "_"
	}

	// Tool metrics.
	fmt.Fprintf(&b, "# HELP %stool_calls_total Total tool invocations\n", ns)
	fmt.Fprintf(&b, "# TYPE %stool_calls_total counter\n", ns)
	fmt.Fprintf(&b, "%stool_calls_total %d\n", ns, snap.ToolCalls)

	fmt.Fprintf(&b, "# HELP %stool_success_rate Tool success rate\n", ns)
	fmt.Fprintf(&b, "# TYPE %stool_success_rate gauge\n", ns)
	fmt.Fprintf(&b, "%stool_success_rate %.4f\n", ns, snap.ToolSuccessRate)

	fmt.Fprintf(&b, "# HELP %stool_retry_rate Tool retry rate\n", ns)
	fmt.Fprintf(&b, "# TYPE %stool_retry_rate gauge\n", ns)
	fmt.Fprintf(&b, "%stool_retry_rate %.4f\n", ns, snap.ToolRetryRate)

	// Checkpoint metrics.
	fmt.Fprintf(&b, "# HELP %scheckpoint_saves_total Total checkpoint saves\n", ns)
	fmt.Fprintf(&b, "# TYPE %scheckpoint_saves_total counter\n", ns)
	fmt.Fprintf(&b, "%scheckpoint_saves_total %d\n", ns, snap.CheckpointSaves)

	fmt.Fprintf(&b, "# HELP %scheckpoint_restore_success Checkpoint restore success rate\n", ns)
	fmt.Fprintf(&b, "# TYPE %scheckpoint_restore_success gauge\n", ns)
	fmt.Fprintf(&b, "%scheckpoint_restore_success %.4f\n", ns, snap.CheckpointRestoreSuccess)

	// Execution metrics.
	fmt.Fprintf(&b, "# HELP %ssteps_total Total supersteps executed\n", ns)
	fmt.Fprintf(&b, "# TYPE %ssteps_total counter\n", ns)
	fmt.Fprintf(&b, "%ssteps_total %d\n", ns, snap.Steps)

	fmt.Fprintf(&b, "# HELP %snodes_executed_total Total nodes executed\n", ns)
	fmt.Fprintf(&b, "# TYPE %snodes_executed_total counter\n", ns)
	fmt.Fprintf(&b, "%snodes_executed_total %d\n", ns, snap.NodesExecuted)

	fmt.Fprintf(&b, "# HELP %sinterrupts_total Total interrupts\n", ns)
	fmt.Fprintf(&b, "# TYPE %sinterrupts_total counter\n", ns)
	fmt.Fprintf(&b, "%sinterrupts_total %d\n", ns, snap.InterruptCount)

	return b.String()
}

// ExportCSV 输出单行 CSV（trace_id 与核心计数/比率字段）。
func (e *Exporter) ExportCSV(m *AgentMetrics) string {
	snap := m.Snapshot()
	return fmt.Sprintf("%s,%d,%d,%d,%.4f,%.4f,%d,%d,%.4f,%d,%d,%d,%.6f",
		snap.TraceID,
		snap.ToolCalls, snap.ToolSuccesses, snap.ToolFailures,
		snap.ToolSuccessRate, snap.ToolRetryRate,
		snap.CheckpointSaves, snap.CheckpointRestores,
		snap.CheckpointRestoreSuccess,
		snap.Steps, snap.NodesExecuted, snap.InterruptCount,
		snap.CostPerTask)
}

// 指标名形如 {namespace}_tool_calls_total；Snapshot 保证并发安全读取。
