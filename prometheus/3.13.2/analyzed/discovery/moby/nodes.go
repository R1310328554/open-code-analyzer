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

// Docker Swarm 节点角色发现：列出 Swarm 节点，将 hostname、角色、Manager 状态与平台信息映射为抓取 target。

// Docker Swarm 节点角色发现：列出 Swarm 节点，将 hostname、角色、Manager 状态与平台信息映射为抓取 target。

// Docker Swarm 节点角色发现：列出 Swarm 节点，将 hostname、角色、Manager 状态与平台信息映射为抓取 target。

package moby

import (
	"context"
	"fmt"
	"net"
	"strconv"

	"github.com/moby/moby/client"
	"github.com/prometheus/common/model"

	"github.com/prometheus/prometheus/discovery/targetgroup"
	"github.com/prometheus/prometheus/util/strutil"
)

// Swarm 节点 meta 标签常量（node_id/role/address 等）。
const (
	swarmLabelNodePrefix               = swarmLabel + "node_"
	swarmLabelNodeAddress              = swarmLabelNodePrefix + "address"
	swarmLabelNodeAvailability         = swarmLabelNodePrefix + "availability"
	swarmLabelNodeEngineVersion        = swarmLabelNodePrefix + "engine_version"
	swarmLabelNodeHostname             = swarmLabelNodePrefix + "hostname"
	swarmLabelNodeID                   = swarmLabelNodePrefix + "id"
	swarmLabelNodeLabelPrefix          = swarmLabelNodePrefix + "label_"
	swarmLabelNodeManagerAddr          = swarmLabelNodePrefix + "manager_address"
	swarmLabelNodeManagerLeader        = swarmLabelNodePrefix + "manager_leader"
	swarmLabelNodeManagerReachability  = swarmLabelNodePrefix + "manager_reachability"
	swarmLabelNodePlatformArchitecture = swarmLabelNodePrefix + "platform_architecture"
	swarmLabelNodePlatformOS           = swarmLabelNodePrefix + "platform_os"
	swarmLabelNodeRole                 = swarmLabelNodePrefix + "role"
	swarmLabelNodeStatus               = swarmLabelNodePrefix + "status"
)

// 刷新 nodes 角色：NodeList 过滤后组装 __address__ 与 swarm node 标签。
func (d *Discovery) refreshNodes(ctx context.Context) ([]*targetgroup.Group, error) {
	tg := &targetgroup.Group{
		Source: "DockerSwarm",
	}

	nodes, err := d.client.NodeList(ctx, client.NodeListOptions{Filters: d.filters})
	if err != nil {
		return nil, fmt.Errorf("error while listing swarm nodes: %w", err)
	}

	for _, n := range nodes.Items {
		labels := model.LabelSet{
			swarmLabelNodeID:                   model.LabelValue(n.ID),
			swarmLabelNodeRole:                 model.LabelValue(n.Spec.Role),
			swarmLabelNodeAvailability:         model.LabelValue(n.Spec.Availability),
			swarmLabelNodeHostname:             model.LabelValue(n.Description.Hostname),
			swarmLabelNodePlatformArchitecture: model.LabelValue(n.Description.Platform.Architecture),
			swarmLabelNodePlatformOS:           model.LabelValue(n.Description.Platform.OS),
			swarmLabelNodeEngineVersion:        model.LabelValue(n.Description.Engine.EngineVersion),
			swarmLabelNodeStatus:               model.LabelValue(n.Status.State),
			swarmLabelNodeAddress:              model.LabelValue(n.Status.Addr),
		}
// Manager 节点额外附加 leader/reachability/manager_address 标签。
		if n.ManagerStatus != nil {
			labels[swarmLabelNodeManagerLeader] = model.LabelValue(strconv.FormatBool(n.ManagerStatus.Leader))
			labels[swarmLabelNodeManagerReachability] = model.LabelValue(n.ManagerStatus.Reachability)
			labels[swarmLabelNodeManagerAddr] = model.LabelValue(n.ManagerStatus.Addr)
		}

		for k, v := range n.Spec.Labels {
			ln := strutil.SanitizeLabelName(k)
			labels[model.LabelName(swarmLabelNodeLabelPrefix+ln)] = model.LabelValue(v)
		}

		addr := net.JoinHostPort(n.Status.Addr, strconv.FormatUint(uint64(d.port), 10))
		labels[model.AddressLabel] = model.LabelValue(addr)

		tg.Targets = append(tg.Targets, labels)
	}
	return []*targetgroup.Group{tg}, nil
}

// 为 task 发现提供 node ID → 标签 map，供 tasks 合并节点元数据。
func (d *Discovery) getNodesLabels(ctx context.Context) (map[string]map[string]string, error) {
	nodes, err := d.client.NodeList(ctx, client.NodeListOptions{})
	if err != nil {
		return nil, fmt.Errorf("error while listing swarm nodes: %w", err)
	}
	labels := make(map[string]map[string]string, len(nodes.Items))
	for _, n := range nodes.Items {
		labels[n.ID] = map[string]string{
			swarmLabelNodeID:                   n.ID,
			swarmLabelNodeRole:                 string(n.Spec.Role),
			swarmLabelNodeAddress:              n.Status.Addr,
			swarmLabelNodeAvailability:         string(n.Spec.Availability),
			swarmLabelNodeHostname:             n.Description.Hostname,
			swarmLabelNodePlatformArchitecture: n.Description.Platform.Architecture,
			swarmLabelNodePlatformOS:           n.Description.Platform.OS,
			swarmLabelNodeStatus:               string(n.Status.State),
		}
		for k, v := range n.Spec.Labels {
			ln := strutil.SanitizeLabelName(k)
			labels[n.ID][swarmLabelNodeLabelPrefix+ln] = v
		}
	}
	return labels, nil
}
