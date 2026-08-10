// 投机解码统计：draft/accept 计数、深度分桶与结束日志。
package mlxrunner

import (
	"context"
	"fmt"
	"log/slog"
	"strings"
)

// specStats 累积迭代次数、draft/accept 与每轮 chosen 深度。
type specStats struct {
	iterations int
	drafted    int
	accepted   int
	maxDraft   int
	// chosen 记录每轮选择的 draft 深度，用于 depthOverTime 分桶。
	// chosen is the draft depth picked each round, in order; split into time
	// buckets it distinguishes a ramp that holds from one that thrashes shallow.
	chosen []int
}

// recordRound 追加本轮 chosen 深度。
func (s *specStats) recordRound(depth int) {
	s.chosen = append(s.chosen, depth)
}

// depthBuckets 为 depthOverTime 均分时间桶数量。
// depthBuckets is how many equal time slices depthOverTime splits a run into.
const depthBuckets = 8

// depthOverTime 输出各时间桶 mean/max 深度，如 "0.3/1 2.1/3"。
// depthOverTime reports per-bucket mean/max chosen depth across up to depthBuckets
// equal time buckets, e.g. "0.3/1 2.1/3 4.8/5 5.0/6".
// depthOverTime 计算分桶 mean/max 深度字符串。
func (s *specStats) depthOverTime() string {
	if len(s.chosen) == 0 {
		return ""
	}
	buckets := min(depthBuckets, len(s.chosen))
	parts := make([]string, 0, buckets)
	for b := range buckets {
		lo := b * len(s.chosen) / buckets
		hi := (b + 1) * len(s.chosen) / buckets
		sum, mx := 0, 0
		for _, d := range s.chosen[lo:hi] {
			sum += d
			mx = max(mx, d)
		}
		parts = append(parts, fmt.Sprintf("%.1f/%d", float64(sum)/float64(hi-lo), mx))
	}
	return strings.Join(parts, " ")
}

// logStats 输出投机解码汇总与 debug 深度控制器信息。
func (s *speculationSession) logStats() {
	if !s.enabled {
		return
	}
	acceptance := 0.0
	if s.stats.drafted > 0 {
		acceptance = float64(s.stats.accepted) / float64(s.stats.drafted)
	}
	avgDraft := 0.0
	avgAccepted := 0.0
	if s.stats.iterations > 0 {
		avgDraft = float64(s.stats.drafted) / float64(s.stats.iterations)
		avgAccepted = float64(s.stats.accepted) / float64(s.stats.iterations)
	}
	slog.Info("speculative decode stats", "iterations", s.stats.iterations, "drafted", s.stats.drafted, "accepted", s.stats.accepted, "acceptance", fmt.Sprintf("%.2f", acceptance), "avg_draft", fmt.Sprintf("%.2f", avgDraft), "max_draft", s.stats.maxDraft, "avg_accepted", fmt.Sprintf("%.2f", avgAccepted), "depth_over_time", s.stats.depthOverTime())

	if !slog.Default().Enabled(context.TODO(), slog.LevelDebug) {
		return
	}

	// Debug 日志输出 trusted 位置接受率与期望 TPS。
	// Log learned acceptance over the trusted positions [1, frontier] and
	// expected throughput over the searched window [0, limit]; deeper
	// depths have no data of their own.
	d := s.spec.depth
	frontier := d.frontier()
	rates := make([]string, 0, frontier)
	for n := 1; n <= frontier; n++ {
		rates = append(rates, fmt.Sprintf("%d:%.2f", n, d.acc.acceptance(n)))
	}
	limit := d.limit()
	tps := make([]string, 0, limit+1)
	if d.cost.ready() {
		for n := 0; n <= limit; n++ {
			tps = append(tps, fmt.Sprintf("%d:%.1f", n, 1000*d.acc.expectedCommitted(n)/d.cost.cost(n)))
		}
	}
	slog.Debug("speculation depth controller", "cost", d.cost.sampleString(), "acceptance", strings.Join(rates, " "), "expected_tps", strings.Join(tps, " "), "probe_interval", d.probeInterval)
}
