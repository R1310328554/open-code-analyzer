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

// Hetzner 服务发现指标占位：实现 DiscovererMetrics，刷新耗时由通用 refresh 指标提供。

package hetzner

import (
	"github.com/prometheus/prometheus/discovery"
)

var _ discovery.DiscovererMetrics = (*hetznerMetrics)(nil)

type hetznerMetrics struct {
	refreshMetrics discovery.RefreshMetricsInstantiator
}

// 注册指标（Hetzner 发现器无额外计数器）。
// Register implements discovery.DiscovererMetrics.
func (*hetznerMetrics) Register() error {
	return nil
}

// Unregister implements discovery.DiscovererMetrics.
func (*hetznerMetrics) Unregister() {}
