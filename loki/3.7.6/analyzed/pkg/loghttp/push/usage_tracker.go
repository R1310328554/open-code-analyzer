package push

// usage_tracker 定义 ingest 用量追踪接口，供 limits/计费组件记录接收与丢弃字节。

import (
	"context"
	"time"

	"github.com/prometheus/prometheus/model/labels"
)

type UsageTracker interface {

// ReceivedBytesAdd 记录成功 ingest 的字节数，format 区分 loki/OTLP 等来源。
	// ReceivedBytesAdd records ingested bytes by tenant, retention period and labels.
	ReceivedBytesAdd(ctx context.Context, tenant string, retentionPeriod time.Duration, labels labels.Labels, value float64, format string)

// DiscardedBytesAdd 记录因限流等原因丢弃的字节，reason 说明丢弃原因码。
	// DiscardedBytesAdd records discarded bytes by tenant and labels.
	DiscardedBytesAdd(ctx context.Context, tenant, reason string, labels labels.Labels, value float64, format string)
}
// 实现可为 nil；push 解析路径在 tracker 非空且非内部流时才会调用 ReceivedBytesAdd。
