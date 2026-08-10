package partition

// committer 异步/定时将已消费 offset 提交到 consumer group，并暴露 commit 请求量、延迟与最后提交 offset 指标。

import (
	"context"
	"strconv"
	"sync"
	"time"

	"github.com/go-kit/log"
	"github.com/go-kit/log/level"
	"github.com/prometheus/client_golang/prometheus"
	"github.com/prometheus/client_golang/prometheus/promauto"
	"go.uber.org/atomic"

	"github.com/grafana/loki/v3/pkg/kafka/client"
)

// Committer 抽象 offset 提交：Commit 立即写、EnqueueOffset 入队待定时提交。
// Committer defines an interface for committing offsets
type Committer interface {
	Commit(ctx context.Context, offset int64) error
	EnqueueOffset(offset int64)
}

// partitionCommitter offset 提交抽象接口。
type partitionCommitter struct {
	commitRequestsTotal   prometheus.Counter
	commitRequestsLatency prometheus.Histogram
	commitFailuresTotal   prometheus.Counter
	lastCommittedOffset   prometheus.Gauge

	logger        log.Logger
	partition     int32
	offsetManager OffsetManager
	commitFreq    time.Duration

	toCommit *atomic.Int64
	wg       sync.WaitGroup
	cancel   context.CancelFunc
}

// newCommitter 实现该路径上的核心处理逻辑。
func newCommitter(offsetManager OffsetManager, partition int32, commitFreq time.Duration, logger log.Logger, reg prometheus.Registerer) *partitionCommitter {
	c := &partitionCommitter{
		logger:        logger,
		offsetManager: offsetManager,
		partition:     partition,
		commitFreq:    commitFreq,
		commitRequestsTotal: promauto.With(reg).NewCounter(prometheus.CounterOpts{
			Namespace:   client.MetricsPrefix,
			Name:        "partition_reader_offset_commit_requests_total",
			Help:        "Total number of requests issued to commit the last consumed offset (includes both successful and failed requests).",
			ConstLabels: prometheus.Labels{"partition": strconv.Itoa(int(partition))},
		}),
		commitFailuresTotal: promauto.With(reg).NewCounter(prometheus.CounterOpts{
			Namespace:   client.MetricsPrefix,
			Name:        "partition_reader_offset_commit_failures_total",
			Help:        "Total number of failed requests to commit the last consumed offset.",
			ConstLabels: prometheus.Labels{"partition": strconv.Itoa(int(partition))},
		}),
		commitRequestsLatency: promauto.With(reg).NewHistogram(prometheus.HistogramOpts{
			Namespace:                       client.MetricsPrefix,
			Name:                            "partition_reader_offset_commit_request_duration_seconds",
			Help:                            "The duration of requests to commit the last consumed offset.",
			ConstLabels:                     prometheus.Labels{"partition": strconv.Itoa(int(partition))},
			NativeHistogramBucketFactor:     1.1,
			NativeHistogramMaxBucketNumber:  100,
			NativeHistogramMinResetDuration: time.Hour,
			Buckets:                         prometheus.DefBuckets,
		}),
		lastCommittedOffset: promauto.With(reg).NewGauge(prometheus.GaugeOpts{
			Namespace:   client.MetricsPrefix,
			Name:        "partition_reader_last_committed_offset",
			Help:        "The last consumed offset successfully committed by the partition reader. Set to -1 if not offset has been committed yet.",
			ConstLabels: prometheus.Labels{"partition": strconv.Itoa(int(partition))},
		}),
		toCommit: atomic.NewInt64(-1),
	}

	// Initialize the last committed offset metric to -1 to signal no offset has been committed yet
	c.lastCommittedOffset.Set(-1)

	if commitFreq > 0 {
		c.wg.Add(1)
		ctx, cancel := context.WithCancel(context.Background())
		c.cancel = cancel
		go c.autoCommitLoop(ctx)
	}

	return c
}

// autoCommitLoop 实现该路径上的核心处理逻辑。
func (c *partitionCommitter) autoCommitLoop(ctx context.Context) {
	defer c.wg.Done()
	commitTicker := time.NewTicker(c.commitFreq)
	defer commitTicker.Stop()

	previousOffset := c.toCommit.Load()
	for {
		select {
		case <-ctx.Done():
			return
		case <-commitTicker.C:
			currOffset := c.toCommit.Load()
			if currOffset == previousOffset {
				continue
			}

			if err := c.Commit(ctx, currOffset); err != nil {
				level.Error(c.logger).Log("msg", "failed to commit", "offset", currOffset, "err", err)
				continue
			}

			c.lastCommittedOffset.Set(float64(currOffset))
			previousOffset = currOffset
		}
	}
}

// 将待提交 offset 写入 committer 队列。
func (c *partitionCommitter) EnqueueOffset(o int64) {
	if c.commitFreq > 0 {
		c.toCommit.Store(o)
	}
}

// 将 offset 写入 consumer group。
func (c *partitionCommitter) Commit(ctx context.Context, offset int64) error {
	startTime := time.Now()
	c.commitRequestsTotal.Inc()

	if err := c.offsetManager.Commit(ctx, c.partition, offset); err != nil {
		level.Error(c.logger).Log("msg", "failed to commit offset", "err", err, "offset", offset)
		c.commitFailuresTotal.Inc()
		c.commitRequestsLatency.Observe(time.Since(startTime).Seconds())
		return err
	}

	level.Debug(c.logger).Log("msg", "successfully committed offset", "offset", offset)
	c.lastCommittedOffset.Set(float64(offset))
	c.commitRequestsLatency.Observe(time.Since(startTime).Seconds())
	return nil
}

// 停止自动提交循环并 flush 最终 offset。
func (c *partitionCommitter) Stop() {
	if c.commitFreq <= 0 {
		return
	}
	c.cancel()
	c.wg.Wait()

	offset := c.toCommit.Load()
	if offset < 0 {
		return
	}

	// Commit has internal timeouts, so this call shouldn't block for too long
	logger := log.With(c.logger, "msg", "stopping partition committer", "final_offset", offset)
	if err := c.Commit(context.Background(), offset); err != nil {
		level.Error(logger).Log("err", err)
	} else {
		level.Info(logger).Log()
	}
}
// Stop 时 flush 最终 toCommit，确保关停前 offset 尽力持久化。
