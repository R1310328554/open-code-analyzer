package metric

import (
	"time"

	"github.com/mitchellh/mapstructure"
	"github.com/prometheus/client_golang/prometheus"
	"github.com/prometheus/common/model"
)

// HistogramConfig 定义 histogram 观测值来源与自定义 bucket 边界。
type HistogramConfig struct {
	Value   *string   `mapstructure:"value"`
	Buckets []float64 `mapstructure:"buckets"`
}

// validateHistogramConfig 当前无额外约束，预留扩展校验入口。
func validateHistogramConfig(_ *HistogramConfig) error {
	return nil
}

// parseHistogramConfig 将 pipeline 配置解码为 HistogramConfig。
func parseHistogramConfig(config interface{}) (*HistogramConfig, error) {
	cfg := &HistogramConfig{}
	err := mapstructure.Decode(config, cfg)
	if err != nil {
		return nil, err
	}
	return cfg, nil
}

// Histograms 为每个日志流维护独立的 Histogram 指标向量。
type Histograms struct {
	*metricVec
	Cfg *HistogramConfig
}

// NewHistograms 创建带 bucket 配置与空闲过期策略的 Histogram 向量。
func NewHistograms(name, help string, config interface{}, maxIdleSec int64) (*Histograms, error) {
	cfg, err := parseHistogramConfig(config)
	if err != nil {
		return nil, err
	}
	err = validateHistogramConfig(cfg)
	if err != nil {
		return nil, err
	}
	return &Histograms{
		metricVec: newMetricVec(func(labels map[string]string) prometheus.Metric {
			return &expiringHistogram{prometheus.NewHistogram(prometheus.HistogramOpts{
				Help:        help,
				Name:        name,
				ConstLabels: labels,
				Buckets:     cfg.Buckets,
			}),
				0,
			}
		}, maxIdleSec),
		Cfg: cfg,
	}, nil
}

// With 根据流标签集返回对应的 Histogram 实例。
func (h *Histograms) With(labels model.LabelSet) prometheus.Histogram {
	return h.metricVec.With(labels).(prometheus.Histogram)
}

// expiringHistogram 包装 prometheus.Histogram，Observe 时刷新最后修改时间。
type expiringHistogram struct {
	prometheus.Histogram
	lastModSec int64
}

// Observe adds a single observation to the histogram.
func (h *expiringHistogram) Observe(val float64) {
	h.Histogram.Observe(val)
	h.lastModSec = time.Now().Unix()
}

// HasExpired 实现 Expirable：长时间无观测则可在 prune 时移除。
func (h *expiringHistogram) HasExpired(currentTimeSec int64, maxAgeSec int64) bool {
	return currentTimeSec-h.lastModSec >= maxAgeSec
}
