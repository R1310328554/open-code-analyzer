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

// OpenStack hypervisor 角色发现：通过 Nova Compute API 列出物理/虚拟 hypervisor，以 HostIP 与配置 port 作为抓取地址。

// OpenStack hypervisor 角色发现：通过 Nova Compute API 列出物理/虚拟 hypervisor，以 HostIP 与配置 port 作为抓取地址。

// OpenStack hypervisor 角色发现：通过 Nova Compute API 列出物理/虚拟 hypervisor，以 HostIP 与配置 port 作为抓取地址。

package openstack

import (
	"context"
	"fmt"
	"log/slog"
	"net"
	"strconv"

	"github.com/gophercloud/gophercloud/v2"
	"github.com/gophercloud/gophercloud/v2/openstack"
	"github.com/gophercloud/gophercloud/v2/openstack/compute/v2/hypervisors"
	"github.com/gophercloud/gophercloud/v2/pagination"
	"github.com/prometheus/common/model"

	"github.com/prometheus/prometheus/discovery/targetgroup"
)

// Hypervisor meta 标签常量（hypervisor_id/hostname/status 等）。
const (
	openstackLabelHypervisorID       = openstackLabelPrefix + "hypervisor_id"
	openstackLabelHypervisorHostIP   = openstackLabelPrefix + "hypervisor_host_ip"
	openstackLabelHypervisorHostName = openstackLabelPrefix + "hypervisor_hostname"
	openstackLabelHypervisorStatus   = openstackLabelPrefix + "hypervisor_status"
	openstackLabelHypervisorState    = openstackLabelPrefix + "hypervisor_state"
	openstackLabelHypervisorType     = openstackLabelPrefix + "hypervisor_type"
)

// HypervisorDiscovery 实现 hypervisor 角色的 refresh 逻辑。
// HypervisorDiscovery discovers OpenStack hypervisors.
type HypervisorDiscovery struct {
	provider     *gophercloud.ProviderClient
	authOpts     *gophercloud.AuthOptions
	region       string
	logger       *slog.Logger
	port         int
	availability gophercloud.Availability
}

// newHypervisorDiscovery returns a new hypervisor discovery.
// 构造 HypervisorDiscovery：绑定 gophercloud Provider 与区域信息。
func newHypervisorDiscovery(provider *gophercloud.ProviderClient, opts *gophercloud.AuthOptions,
	port int, region string, availability gophercloud.Availability, l *slog.Logger,
) *HypervisorDiscovery {
	return &HypervisorDiscovery{
		provider: provider, authOpts: opts,
		region: region, port: port, availability: availability, logger: l,
	}
}

// 认证后分页拉取 hypervisor 列表，填充 HostIP 与状态 meta 标签。
func (h *HypervisorDiscovery) refresh(ctx context.Context) ([]*targetgroup.Group, error) {
	err := openstack.Authenticate(ctx, h.provider, *h.authOpts)
	if err != nil {
		return nil, fmt.Errorf("could not authenticate to OpenStack: %w", err)
	}

	client, err := openstack.NewComputeV2(h.provider, gophercloud.EndpointOpts{
		Region: h.region, Availability: h.availability,
	})
	if err != nil {
		return nil, fmt.Errorf("could not create OpenStack compute session: %w", err)
	}

	tg := &targetgroup.Group{
		Source: "OS_" + h.region,
	}
	// OpenStack API reference
	// https://developer.openstack.org/api-ref/compute/#list-hypervisors-details
	pagerHypervisors := hypervisors.List(client, nil)
	err = pagerHypervisors.EachPage(ctx, func(_ context.Context, page pagination.Page) (bool, error) {
		hypervisorList, err := hypervisors.ExtractHypervisors(page)
		if err != nil {
			return false, fmt.Errorf("could not extract hypervisors: %w", err)
		}
		for _, hypervisor := range hypervisorList {
			labels := model.LabelSet{}
			addr := net.JoinHostPort(hypervisor.HostIP, strconv.Itoa(h.port))
			labels[model.AddressLabel] = model.LabelValue(addr)
			labels[openstackLabelHypervisorID] = model.LabelValue(hypervisor.ID)
			labels[openstackLabelHypervisorHostName] = model.LabelValue(hypervisor.HypervisorHostname)
			labels[openstackLabelHypervisorHostIP] = model.LabelValue(hypervisor.HostIP)
			labels[openstackLabelHypervisorStatus] = model.LabelValue(hypervisor.Status)
			labels[openstackLabelHypervisorState] = model.LabelValue(hypervisor.State)
			labels[openstackLabelHypervisorType] = model.LabelValue(hypervisor.HypervisorType)
			tg.Targets = append(tg.Targets, labels)
		}
		return true, nil
	})
	if err != nil {
		return nil, err
	}

	return []*targetgroup.Group{tg}, nil
}
