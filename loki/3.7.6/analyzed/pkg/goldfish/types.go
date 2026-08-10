package goldfish

// types 定义 Goldfish A/B 查询对比的核心数据结构：采样记录、双单元性能统计、对比状态枚举及 API 过滤/统计类型。

import (
	"time"
)

// QuerySample 记录一次双单元查询的元数据、性能统计与对比结论（不含原始响应体）。
// QuerySample represents a sampled query with performance stats from both cells
type QuerySample struct {
	CorrelationID   string        `json:"correlationId"`
	TenantID        string        `json:"tenantId"`
	User            string        `json:"user"`
	Issuer          string        `json:"issuer"`
	IsLogsDrilldown bool          `json:"isLogsDrilldown"`
	Query           string        `json:"query"`
	QueryType       string        `json:"queryType"`
	StartTime       time.Time     `json:"startTime"`
	EndTime         time.Time     `json:"endTime"`
	Step            time.Duration `json:"step"`

// 仅存性能统计而非完整响应，降低敏感数据泄露风险。
	// Performance statistics instead of raw responses
	CellAStats QueryStats `json:"cellAStats"`
	CellBStats QueryStats `json:"cellBStats"`

	// Response metadata without sensitive content
	CellAResponseHash string `json:"cellAResponseHash"`
	CellBResponseHash string `json:"cellBResponseHash"`
	CellAResponseSize int64  `json:"cellAResponseSize"`
	CellBResponseSize int64  `json:"cellBResponseSize"`
	CellAStatusCode   int    `json:"cellAStatusCode"`
	CellBStatusCode   int    `json:"cellBStatusCode"`
	CellATraceID      string `json:"cellATraceID"`
	CellBTraceID      string `json:"cellBTraceID"`
	CellASpanID       string `json:"cellASpanID"`
	CellBSpanID       string `json:"cellBSpanID"`

	// Result storage metadata
	CellAResultURI         string `json:"cellAResultURI"`
	CellBResultURI         string `json:"cellBResultURI"`
	CellAResultSize        int64  `json:"cellAResultSize"`
	CellBResultSize        int64  `json:"cellBResultSize"`
	CellAResultCompression string `json:"cellAResultCompression"`
	CellBResultCompression string `json:"cellBResultCompression"`

	// Query engine version tracking
	CellAUsedNewEngine bool `json:"cellAUsedNewEngine"`
	CellBUsedNewEngine bool `json:"cellBUsedNewEngine"`

	// Comparison outcome
	ComparisonStatus     ComparisonStatus `json:"comparisonStatus"`
	MatchWithinTolerance bool             `json:"matchWithinTolerance"`
	MismatchCause        string           `json:"mismatchCause,omitempty"` // Set when ComparisonStatus is mismatch

	SampledAt time.Time `json:"sampledAt"`
}

// QueryStats 从查询响应头/统计中提取的执行时间、吞吐与分片信息。
// QueryStats contains extracted performance statistics
type QueryStats struct {
	ExecTimeMs           int64 `json:"execTimeMs"`           // Execution time in milliseconds
	QueueTimeMs          int64 `json:"queueTimeMs"`          // Queue time in milliseconds
	BytesProcessed       int64 `json:"bytesProcessed"`       // Total bytes processed
	LinesProcessed       int64 `json:"linesProcessed"`       // Total lines processed
	BytesPerSecond       int64 `json:"bytesPerSecond"`       // Bytes processed per second
	LinesPerSecond       int64 `json:"linesPerSecond"`       // Lines processed per second
	TotalEntriesReturned int64 `json:"totalEntriesReturned"` // Number of result entries
	Splits               int64 `json:"splits"`               // Number of splits
	Shards               int64 `json:"shards"`               // Number of shards
}

// ComparisonResult 保存 correlation 维度的对比状态、差异详情与性能比值。
// ComparisonResult represents the outcome of comparing two responses
type ComparisonResult struct {
	CorrelationID        string
	ComparisonStatus     ComparisonStatus
	MatchWithinTolerance bool
	MismatchCause        string
	DifferenceDetails    map[string]any
	PerformanceMetrics   PerformanceMetrics
	ComparedAt           time.Time
}

// ComparisonStatus 枚举 match/mismatch/error/partial/match_within_tolerance 等结果。
// ComparisonStatus represents the outcome of a comparison
type ComparisonStatus string

const (
	ComparisonStatusMatch                ComparisonStatus = "match"
	ComparisonStatusMismatch             ComparisonStatus = "mismatch"
	ComparisonStatusError                ComparisonStatus = "error"
	ComparisonStatusPartial              ComparisonStatus = "partial"
	ComparisonStatusMatchWithinTolerance ComparisonStatus = "match_within_tolerance"
)

// IsValid 校验 ComparisonStatus 是否为已知枚举值。
// IsValid checks if the ComparisonStatus value is valid
func (cs ComparisonStatus) IsValid() bool {
	switch cs {
	case ComparisonStatusMatch, ComparisonStatusMismatch, ComparisonStatusError, ComparisonStatusPartial:
		return true
	default:
		return false
	}
}

// PerformanceMetrics 对比双单元查询耗时与处理字节的比值。
// PerformanceMetrics contains performance comparison data
type PerformanceMetrics struct {
	CellAQueryTime  time.Duration
	CellBQueryTime  time.Duration
	QueryTimeRatio  float64
	CellABytesTotal int64
	CellBBytesTotal int64
	BytesRatio      float64
}

// QueryFilter 列表 API 的租户、用户、引擎版本及时间范围过滤条件。
// QueryFilter contains filters for querying sampled queries
type QueryFilter struct {
	Tenant           string
	User             string
	IsLogsDrilldown  *bool // pointer to handle true/false/nil states
	UsedNewEngine    *bool // pointer to handle true/false/nil states
	ComparisonStatus ComparisonStatus
	From, To         time.Time
}

// StatsFilter 统计 API 的时间范围及是否排除近期数据。
// StatsFilter contains filters for statistics queries
type StatsFilter struct {
	From           time.Time
	To             time.Time
	UsesRecentData bool // When false, exclude queries that touch data within the last 3h
}

// Statistics 聚合面板展示的执行量、覆盖率、匹配率与性能差异指标。
// Statistics contains aggregated statistics across sampled queries
type Statistics struct {
	QueriesExecuted       int64   `json:"queriesExecuted"`       // Count of queries executed with new engine
	EngineCoverage        float64 `json:"engineCoverage"`        // Ratio of queries using new engine
	MatchingQueries       float64 `json:"matchingQueries"`       // Ratio of queries with matching responses
	PerformanceDifference float64 `json:"performanceDifference"` // Geometric mean of performance ratio
}
// MismatchCause 仅在 ComparisonStatus 为 mismatch 时有意义。
