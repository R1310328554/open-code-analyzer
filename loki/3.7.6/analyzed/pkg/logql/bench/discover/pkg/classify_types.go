package discover

// 分类阶段类型定义：ClassifyAPI 接口与 ClassifyResult 倒排索引结构。

import (
	"time"

	"github.com/grafana/loki/v3/pkg/loghttp"
	bench "github.com/grafana/loki/v3/pkg/logql/bench"
)

// ClassifyAPI 抽象 detected_fields 调用，便于单元测试注入 mock。
// ClassifyAPI is the subset of the Loki client interface required by
// RunClassification. Abstracting makes testing simpler.
type ClassifyAPI interface {
	GetDetectedFields(
		queryStr string,
		limit int,
		start, end time.Time,
	) (*loghttp.DetectedFieldsResponse, error)
}

// ClassifyResult 各 By* 映射将属性值关联到拥有该属性的 canonical selector 列表。
// ClassifyResult holds the output of a completed RunClassification invocation.
// Each inverted index maps a property to the list of canonical stream selectors
// that have that property.
type ClassifyResult struct {
	// ByFormat maps log format to the list of selectors with that format.
	ByFormat map[bench.LogFormat][]string

	// ByUnwrappableField maps field name to the list of selectors that expose
	// that field as a numeric value usable with | unwrap.
	ByUnwrappableField map[string][]string

	// ByDetectedField maps field name to the list of selectors that expose that
	// field via a parser (json or logfmt).
	ByDetectedField map[string][]string

	// ByStructuredMetadata maps metadata key to the list of selectors that
	// carry that key as structured metadata (not as an indexed stream label).
	ByStructuredMetadata map[string][]string

	// ByLabelKey maps label key name to the list of selectors that use that
	// key as an indexed stream label.
	ByLabelKey map[string][]string

	// FormatBySelector maps each canonical selector to its detected log format.
	FormatBySelector map[string]bench.LogFormat

	// Warnings accumulates non-fatal issues encountered during classification,
	// such as per-stream API errors that caused a stream to be skipped.
	Warnings []string

	// TotalClassified is the number of streams successfully classified.
	TotalClassified int

	// TotalSkipped is the number of streams skipped due to API errors.
	TotalSkipped int
}
// TotalSkipped 统计 API 错误导致跳过的流数；Warnings 记录非致命问题。
