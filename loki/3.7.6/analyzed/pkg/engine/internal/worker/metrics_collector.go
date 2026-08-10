package worker

// collector 实现 prometheus.Collector，按 idle/ready/busy 状态统计 worker 线程数量。

import (
	"sync"

	"github.com/prometheus/client_golang/prometheus"
)

// collector 通过 setThreads 注入线程列表，Collect 时遍历各线程 State 聚合计数。
// collector implements [prometheus.Collector], collecting metrics for a
// worker.
type collector struct {
	threadsMut     sync.RWMutex
	collectThreads []*thread

	threads *prometheus.Desc
}

var _ prometheus.Collector = (*collector)(nil)

// newCollector 创建线程状态 Gauge 描述符；初始无线程时各状态计数为零。
// newCollector returns a new collector. The collector will report that the
// worker has no threads until calling setThreads.
func newCollector() *collector {
	return &collector{
		threads: prometheus.NewDesc(
			"loki_engine_worker_threads",
			"Number of worker threads by state",
			[]string{"state"},
			nil,
		),
	}
}

// setThreads 在 worker 启动线程池后更新待采集的 thread 切片。
// setThreads sets the threads to be collected.
func (c *collector) setThreads(threads []*thread) {
	c.threadsMut.Lock()
	defer c.threadsMut.Unlock()
	c.collectThreads = threads
}

func (c *collector) Collect(ch chan<- prometheus.Metric) {
	threadsByState := map[threadState]int{
		threadStateIdle:  0,
		threadStateReady: 0,
		threadStateBusy:  0,
	}

	c.threadsMut.RLock()
	defer c.threadsMut.RUnlock()

	for _, thread := range c.collectThreads {
		threadsByState[thread.State()]++
	}

	for state, count := range threadsByState {
		ch <- prometheus.MustNewConstMetric(c.threads, prometheus.GaugeValue, float64(count), state.String())
	}
}

// Describe 向 Prometheus 注册 loki_engine_worker_threads 指标元数据。
func (c *collector) Describe(ch chan<- *prometheus.Desc) {
	ch <- c.threads
}
// 线程状态与 thread.go 中 threadState 常量一一对应。
