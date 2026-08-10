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

// Linode 服务发现内部指标：刷新失败计数 prometheus_sd_linode_failures_total。
// 同时持有 refresh 包通用指标实例化器，供 refresh.Discovery 记录耗时与失败。

package linode

import (
	"github.com/prometheus/client_golang/prometheus"

	"github.com/prometheus/prometheus/discovery"
)

var _ discovery.DiscovererMetrics = (*linodeMetrics)(nil)

// Linode SD 专用 DiscovererMetrics 实现。
type linodeMetrics struct {
	refreshMetrics discovery.RefreshMetricsInstantiator

	failuresCount prometheus.Counter

	metricRegisterer discovery.MetricRegisterer
}

// 注册 Linode SD 失败计数指标并绑定 refresh 通用指标实例化器。
func newDiscovererMetrics(reg prometheus.Registerer, rmi discovery.RefreshMetricsInstantiator) discovery.DiscovererMetrics {
	m := &linodeMetrics{
		refreshMetrics: rmi,
		failuresCount: prometheus.NewCounter(
			prometheus.CounterOpts{
				Name: "prometheus_sd_linode_failures_total",
				Help: "Number of Linode service discovery refresh failures.",
			}),
	}

	m.metricRegisterer = discovery.NewMetricRegisterer(reg, []prometheus.Collector{
		m.failuresCount,
	})

	return m
}

// Register 将 failuresCount 注册到 Prometheus registerer。
// Register implements discovery.DiscovererMetrics.
func (m *linodeMetrics) Register() error {
	return m.metricRegisterer.RegisterMetrics()
}

// Unregister 从 registerer 移除 Linode SD 失败计数指标。
// Unregister implements discovery.DiscovererMetrics.
func (m *linodeMetrics) Unregister() {
	m.metricRegisterer.UnregisterMetrics()
}
