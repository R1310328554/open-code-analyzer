package consumer

// flush_committer 模块在刷写成功后提交 Kafka offset 并发送 metastore 事件：
// 保证对象落盘、索引可见与消费位点一致。

import (
	"context"
	"fmt"
	"time"

	"github.com/go-kit/log"
	"github.com/go-kit/log/level"
	"github.com/grafana/dskit/backoff"
	"github.com/prometheus/client_golang/prometheus"
	"github.com/prometheus/client_golang/prometheus/promauto"
)

// committer 接口抽象 offset 提交，便于测试 mock kgo.Client。
// A committer allows mocking of certain [kgo.Client] methods in tests.
type committer interface {
	Commit(ctx context.Context, partition int32, offset int64) error
}

// metastoreEventEmitter 接口抽象 metastore 事件发送。
// A metastoreEventEmitter allows mocking of [metastoreEvents] in tests.
type metastoreEventEmitter interface {
	Emit(ctx context.Context, objectPath string, earliestRecordTime time.Time) error
}

// flusher 接口抽象 data object 刷写操作。
// A flusher allows mocking of flushes in tests.
type flusher interface {
	Flush(ctx context.Context, builder builder, reason string) (string, error)
}

// flushCommitterImpl 串联 flusher、metastore 事件与 offset commit。
// A flushCommitterImpl manages the flushing of data objects and commits.
type flushCommitterImpl struct {
	flusher         flusher
	metastoreEvents metastoreEventEmitter
	committer       committer
	partition       int32
	logger          log.Logger

	// Metrics.
	commits        prometheus.Counter
	commitFailures prometheus.Counter
}

func newFlushCommitter(
	flusher flusher,
	metastoreEvents metastoreEventEmitter,
	committer committer,
	partition int32,
	logger log.Logger,
	r prometheus.Registerer,
) *flushCommitterImpl {
	return &flushCommitterImpl{
		flusher:         flusher,
		metastoreEvents: metastoreEvents,
		committer:       committer,
		partition:       partition,
		logger:          logger,
		commits: promauto.With(r).NewCounter(prometheus.CounterOpts{
			Name: "loki_dataobj_consumer_commits_total",
			Help: "Total number of commits.",
		}),
		commitFailures: promauto.With(r).NewCounter(prometheus.CounterOpts{
			Name: "loki_dataobj_consumer_commit_failures_total",
			Help: "Total number of commit failures.",
		}),
	}
}

// Flush 刷写 builder，成功后发 metastore 事件并提交 offset。
// Flush the data object builder and, if successful, commit the offset.
func (c *flushCommitterImpl) Flush(ctx context.Context, builder builder, reason string, offset int64, earliestRecordTime time.Time) error {
	objectPath, err := c.flusher.Flush(ctx, builder, reason)
	if err != nil {
		return fmt.Errorf("failed to flush data object: %w", err)
	}
	if err := c.emitEvent(ctx, objectPath, earliestRecordTime); err != nil {
		return fmt.Errorf("failed to emit metastore event: %w", err)
	}
	if err := c.commit(ctx, offset); err != nil {
		c.commitFailures.Inc()
		return fmt.Errorf("failed to commit data object: %w", err)
	}
	return nil
}

// emitEvent 向 metastore 发送 ObjectWritten 事件，失败时指数退避重试。
// emitEvent emits a metastore event for the object, retries with exponential
// backoff until successful or the context is canceled.
func (c *flushCommitterImpl) emitEvent(ctx context.Context, objectPath string, earliestRecordTime time.Time) error {
	b := backoff.New(ctx, backoff.Config{
		MinBackoff: 100 * time.Millisecond,
		MaxBackoff: 10 * time.Second,
		MaxRetries: 0,
	})
	var lastErr error
	for b.Ongoing() {
		lastErr = c.metastoreEvents.Emit(ctx, objectPath, earliestRecordTime)
		if lastErr == nil {
			break
		}
		level.Warn(c.logger).Log("msg", "failed to emit metastore event", "err", lastErr, "attempt", b.NumRetries())
		b.Wait()
	}
	return lastErr
}

// commit 提交分区 offset，失败时指数退避直至成功或 context 取消。
// commits the offset, retries with exponential backoff until successful or
// the context is canceled.
func (c *flushCommitterImpl) commit(ctx context.Context, offset int64) error {
	b := backoff.New(ctx, backoff.Config{
		MinBackoff: 100 * time.Millisecond,
		MaxBackoff: 10 * time.Second,
		MaxRetries: 0,
	})
	c.commits.Inc()
	var lastErr error
	for b.Ongoing() {
		lastErr = c.committer.Commit(ctx, c.partition, offset)
		if lastErr == nil {
			level.Debug(c.logger).Log("msg", "committed offset", "partition", c.partition, "offset", offset)
			break
		}
		level.Warn(c.logger).Log("msg", "failed to commit offset", "err", lastErr, "attempt", b.NumRetries())
		b.Wait()
	}
	return lastErr
}
