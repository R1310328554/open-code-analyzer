package consumer

// metastore 模块向 Kafka metastore-events 主题发送 ObjectWritten 事件：
// 通知 metastore 新对象路径与最早日志时间，供索引更新。

import (
	"context"
	"fmt"
	"time"

	"github.com/twmb/franz-go/pkg/kgo"

	"github.com/grafana/loki/v3/pkg/dataobj/metastore"
)

// producer 接口抽象 Kafka 同步生产，便于测试 mock。
// A producer allows mocking of certain [kgo.Client] methods in tests.
type producer interface {
	ProduceSync(ctx context.Context, records ...*kgo.Record) kgo.ProduceResults
}

// metastoreEvents 按消费分区与 partitionRatio 计算目标 metastore 分区并发送事件。
// metastoreEvents emits events to the metastore Kafka topic.
type metastoreEvents struct {
	producer       producer
	partition      int32
	partitionRatio int32
}

// newMetastoreEvents 构造 metastore 事件发送器。
// newMetastoreEvents returns a new metastore events.
func newMetastoreEvents(partition int32, partitionRatio int32, producer producer) *metastoreEvents {
	return &metastoreEvents{
		producer:       producer,
		partition:      partition,
		partitionRatio: partitionRatio,
	}
}

// Emit 序列化 ObjectWrittenEvent 并通过 ProduceSync 写入 metastore 主题。
// Emit an event to the metastore Kafka topic.
func (m *metastoreEvents) Emit(ctx context.Context, objectPath string, earliestRecordTime time.Time) error {
	event := &metastore.ObjectWrittenEvent{
		ObjectPath:         objectPath,
		WriteTime:          time.Now().Format(time.RFC3339),
		EarliestRecordTime: earliestRecordTime.Format(time.RFC3339),
	}
	b, err := event.Marshal()
	if err != nil {
		return fmt.Errorf("failed to marshal event proto: %w", err)
	}
	// The metastore events partition is calculated based on the consumed partition
	// and the partition ratio. This has the effect of concentrating events within
	// fewer metastore partitions.
	partition := m.partition / m.partitionRatio
	res := m.producer.ProduceSync(ctx, &kgo.Record{
		Value:     b,
		Partition: partition,
	})
	return res.FirstErr()
}
