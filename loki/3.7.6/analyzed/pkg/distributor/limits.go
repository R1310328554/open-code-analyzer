package distributor

// Limits 接口：distributor 所需的租户级校验、分片、限流、OTLP 与 ingestion 阻断配置。

import (
	"time"

	"github.com/grafana/loki/v3/pkg/compactor/retention"
	"github.com/grafana/loki/v3/pkg/distributor/shardstreams"
	"github.com/grafana/loki/v3/pkg/loghttp/push"
)

// Limits 扩展 retention.Limits，涵盖行/标签/结构化元数据/流分片与 ingestion 策略等。
// Limits is an interface for distributor limits/related configs
type Limits interface {
	retention.Limits
	MaxLineSize(userID string) int
	MaxLineSizeTruncate(userID string) bool
	MaxLineSizeTruncateIdentifier(userID string) string

	MaxLabelNamesPerSeries(userID string) int
	MaxLabelNameLength(userID string) int
	MaxLabelValueLength(userID string) int

	CreationGracePeriod(userID string) time.Duration
	RejectOldSamples(userID string) bool
	RejectOldSamplesMaxAge(userID string) time.Duration

	IncrementDuplicateTimestamps(userID string) bool
	DiscoverServiceName(userID string) []string
	DiscoverGenericFields(userID string) map[string][]string
	DiscoverLogLevels(userID string) bool
	LogLevelFields(userID string) []string
	LogLevelFromJSONMaxDepth(userID string) int

// ShardStreams 返回流分片开关、期望速率与最大分片数等 shardstreams 配置。
	ShardStreams(userID string) shardstreams.Config
	IngestionRateStrategy() string
	IngestionRateBytes(userID string) float64
	IngestionBurstSizeBytes(userID string) int
	AllowStructuredMetadata(userID string) bool
	MaxStructuredMetadataSize(userID string) int
	MaxStructuredMetadataCount(userID string) int
	OTLPConfig(userID string) push.OTLPConfig

// BlockIngestionUntil/PolicyUntil 支持按租户或 policy 临时阻断写入。
	BlockIngestionUntil(userID string) time.Time
	BlockIngestionStatusCode(userID string) int
	BlockIngestionPolicyUntil(userID string, policy string) time.Time
	EnforcedLabels(userID string) []string
	PolicyEnforcedLabels(userID string, policy string) []string

	IngestionPartitionsTenantShardSize(userID string) int

	SimulatedPushLatency(userID string) time.Duration
}
// 实现通常来自 validation.Overrides，distributor 通过该接口解耦具体 limits 来源。
