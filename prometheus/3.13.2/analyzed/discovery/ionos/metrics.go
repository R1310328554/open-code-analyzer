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

// IONOS 服务发现指标占位：实现 DiscovererMetrics，无额外 Prometheus 计数器。

package ionos

import (
	"github.com/prometheus/prometheus/discovery"
)

var _ discovery.DiscovererMetrics = (*ionosMetrics)(nil)

type ionosMetrics struct {
	refreshMetrics discovery.RefreshMetricsInstantiator
}

// 注册指标（IONOS 发现器当前为空实现）。
// Register implements discovery.DiscovererMetrics.
func (*ionosMetrics) Register() error {
	return nil
}

// Unregister implements discovery.DiscovererMetrics.
func (*ionosMetrics) Unregister() {}
