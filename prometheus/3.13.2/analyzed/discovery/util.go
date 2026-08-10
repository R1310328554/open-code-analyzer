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

// discovery 包通用指标注册辅助：MetricRegisterer 封装 Prometheus Collector的批量注册/注销，失败时回滚已注册项以保证可重入。

// discovery 包通用指标注册辅助：MetricRegisterer 封装 Prometheus Collector的批量注册/注销，失败时回滚已注册项以保证可重入。

package discovery

import (
	"fmt"

	"github.com/prometheus/client_golang/prometheus"
)

// MetricRegisterer 供需管理指标生命周期的 Discoverer 实现使用。
// MetricRegisterer is used by implementations of discovery.Discoverer that need
// to manage the lifetime of their metrics.
type MetricRegisterer interface {
	RegisterMetrics() error
	UnregisterMetrics()
}

// metricRegistererImpl is an implementation of MetricRegisterer.
// metricRegistererImpl 持有 Registerer 与待注册的 Collector 列表。
type metricRegistererImpl struct {
	reg     prometheus.Registerer
	metrics []prometheus.Collector
}

var _ MetricRegisterer = &metricRegistererImpl{}

// 在 NewDiscoverer 中创建 MetricRegisterer，传入待注册指标集合。
// NewMetricRegisterer creates an instance of a MetricRegisterer.
// Typically called inside the implementation of the NewDiscoverer() method.
func NewMetricRegisterer(reg prometheus.Registerer, metrics []prometheus.Collector) MetricRegisterer {
	return &metricRegistererImpl{
		reg:     reg,
		metrics: metrics,
	}
}

// RegisterMetrics 批量注册指标；任一失败则注销已注册项并返回错误。
// RegisterMetrics registers the metrics with a Prometheus registerer.
// If any metric fails to register, it will unregister all metrics that
// were registered so far, and return an error.
// Typically called at the start of the SD's Run() method.
func (rh *metricRegistererImpl) RegisterMetrics() error {
	for _, collector := range rh.metrics {
		err := rh.reg.Register(collector)
		if err != nil {
			// Unregister all metrics that were registered so far.
			// This is so that if RegisterMetrics() gets called again,
			// there will not be an error due to a duplicate registration.
// 注册失败时回滚，避免重复注册导致后续 RegisterMetrics 报错。
			rh.UnregisterMetrics()

			return fmt.Errorf("failed to register metric: %w", err)
		}
	}
	return nil
}

// UnregisterMetrics 从同一 Registerer 注销全部指标，通常在 Run 结束时 defer 调用。
// UnregisterMetrics unregisters the metrics from the same Prometheus
// registerer which was used to register them.
// Typically called at the end of the SD's Run() method by a defer statement.
func (rh *metricRegistererImpl) UnregisterMetrics() {
	for _, collector := range rh.metrics {
		rh.reg.Unregister(collector)
	}
}
