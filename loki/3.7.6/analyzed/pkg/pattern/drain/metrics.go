package drain

// drain 包 metrics 与 DetectLogFormat：记录模式检测/驱逐指标，并启发式识别 logfmt/JSON 格式。

import (
	"regexp"

	"github.com/prometheus/client_golang/prometheus"
)

const (
	FormatLogfmt  = "logfmt"
	FormatJSON    = "json"
	FormatUnknown = "unknown"
	TooFewTokens  = "too_few_tokens"
	TooManyTokens = "too_many_tokens"
	LineTooLong   = "line_too_long"
)

var logfmtRegex = regexp.MustCompile("^(\\w+?=([^\"]\\S*?|\".+?\") )*?(\\w+?=([^\"]\\S*?|\".+?\"))+$")

// DetectLogFormat 在新 stream 首行上判断 logfmt/JSON/unknown，仅运行一次。
// DetectLogFormat guesses at how the logs are encoded based on some simple heuristics.
// It only runs on the first log line when a new stream is created, so it could do some more complex parsing or regex.
func DetectLogFormat(line string) string {
	if len(line) < 2 {
		return FormatUnknown
	} else if line[0] == '{' && line[len(line)-1] == '}' {
		return FormatJSON
	} else if logfmtRegex.MatchString(line) {
		return FormatLogfmt
	}
	return FormatUnknown
}

// Metrics 聚合 PatternsEvicted/Pruned/Detected、LinesSkipped 与 token/state 分布直方图。
type Metrics struct {
	PatternsEvictedTotal  prometheus.Counter
	PatternsPrunedTotal   prometheus.Counter
	PatternsDetectedTotal prometheus.Counter
	LinesSkipped          *prometheus.CounterVec
	TokensPerLine         prometheus.Observer
	StatePerLine          prometheus.Observer
}
// FormatLogfmt/FormatJSON/TooFewTokens 等常量用作指标 label 与跳过原因分类。
