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

// ec2Metrics — AWS EC2 服务发现的 Prometheus 指标占位实现。

package aws

import (
	"github.com/prometheus/prometheus/discovery"
)

// ec2Metrics 持有刷新指标实例化器，供 EC2 服务发现复用。
type ec2Metrics struct {
	refreshMetrics discovery.RefreshMetricsInstantiator
}

var _ discovery.DiscovererMetrics = (*ec2Metrics)(nil)

// Register 实现 discovery.DiscovererMetrics，注册 Prometheus 指标（本实现为空操作）。
func (*ec2Metrics) Register() error {
	return nil
}

// Unregister 实现 discovery.DiscovererMetrics，注销已注册的指标。
func (*ec2Metrics) Unregister() {}
