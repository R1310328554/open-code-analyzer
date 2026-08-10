package index

// index 包 Prometheus 指标：Builder 侧 commit 与 processing delay，
// indexer 侧请求数、构建耗时、队列深度与端到端处理延迟。

import (
	"strconv"
	"sync"
	"time"

	"github.com/prometheus/client_golang/prometheus"
)

var (
	processingDelayDesc = prometheus.NewDesc(
		"loki_index_builder_latest_processing_delay_seconds",
		"Latest time difference between record timestamp and processing time in seconds",
		[]string{"partition"},
		nil,
	)
)

// processingDelayCollector 仅导出活跃分区的处理延迟，避免 partition 标签基数爆炸。
// processingDelayCollector implements prometheus.Collector to dynamically report
// processing delay only for active partitions, preventing cardinality explosion.
type processingDelayCollector struct {
	mtx    sync.RWMutex
	delays map[int32]float64 // partition -> delay in seconds
}

func newProcessingDelayCollector() *processingDelayCollector {
	return &processingDelayCollector{
		delays: make(map[int32]float64),
	}
}

// Describe implements prometheus.Collector.
func (c *processingDelayCollector) Describe(descs chan<- *prometheus.Desc) {
	descs <- processingDelayDesc
}

// Collect implements prometheus.Collector.
func (c *processingDelayCollector) Collect(metrics chan<- prometheus.Metric) {
	c.mtx.RLock()
	defer c.mtx.RUnlock()
	for partition, delay := range c.delays {
		metrics <- prometheus.MustNewConstMetric(
			processingDelayDesc,
			prometheus.GaugeValue,
			delay,
			strconv.Itoa(int(partition)),
		)
	}
}

func (c *processingDelayCollector) set(partition int32, delay float64) {
	c.mtx.Lock()
	defer c.mtx.Unlock()
	c.delays[partition] = delay
}

func (c *processingDelayCollector) delete(partition int32) {
	c.mtx.Lock()
	defer c.mtx.Unlock()
	delete(c.delays, partition)
}

// builderMetrics 跟踪 Kafka commit 成功/失败与各分区最新 processing delay。
type builderMetrics struct {
	// Error counters
	commitFailures prometheus.Counter

	// Request counters
	commitsTotal prometheus.Counter

	// Processing delay metrics
	processingDelay *processingDelayCollector
}

func newBuilderMetrics() *builderMetrics {
	p := &builderMetrics{
		commitFailures: prometheus.NewCounter(prometheus.CounterOpts{
			Name: "loki_index_builder_commit_failures_total",
			Help: "Total number of commit failures",
		}),
		commitsTotal: prometheus.NewCounter(prometheus.CounterOpts{
			Name: "loki_index_builder_commits_total",
			Help: "Total number of commits",
		}),
		processingDelay: newProcessingDelayCollector(),
	}

	return p
}

func (p *builderMetrics) register(reg prometheus.Registerer) error {
	collectors := []prometheus.Collector{
		p.commitFailures,
		p.commitsTotal,
		p.processingDelay,
	}

	for _, collector := range collectors {
		if err := reg.Register(collector); err != nil {
			if _, ok := err.(prometheus.AlreadyRegisteredError); !ok {
				return err
			}
		}
	}
	return nil
}

func (p *builderMetrics) unregister(reg prometheus.Registerer) {
	collectors := []prometheus.Collector{
		p.commitFailures,
		p.commitsTotal,
		p.processingDelay,
	}

	for _, collector := range collectors {
		reg.Unregister(collector)
	}
}

func (p *builderMetrics) incCommitFailures() {
	p.commitFailures.Inc()
}

func (p *builderMetrics) incCommitsTotal() {
	p.commitsTotal.Inc()
}

// setProcessingDelay 计算 record 写入时间与当前处理时间的差值并缓存。
func (p *builderMetrics) setProcessingDelay(partition int32, recordTimestamp time.Time) {
	if !recordTimestamp.IsZero() {
		p.processingDelay.set(partition, time.Since(recordTimestamp).Seconds())
	}
}

// deletePartitionMetrics 在分区 revoke 时移除对应 delay 条目，控制指标基数。
func (p *builderMetrics) deletePartitionMetrics(partition int32) {
	p.processingDelay.delete(partition)
}

// indexerMetrics 记录 build 请求吞吐、末次构建耗时与 ingest 端到端延迟。
type indexerMetrics struct {
	// Request counters
	totalRequests prometheus.Counter
	totalBuilds   prometheus.Counter

	// Build time metrics
	buildTimeSeconds prometheus.Gauge

	// Queue metrics
	queueDepth prometheus.Gauge

	// End-to-end processing time metric
	endToEndProcessingTime prometheus.Gauge
}

func newIndexerMetrics() *indexerMetrics {
	m := &indexerMetrics{
		totalRequests: prometheus.NewCounter(prometheus.CounterOpts{
			Name: "loki_index_builder_requests_total",
			Help: "Total number of build requests submitted to the indexer",
		}),
		totalBuilds: prometheus.NewCounter(prometheus.CounterOpts{
			Name: "loki_index_builder_builds_total",
			Help: "Total number of index builds completed",
		}),
		buildTimeSeconds: prometheus.NewGauge(prometheus.GaugeOpts{
			Name: "loki_index_builder_build_time_seconds",
			Help: "Time spent on the last index build in seconds",
		}),
		queueDepth: prometheus.NewGauge(prometheus.GaugeOpts{
			Name: "loki_index_builder_queue_depth",
			Help: "Current depth of the build request queue",
		}),
		endToEndProcessingTime: prometheus.NewGauge(prometheus.GaugeOpts{
			Name: "loki_ingest_end_to_end_processing_time_seconds",
			Help: "Time between a log line being written to kafka by the distributors and the index-builder making it available for querying in seconds",
		}),
	}

	return m
}

func (m *indexerMetrics) register(reg prometheus.Registerer) error {
	collectors := []prometheus.Collector{
		m.totalRequests,
		m.totalBuilds,
		m.buildTimeSeconds,
		m.queueDepth,
		m.endToEndProcessingTime,
	}

	for _, collector := range collectors {
		if err := reg.Register(collector); err != nil {
			if _, ok := err.(prometheus.AlreadyRegisteredError); !ok {
				return err
			}
		}
	}
	return nil
}

func (m *indexerMetrics) unregister(reg prometheus.Registerer) {
	collectors := []prometheus.Collector{
		m.totalRequests,
		m.totalBuilds,
		m.buildTimeSeconds,
		m.queueDepth,
		m.endToEndProcessingTime,
	}

	for _, collector := range collectors {
		reg.Unregister(collector)
	}
}

func (m *indexerMetrics) incRequests() {
	m.totalRequests.Inc()
}

func (m *indexerMetrics) incBuilds() {
	m.totalBuilds.Inc()
}

func (m *indexerMetrics) setBuildTime(duration time.Duration) {
	m.buildTimeSeconds.Set(duration.Seconds())
}

func (m *indexerMetrics) setQueueDepth(depth int) {
	m.queueDepth.Set(float64(depth))
}

func (m *indexerMetrics) setEndToEndProcessingTime(duration time.Duration) {
	m.endToEndProcessingTime.Set(duration.Seconds())
}
// 端到端处理时间衡量从 distributor 写 Kafka 到索引可查的完整 ingest 路径延迟。
