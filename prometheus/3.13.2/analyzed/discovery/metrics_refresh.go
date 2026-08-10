// Copyright The Prometheus Authors
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

// refresh 包通用 SD 指标定义：刷新失败计数、耗时 Summary 与 Histogram，
// 置于 discovery 包以避免 discovery 与 refresh 之间的循环依赖。

package discovery

import (
	"time"

	"github.com/prometheus/client_golang/prometheus"
)

// RefreshMetricsVecs 持有 refresh 机制共用的 CounterVec/SummaryVec/HistogramVec。
// RefreshMetricsVecs are metric vectors for the "refresh" package.
// We define them here in the "discovery" package in order to avoid a cyclic dependency between
// "discovery" and "refresh".
type RefreshMetricsVecs struct {
	failuresVec     *prometheus.CounterVec
	durationVec     *prometheus.SummaryVec
	durationHistVec *prometheus.HistogramVec

	metricRegisterer MetricRegisterer
}

var _ RefreshMetricsManager = (*RefreshMetricsVecs)(nil)

// 创建 refresh 指标向量，延迟注册直至首次记录数据。
func NewRefreshMetrics(reg prometheus.Registerer) RefreshMetricsManager {
	m := &RefreshMetricsVecs{
		failuresVec: prometheus.NewCounterVec(
			prometheus.CounterOpts{
				Name: "prometheus_sd_refresh_failures_total",
				Help: "Number of refresh failures for the given SD mechanism.",
			},
			[]string{"mechanism", "config"}),
		durationVec: prometheus.NewSummaryVec(
			prometheus.SummaryOpts{
				Name:       "prometheus_sd_refresh_duration_seconds",
				Help:       "The duration of a refresh in seconds for the given SD mechanism.",
				Objectives: map[float64]float64{0.5: 0.05, 0.9: 0.01, 0.99: 0.001},
			},
			[]string{"mechanism", "config"}),
		durationHistVec: prometheus.NewHistogramVec(
			prometheus.HistogramOpts{
				Name:                            "prometheus_sd_refresh_duration_histogram_seconds",
				Help:                            "The duration of a refresh for the given SD mechanism.",
				Buckets:                         []float64{.01, .1, 1, 10},
				NativeHistogramBucketFactor:     1.1,
				NativeHistogramMaxBucketNumber:  100,
				NativeHistogramMinResetDuration: 1 * time.Hour,
			},
			[]string{"mechanism"}),
	}

	// The reason we register metric vectors instead of metrics is so that
	// the metrics are not visible until they are recorded.
	m.metricRegisterer = NewMetricRegisterer(reg, []prometheus.Collector{
		m.failuresVec,
		m.durationVec,
		m.durationHistVec,
	})

	return m
}

// Instantiate returns metrics out of metric vectors for a given mechanism and config.
// 为指定 SD 机制与 scrape job 实例化具体 Counter/Summary/Histogram 指标。
func (m *RefreshMetricsVecs) Instantiate(mech, config string) *RefreshMetrics {
	return &RefreshMetrics{
		Failures:          m.failuresVec.WithLabelValues(mech, config),
		Duration:          m.durationVec.WithLabelValues(mech, config),
		DurationHistogram: m.durationHistVec.WithLabelValues(mech),
	}
}

// Register implements discovery.DiscovererMetrics.
func (m *RefreshMetricsVecs) Register() error {
	return m.metricRegisterer.RegisterMetrics()
}

// Unregister implements discovery.DiscovererMetrics.
func (m *RefreshMetricsVecs) Unregister() {
	m.metricRegisterer.UnregisterMetrics()
}

// 删除指定 mechanism/config 标签组合的 refresh 指标时序（scrape job 移除时调用）。
// DeleteLabelValues deletes refresh metrics for a specific mechanism and config. Smart to use this when a scrape job is removed.
func (m *RefreshMetricsVecs) DeleteLabelValues(mech, config string) {
	m.failuresVec.DeleteLabelValues(mech, config)
	m.durationVec.DeleteLabelValues(mech, config)
}
