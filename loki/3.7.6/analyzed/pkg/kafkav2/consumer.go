package kafkav2

// kafkav2 封装 franz-go 消费者：抽象轮询循环、消费组与单分区两种模式，
// 统一将 Record 写入下游 channel 并暴露 Prometheus 指标。

import (
	"context"
	"errors"
	"strconv"
	"time"

	"github.com/go-kit/log"
	"github.com/go-kit/log/level"
	"github.com/grafana/dskit/backoff"
	"github.com/grafana/dskit/services"
	"github.com/prometheus/client_golang/prometheus"
	"github.com/prometheus/client_golang/prometheus/promauto"
	"github.com/twmb/franz-go/pkg/kgo"
)

// abstractConsumer 是共享轮询逻辑的基础结构，嵌入 GroupConsumer 与 SinglePartitionConsumer。
// An abstractConsumer is a basic consumer that is embedded in most other
// consumers.
type abstractConsumer struct {
	*services.BasicService
	client      *kgo.Client
	records     chan<- *kgo.Record
	logger      log.Logger
	fetchErrors prometheus.Counter
	polls       prometheus.Counter
}

// run 持续 PollRecords，将拉取到的 Record 写入 channel；空轮询时指数退避。
func (c *abstractConsumer) run(ctx context.Context) error {
	defer close(c.records)
	b := backoff.New(ctx, backoff.Config{
		MinBackoff: time.Millisecond * 100,
		MaxBackoff: time.Second * 10,
		MaxRetries: 0, // Infinite retries.
	})
	for b.Ongoing() {
		c.polls.Inc()
		fetches := c.client.PollRecords(ctx, -1)
		// If the client is closed, or the context was canceled, exit the service.
		// We use Err0 instead of [kgo.IsClientClosed] so we can also check if the
		// context was canceled.
// 客户端关闭或 context 取消时静默退出，避免停止服务时被判定为失败。
		if err := fetches.Err0(); errors.Is(err, kgo.ErrClientClosed) || errors.Is(err, context.Canceled) {
			// We don't return ctx.Err() here as it manifests as a service failure
			// when stopping the service.
			return nil
		}
		// The client can fetch from multiple brokers in a single poll. This means
		// we must handle both records and errors at the same time, as some brokers
		// might be polled successfully while others return errors.
		var numRecords int
		for record := range fetches.RecordsAll() {
			// We must check for cancelation to avoid a deadlock. This can happen
			// if the receiver stopped without draining the chan.
// 写入 channel 前检查 ctx，防止接收方已停止导致 goroutine 死锁。
			select {
			case <-ctx.Done():
				// We don't return ctx.Err() here as it manifests as a service failure
				// when stopping the service.
				return nil
			case c.records <- record:
				numRecords++
			}
		}
		fetches.EachError(func(topic string, partition int32, err error) {
			level.Error(c.logger).Log("msg", "failed to poll fetches", "topic", topic, "partition", partition, "err", err)
			c.fetchErrors.Inc()
		})
		if numRecords == 0 {
			// If no records were fetched, backoff before the next poll.
			b.Wait()
		} else {
			// If records were fetched, reset the backoff before the next poll.
			b.Reset()
		}
	}
	return nil
}

// GroupConsumer 配合 consumer group 使用，分区分配由 broker 协调。
// A GroupConsumer consumes records from many partitions. It should be used
// with consumer groups, and is incompatible with direct consumers. The consumed
// partitions are determined by the broker.
type GroupConsumer struct {
	abstractConsumer
}

// NewGroupConsumer 注册 topic 标签的 fetch/poll 计数器并订阅指定 topic。
// NewGroupConsumer returns a new GroupConsumer. It expects a wrapped Prometheus
// registerer with a prefix (for example, "loki_service_name_") to avoid metric
// conflicts.
func NewGroupConsumer(
	client *kgo.Client,
	topic string,
	records chan<- *kgo.Record,
	logger log.Logger,
	r prometheus.Registerer,
) *GroupConsumer {
	// Wrap the registerer with labels for the topic so we don't need to add it for
	// each metric.
	r = prometheus.WrapRegistererWith(prometheus.Labels{"topic": topic}, r)
	client.AddConsumeTopics(topic)
	c := GroupConsumer{
		abstractConsumer: abstractConsumer{
			client:  client,
			records: records,
			logger:  log.With(logger, "topic", topic),
			fetchErrors: promauto.With(r).NewCounter(prometheus.CounterOpts{
				Name: "group_consumer_fetch_errors_total",
				Help: "The number of fetch errors.",
			}),
			polls: promauto.With(r).NewCounter(prometheus.CounterOpts{
				Name: "group_consumer_polls_total",
				Help: "Total number of polls.",
			}),
		},
	}
	c.BasicService = services.NewBasicService(c.starting, c.running, c.stopping)
	return &c
}

// starting implements [services.StartingFn].
func (c *GroupConsumer) starting(_ context.Context) error {
	return nil
}

// running implements [services.RunningFn].
func (c *GroupConsumer) running(ctx context.Context) error {
	return c.run(ctx)
}

// running implements [services.StoppingFn].
func (c *GroupConsumer) stopping(_ error) error {
	return nil
}

// SinglePartitionConsumer 用于 direct consumer，从固定分区与初始 offset 消费。
// A SinglePartitionConsumer consumes records from a single partition. It should
// be used with direct consumers, and is incompatible with consumer groups.
type SinglePartitionConsumer struct {
	abstractConsumer
	topic         string
	partition     int32
	initialOffset int64
}

// NewSinglePartitionConsumer 支持 -2（从头）与 -1（从尾）特殊 offset。
// NewSinglePartitionConsumer returns a new SinglePartitionConsumer. It
// consumes records from the specified offset. It accepts the two special
// offsets of -2 to consume from the start and -1 to consume from the end.
// It expects a wrapped Prometheus registerer with a prefix (for example,
// "loki_service_name_") to avoid metric conflicts.
func NewSinglePartitionConsumer(
	client *kgo.Client,
	topic string,
	partition int32,
	initialOffset int64,
	records chan<- *kgo.Record,
	logger log.Logger,
	r prometheus.Registerer,
) *SinglePartitionConsumer {
	// Wrap the registerer with labels for the topic and partition so we don't
	// need to add it to each metric.
	r = prometheus.WrapRegistererWith(prometheus.Labels{
		"topic":     topic,
		"partition": strconv.Itoa(int(partition)),
	}, r)
	c := SinglePartitionConsumer{
		abstractConsumer: abstractConsumer{
			client:  client,
			records: records,
			logger:  log.With(logger, "topic", topic, "partition", partition),
			fetchErrors: promauto.With(r).NewCounter(prometheus.CounterOpts{
				Name: "single_partition_consumer_fetch_errors_total",
				Help: "The number of fetch errors.",
			}),
			polls: promauto.With(r).NewCounter(prometheus.CounterOpts{
				Name: "single_partition_consumer_polls_total",
				Help: "Total number of polls.",
			}),
		},
		topic:         topic,
		partition:     partition,
		initialOffset: initialOffset,
	}
	c.BasicService = services.NewBasicService(c.starting, c.running, c.stopping)
	return &c
}

// starting implements [services.StartingFn].
func (c *SinglePartitionConsumer) starting(_ context.Context) error {
	// Consume the topic and partition from the specified offset.
// starting 阶段绑定 topic/partition 与初始 offset，服务启动前不可再改。
	c.client.AddConsumePartitions(map[string]map[int32]kgo.Offset{
		c.topic: {c.partition: kgo.NewOffset().At(c.initialOffset)},
	})
	return nil
}

// running implements [services.RunningFn].
func (c *SinglePartitionConsumer) running(ctx context.Context) error {
	return c.run(ctx)
}

// running implements [services.StoppingFn].
func (c *SinglePartitionConsumer) stopping(_ error) error {
	return nil
}

// GetInitialOffset 返回启动时将使用的分区起始 offset。
// GetInitialOffset returns the initial offset for the consumer.
func (c *SinglePartitionConsumer) GetInitialOffset() int64 {
	return c.initialOffset
}

// SetInitialOffset 仅允许在服务 New 状态下修改初始 offset。
// SetInitialOffset sets the initial offset for the consumer. It cannot
// be called after the service has started.
func (c *SinglePartitionConsumer) SetInitialOffset(offset int64) error {
	if c.State() != services.New {
		return errors.New("cannot set initial offset after service has started")
	}
	c.initialOffset = offset
	return nil
}
// 两种消费者均基于 dskit BasicService 生命周期，停止时自动关闭 records channel。
