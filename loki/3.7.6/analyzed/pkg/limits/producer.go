package limits

// producer 将 StreamMetadataRecord 异步写入 Kafka metadata topic，
// 实现跨 zone/实例的流状态复制与 crash 后恢复。

import (
	"context"
	"fmt"

	"github.com/go-kit/log"
	"github.com/go-kit/log/level"
	"github.com/prometheus/client_golang/prometheus"
	"github.com/prometheus/client_golang/prometheus/promauto"
	"github.com/twmb/franz-go/pkg/kgo"

	"github.com/grafana/loki/v3/pkg/limits/proto"
)

// kafkaProducer 抽象 Produce 回调，便于单测注入假 Kafka 客户端。
// kafkaProducer allows mocking of certain [kgo.Client] methods in tests.
type kafkaProducer interface {
	Produce(context.Context, *kgo.Record, func(*kgo.Record, error))
}

// producer 按 streamHash%numPartitions 分片写入，Key 为 tenant ID。
// producer produces records on the metadata topic. It is how state is
// replicated across zones and recovered following a crash or restart.
type producer struct {
	client kafkaProducer
	// TODO(grobinson): We should remove topic in future, as it should be
	// set in the client.
	topic         string
	numPartitions int
	zone          string
	logger        log.Logger

	produced       prometheus.Counter
	producedFailed prometheus.Counter
}

// newProducer returns a new Sender.
func newProducer(client kafkaProducer, topic string, numPartitions int, zone string, logger log.Logger, reg prometheus.Registerer) *producer {
	return &producer{
		client:        client,
		topic:         topic,
		numPartitions: numPartitions,
		zone:          zone,
		logger:        logger,
		produced: promauto.With(reg).NewCounter(
			prometheus.CounterOpts{
				Name: "loki_ingest_limits_records_produced_total",
				Help: "The total number of produced records, including failures.",
			},
		),
		producedFailed: promauto.With(reg).NewCounter(
			prometheus.CounterOpts{
				Name: "loki_ingest_limits_records_produced_failed_total",
				Help: "The total number of failed produced records.",
			},
		),
	}
}

// Produce 序列化 metadata 后 fire-and-forget 推送，错误在回调中记录。
// Produce encodes the metadata in a [proto.StreamMetadataRecord] record
// and pushes it to the metadata topic. It does not wait for the push to
// complete.
func (p *producer) Produce(ctx context.Context, tenant string, metadata *proto.StreamMetadata) error {
	v := proto.StreamMetadataRecord{
		Zone:     p.zone,
		Tenant:   tenant,
		Metadata: metadata,
	}
	b, err := v.Marshal()
	if err != nil {
		return fmt.Errorf("failed to marshal proto: %w", err)
	}
	// The stream metadata topic expects a fixed number of partitions,
	// the size of which is determined ahead of time. Streams are
	// sharded over partitions using a simple mod.
// 分区选择与 consumer/distributor 侧 hash 路由保持一致。
	partition := int32(metadata.StreamHash % uint64(p.numPartitions))
	r := kgo.Record{
		Key:       []byte(tenant),
		Value:     b,
		Topic:     p.topic,
		Partition: partition,
	}
	p.produced.Inc()
	p.client.Produce(context.WithoutCancel(ctx), &r, p.handleProduceErr)
	return nil
}

func (p *producer) handleProduceErr(_ *kgo.Record, err error) {
	if err != nil {
		level.Error(p.logger).Log("msg", "failed to produce record", "err", err.Error())
		p.producedFailed.Inc()
	}
}
// produced/producedFailed 计数器分别跟踪成功与失败的 metadata 写入次数。
