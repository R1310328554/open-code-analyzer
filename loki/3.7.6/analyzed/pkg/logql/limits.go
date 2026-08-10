package logql

// limits 定义 LogQL 查询引擎向租户配置查询上限的接口，含序列数、时间范围与超时等。

import (
	"context"
	"math"
	"time"

	"github.com/grafana/loki/v3/pkg/util/validation"
)

var NoLimits = &fakeLimits{
	maxSeries:               math.MaxInt32,
	timeout:                 time.Hour,
	multiVariantQueryEnable: false, // Multi-variant queries disabled by default
	maxScanTaskParallelism:  0,
}

// Limits 接口按 userID 查询各类配额与调试开关，供引擎在执行前校验查询。
// Limits allow the engine to fetch limits for a given users.
type Limits interface {
	MaxQuerySeries(context.Context, string) int
	MaxQueryRange(ctx context.Context, userID string) time.Duration
	QueryTimeout(context.Context, string) time.Duration
	BlockedQueries(context.Context, string) []*validation.BlockedQuery
	EnableMultiVariantQueries(string) bool

	// v2 engine limits
	MaxScanTaskParallelism(string) int
	DebugEngineTasks(string) bool
	DebugEngineStreams(string) bool
}

// fakeLimits 是 Limits 的内存实现，字段直接映射接口返回值，无外部依赖。
type fakeLimits struct {
	maxSeries               int
	timeout                 time.Duration
	blockedQueries          []*validation.BlockedQuery
	rangeLimit              time.Duration
	requiredLabels          []string
	multiVariantQueryEnable bool

	// v2 engine limits
	maxScanTaskParallelism int
	debugEngineTasks       bool
	debugEngineStreams     bool
}

// MaxQuerySeries 返回单条查询允许返回的最大序列数上限。
func (f fakeLimits) MaxQuerySeries(_ context.Context, _ string) int {
	return f.maxSeries
}

func (f fakeLimits) MaxQueryRange(_ context.Context, _ string) time.Duration {
	return f.rangeLimit
}

func (f fakeLimits) QueryTimeout(_ context.Context, _ string) time.Duration {
	return f.timeout
}

func (f fakeLimits) BlockedQueries(_ context.Context, _ string) []*validation.BlockedQuery {
	return f.blockedQueries
}

func (f fakeLimits) RequiredLabels(_ context.Context, _ string) []string {
	return f.requiredLabels
}

// EnableMultiVariantQueries 控制是否启用多变体查询扩展能力。
func (f fakeLimits) EnableMultiVariantQueries(_ string) bool {
	return f.multiVariantQueryEnable
}

func (f fakeLimits) MaxScanTaskParallelism(_ string) int {
	return f.maxScanTaskParallelism
}

func (f fakeLimits) DebugEngineTasks(_ string) bool {
	return f.debugEngineTasks
}

func (f fakeLimits) DebugEngineStreams(_ string) bool {
	return f.debugEngineStreams
}
// v2 引擎相关字段 MaxScanTaskParallelism 与 DebugEngineTasks/Streams 用于扫描并行度与调试输出。
