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

// Docker/Swarm 网络标签辅助：列出 overlay/bridge 网络，将 ID、名称、scope 与用户 label 映射为 meta 标签供 task/service 合并。

// Docker/Swarm 网络标签辅助：列出 overlay/bridge 网络，将 ID、名称、scope 与用户 label 映射为 meta 标签供 task/service 合并。

// Docker/Swarm 网络标签辅助：列出 overlay/bridge 网络，将 ID、名称、scope 与用户 label 映射为 meta 标签供 task/service 合并。

package moby

import (
	"context"
	"strconv"

	"github.com/moby/moby/client"

	"github.com/prometheus/prometheus/util/strutil"
)

const (
	labelNetworkPrefix      = "network_"
	labelNetworkID          = labelNetworkPrefix + "id"
	labelNetworkName        = labelNetworkPrefix + "name"
	labelNetworkScope       = labelNetworkPrefix + "scope"
	labelNetworkInternal    = labelNetworkPrefix + "internal"
	labelNetworkIngress     = labelNetworkPrefix + "ingress"
	labelNetworkLabelPrefix = labelNetworkPrefix + "label_"
)

// 拉取全部 Docker 网络，按 network ID 返回可合并的标签 map。
func getNetworksLabels(ctx context.Context, c *client.Client, labelPrefix string) (map[string]map[string]string, error) {
	networks, err := c.NetworkList(ctx, client.NetworkListOptions{})
	if err != nil {
		return nil, err
	}
	labels := make(map[string]map[string]string, len(networks.Items))
// 遍历每个网络：填充基础字段并 sanitize 用户自定义 label。
	for _, network := range networks.Items {
		labels[network.ID] = map[string]string{
			labelPrefix + labelNetworkID:       network.ID,
			labelPrefix + labelNetworkName:     network.Name,
			labelPrefix + labelNetworkScope:    network.Scope,
			labelPrefix + labelNetworkInternal: strconv.FormatBool(network.Internal),
			labelPrefix + labelNetworkIngress:  strconv.FormatBool(network.Ingress),
		}
		for k, v := range network.Labels {
			ln := strutil.SanitizeLabelName(k)
			labels[network.ID][labelPrefix+labelNetworkLabelPrefix+ln] = v
		}
	}

	return labels, nil
}
