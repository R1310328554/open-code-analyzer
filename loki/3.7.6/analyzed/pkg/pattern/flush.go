package pattern

// pattern 包 flush 负责 ingester 级定期 sweep：清理过期 stream、关闭 flush 优先级队列。

import (
	"fmt"

	"github.com/go-kit/log/level"
	"github.com/prometheus/common/model"

	"github.com/grafana/loki/v3/pkg/util"
)

func (i *Ingester) initFlushQueues() {
	// i.flushQueuesDone.Add(i.cfg.ConcurrentFlushes)
	for j := 0; j < i.cfg.ConcurrentFlushes; j++ {
		i.flushQueues[j] = util.NewPriorityQueue(i.metrics.flushQueueLength)
		// for now we don't flush only prune old samples.
		// go i.flushLoop(j)
	}
}

// Flush 触发立即 sweep 并等待所有 flush 队列 drain 完成。
func (i *Ingester) Flush() {
	i.flush(true)
}

func (i *Ingester) flush(mayRemoveStreams bool) {
	i.sweepUsers(true, mayRemoveStreams)

	// Close the flush queues, to unblock waiting workers.
	for _, flushQueue := range i.flushQueues {
		flushQueue.Close()
	}

	i.flushQueuesDone.Wait()
	level.Debug(i.logger).Log("msg", "flush queues have drained")
}

// flushOp 实现 PriorityQueue 元素：按 userID/fp 键与 from 时间戳优先级排序。
type flushOp struct {
	from      model.Time
	userID    string
	fp        model.Fingerprint
	immediate bool
}

func (o *flushOp) Key() string {
	return fmt.Sprintf("%s-%s-%v", o.userID, o.fp, o.immediate)
}

func (o *flushOp) Priority() int64 {
	return -int64(o.from)
}

// sweepUsers 遍历所有租户 instance，调用 sweepInstance  prune 超 RetainFor 的 stream。
// sweepUsers periodically schedules series for flushing and garbage collects users with no series
func (i *Ingester) sweepUsers(immediate, mayRemoveStreams bool) {
	instances := i.getInstances()

	for _, instance := range instances {
		i.sweepInstance(instance, immediate, mayRemoveStreams)
	}
}

// sweepInstance 对每个 stream 调用 prune，满足条件时从 instance 索引中移除。
func (i *Ingester) sweepInstance(instance *instance, _, mayRemoveStreams bool) {
	level.Debug(i.logger).Log("msg", "sweeping instance", "instance", instance.instanceID)
	_ = instance.streams.ForEach(func(s *stream) (bool, error) {
		if mayRemoveStreams {
			instance.streams.WithLock(func() {
				if s.prune(i.cfg.RetainFor) {
					instance.removeStream(s)
				}
			})
		}
		return true, nil
	})
}
// flush(false) 在 shutdown 路径关闭队列并 Wait flushQueuesDone，确保后台 worker 退出。
