package uploader

// metrics 为 data object consumer 上传器暴露 Prometheus 计数、直方图与 gauge 指标。

import (
	"github.com/prometheus/client_golang/prometheus"
)

var (
	labelStatus   = "status"
	statusSuccess = "success"
	statusFailure = "failure"
)

type metrics struct {
	uploadFailures prometheus.Counter
	uploadTotal    prometheus.Counter
	uploadTime     prometheus.Histogram
	uploadSize     *prometheus.HistogramVec
	shaPrefixSize  prometheus.Gauge
}

// newMetrics 创建上传相关 collector 并初始化 sha_prefix_size gauge。
func newMetrics(shaPrefixSize int) *metrics {
	metrics := &metrics{
		uploadFailures: prometheus.NewCounter(prometheus.CounterOpts{
			Name: "loki_dataobj_consumer_upload_failures_total",
			Help: "Total number of failed uploads to object storage.",
		}),
		uploadTotal: prometheus.NewCounter(prometheus.CounterOpts{
			Name: "loki_dataobj_consumer_upload_total",
			Help: "Total number of uploads to object storage.",
		}),
		uploadTime: prometheus.NewHistogram(prometheus.HistogramOpts{
			Name:    "loki_dataobj_consumer_upload_time_seconds",
			Help:    "Time taken writing data objects to object storage.",
			Buckets: prometheus.ExponentialBuckets(1.0, 1.5, 10), // 1s, 1.5s, 2.25s, ... -> 57.67s
		}),
		uploadSize: prometheus.NewHistogramVec(prometheus.HistogramOpts{
			Name:    "loki_dataobj_consumer_upload_size_bytes",
			Help:    "Size of data objects uploaded to object storage in bytes grouped by status.",
			Buckets: prometheus.LinearBuckets(128<<20, 128<<20, 10), // 128MB, 256MB, ... -> 1280MB
		}, []string{labelStatus}),
		shaPrefixSize: prometheus.NewGauge(prometheus.GaugeOpts{
			Name: "loki_dataobj_consumer_sha_prefix_size",
			Help: "The size of the SHA prefix used for object storage keys.",
		}),
	}

	metrics.shaPrefixSize.Set(float64(shaPrefixSize))
	return metrics
}

// register 注册所有 collector，已注册时忽略 AlreadyRegisteredError。
func (m *metrics) register(reg prometheus.Registerer) error {
	collectors := []prometheus.Collector{
		m.uploadFailures,
		m.uploadTotal,
		m.uploadTime,
		m.uploadSize,
		m.shaPrefixSize,
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

// unregister 从 Registerer 移除所有上传指标 collector。
func (m *metrics) unregister(reg prometheus.Registerer) {
	collectors := []prometheus.Collector{
		m.uploadFailures,
		m.uploadTotal,
		m.uploadTime,
		m.uploadSize,
		m.shaPrefixSize,
	}

	for _, collector := range collectors {
		reg.Unregister(collector)
	}
}
// upload_size 直方图按 success/failure 标签分组记录上传对象字节大小。
