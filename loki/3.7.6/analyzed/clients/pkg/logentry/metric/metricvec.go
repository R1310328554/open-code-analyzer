package metric

import (
	"strings"
	"sync"
	"time"

	"github.com/grafana/loki/v3/pkg/util"

	"github.com/prometheus/client_golang/prometheus"
	"github.com/prometheus/common/model"
)

// Expirable 表示可依据当前时间与 maxAge 判断是否已过期的指标。
type Expirable interface {
	HasExpired(currentTimeSec int64, maxAgeSec int64) bool
}

// metricVec 按流指纹缓存 Prometheus 指标，支持空闲过期与标签清洗。
type metricVec struct {
	factory   func(labels map[string]string) prometheus.Metric
	mtx       sync.Mutex
	metrics   map[model.Fingerprint]prometheus.Metric
	maxAgeSec int64
}

// newMetricVec 构造 metricVec，factory 负责为每组 const labels 创建具体指标。
func newMetricVec(factory func(labels map[string]string) prometheus.Metric, maxAgeSec int64) *metricVec {
	return &metricVec{
		metrics:   map[model.Fingerprint]prometheus.Metric{},
		factory:   factory,
		maxAgeSec: maxAgeSec,
	}
}

// Describe implements prometheus.Collector and doesn't declare any metrics on purpose to bypass prometheus validation.
// see https://godoc.org/github.com/prometheus/client_golang/prometheus#hdr-Custom_Collectors_and_constant_Metrics search for "unchecked"
func (c *metricVec) Describe(_ chan<- *prometheus.Desc) {}

// Collect 导出当前缓存的全部指标，并在持有锁的情况下触发 prune。
func (c *metricVec) Collect(ch chan<- prometheus.Metric) {
	c.mtx.Lock()
	defer c.mtx.Unlock()
	for _, m := range c.metrics {
		ch <- m
	}
	c.prune()
}

// With 按标签指纹查找或懒创建指标，写入前会清洗非法/reserved 标签名。
func (c *metricVec) With(labels model.LabelSet) prometheus.Metric {
	c.mtx.Lock()
	defer c.mtx.Unlock()
	fp := labels.Fingerprint()
	var ok bool
	var metric prometheus.Metric
	if metric, ok = c.metrics[fp]; !ok {
		metric = c.factory(util.ModelLabelSetToMap(cleanLabels(labels)))
		c.metrics[fp] = metric
	}
	return metric
}

// cleanLabels 移除非法 UTF-8 标签名及以 __ 开头的 Prometheus 保留前缀。
func cleanLabels(set model.LabelSet) model.LabelSet {
	out := make(model.LabelSet, len(set))
	for k, v := range set {
		// Performing the same label validity check the prometheus go client library does.
		// https://github.com/prometheus/client_golang/blob/618194de6ad3db637313666104533639011b470d/prometheus/labels.go#L85
		if !model.UTF8Validation.IsValidLabelName(string(k)) || strings.HasPrefix(string(k), "__") {
			continue
		}
		out[k] = v
	}
	return out
}

// Delete 按标签指纹删除单个缓存指标，存在则返回 true。
func (c *metricVec) Delete(labels model.LabelSet) bool {
	c.mtx.Lock()
	defer c.mtx.Unlock()
	fp := labels.Fingerprint()
	_, ok := c.metrics[fp]
	if ok {
		delete(c.metrics, fp)
	}
	return ok
}

// DeleteAll 清空全部缓存指标（通常在配置重载时使用）。
func (c *metricVec) DeleteAll() {
	c.mtx.Lock()
	defer c.mtx.Unlock()
	c.metrics = map[model.Fingerprint]prometheus.Metric{}
}

// prune 移除实现 Expirable 且已超过 maxAgeSec 的指标；调用方需已持有 metrics 锁。
func (c *metricVec) prune() {
	currentTimeSec := time.Now().Unix()
	for fp, m := range c.metrics {
		if em, ok := m.(Expirable); ok {
			if em.HasExpired(currentTimeSec, c.maxAgeSec) {
				delete(c.metrics, fp)
			}
		}
	}
}
