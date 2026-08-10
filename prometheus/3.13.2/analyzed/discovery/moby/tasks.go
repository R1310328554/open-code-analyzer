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

// Docker Swarm 任务角色发现：列出 Task 并合并 service/node/network 标签，按 PortStatus 或网络附件 IP 生成抓取 target。

// Docker Swarm 任务角色发现：列出 Task 并合并 service/node/network 标签，按 PortStatus 或网络附件 IP 生成抓取 target。

// Docker Swarm 任务角色发现：列出 Task 并合并 service/node/network 标签，按 PortStatus 或网络附件 IP 生成抓取 target。

package moby

import (
	"context"
	"fmt"
	"maps"
	"net"
	"strconv"

	mobynetwork "github.com/moby/moby/api/types/network"
	"github.com/moby/moby/client"
	"github.com/prometheus/common/model"

	"github.com/prometheus/prometheus/discovery/targetgroup"
	"github.com/prometheus/prometheus/util/strutil"
)

// Swarm 任务与容器 label 前缀常量。
const (
	swarmLabelTaskPrefix           = swarmLabel + "task_"
	swarmLabelTaskID               = swarmLabelTaskPrefix + "id"
	swarmLabelTaskDesiredState     = swarmLabelTaskPrefix + "desired_state"
	swarmLabelTaskStatus           = swarmLabelTaskPrefix + "state"
	swarmLabelTaskContainerID      = swarmLabelTaskPrefix + "container_id"
	swarmLabelTaskSlot             = swarmLabelTaskPrefix + "slot"
	swarmLabelTaskPortMode         = swarmLabelTaskPrefix + "port_publish_mode"
	swarmLabelContainerLabelPrefix = swarmLabel + "container_label_"
)

// 刷新 tasks 角色：聚合 service/node/network 标签后展开 TCP 端口 target。
func (d *Discovery) refreshTasks(ctx context.Context) ([]*targetgroup.Group, error) {
	tg := &targetgroup.Group{
		Source: "DockerSwarm",
	}

	tasks, err := d.client.TaskList(ctx, client.TaskListOptions{Filters: d.filters})
	if err != nil {
		return nil, fmt.Errorf("error while listing swarm services: %w", err)
	}

	serviceLabels, servicePorts, err := d.getServicesLabelsAndPorts(ctx)
	if err != nil {
		return nil, fmt.Errorf("error while computing services labels and ports: %w", err)
	}

	nodeLabels, err := d.getNodesLabels(ctx)
	if err != nil {
		return nil, fmt.Errorf("error while computing nodes labels and ports: %w", err)
	}

	networkLabels, err := getNetworksLabels(ctx, d.client, swarmLabel)
	if err != nil {
		return nil, fmt.Errorf("error while computing swarm network labels: %w", err)
	}

	for _, s := range tasks.Items {
		commonLabels := map[string]string{
			swarmLabelTaskID:           s.ID,
			swarmLabelTaskDesiredState: string(s.DesiredState),
			swarmLabelTaskStatus:       string(s.Status.State),
			swarmLabelTaskSlot:         strconv.FormatInt(int64(s.Slot), 10),
		}

		if s.Status.ContainerStatus != nil {
			commonLabels[swarmLabelTaskContainerID] = s.Status.ContainerStatus.ContainerID
		}

		if s.Spec.ContainerSpec != nil {
			for k, v := range s.Spec.ContainerSpec.Labels {
				ln := strutil.SanitizeLabelName(k)
				commonLabels[swarmLabelContainerLabelPrefix+ln] = v
			}
		}

// 合并所属 service 的 ID/name/mode 等公共标签。
		maps.Copy(commonLabels, serviceLabels[s.ServiceID])

		maps.Copy(commonLabels, nodeLabels[s.NodeID])

		for _, p := range s.Status.PortStatus.Ports {
			if p.Protocol != mobynetwork.TCP {
				continue
			}

			labels := model.LabelSet{
				swarmLabelTaskPortMode: model.LabelValue(p.PublishMode),
			}

			for k, v := range commonLabels {
				labels[model.LabelName(k)] = model.LabelValue(v)
			}

			addr := net.JoinHostPort(string(labels[swarmLabelNodeAddress]), strconv.FormatUint(uint64(p.PublishedPort), 10))
			labels[model.AddressLabel] = model.LabelValue(addr)
			tg.Targets = append(tg.Targets, labels)
		}

// 若无 PortStatus，则按网络附件 IP 与 service 端口配置生成 target。
		for _, network := range s.NetworksAttachments {
			for _, address := range network.Addresses {
				var added bool

				ip, _, err := net.ParseCIDR(address.String())
				if err != nil {
					return nil, fmt.Errorf("error while parsing address %s: %w", address, err)
				}

				for _, p := range servicePorts[s.ServiceID] {
					if p.Protocol != mobynetwork.TCP {
						continue
					}
					labels := model.LabelSet{
						swarmLabelTaskPortMode: model.LabelValue(p.PublishMode),
					}

					for k, v := range commonLabels {
						labels[model.LabelName(k)] = model.LabelValue(v)
					}

					for k, v := range networkLabels[network.Network.ID] {
						labels[model.LabelName(k)] = model.LabelValue(v)
					}

					addr := net.JoinHostPort(ip.String(), strconv.FormatUint(uint64(p.PublishedPort), 10))
					labels[model.AddressLabel] = model.LabelValue(addr)

					tg.Targets = append(tg.Targets, labels)
					added = true
				}
				if !added {
					labels := model.LabelSet{}

					for k, v := range commonLabels {
						labels[model.LabelName(k)] = model.LabelValue(v)
					}

					for k, v := range networkLabels[network.Network.ID] {
						labels[model.LabelName(k)] = model.LabelValue(v)
					}

					addr := net.JoinHostPort(ip.String(), strconv.Itoa(d.port))
					labels[model.AddressLabel] = model.LabelValue(addr)

					tg.Targets = append(tg.Targets, labels)
				}
			}
		}
	}
	return []*targetgroup.Group{tg}, nil
}
