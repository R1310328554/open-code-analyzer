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

// STACKIT 服务发现指标适配器：实现 DiscovererMetrics 接口，
// 委托 RefreshMetricsInstantiator 记录刷新耗时与失败次数。

package stackit

import (
	"github.com/prometheus/prometheus/discovery"
)

var _ discovery.DiscovererMetrics = (*stackitMetrics)(nil)

type stackitMetrics struct {
	refreshMetrics discovery.RefreshMetricsInstantiator
}

// Register 无需额外注册，直接返回 nil。
// Register implements discovery.DiscovererMetrics.
func (*stackitMetrics) Register() error {
	return nil
}

// Unregister 为空操作，刷新指标由全局 RefreshMetrics 统一管理。
// Unregister implements discovery.DiscovererMetrics.
func (*stackitMetrics) Unregister() {}
