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

// STACKIT IAAS API 响应类型定义：映射 ListServersInProject 返回的服务器列表、网卡与标签 JSON 结构。

// STACKIT IAAS API 响应类型定义：映射 ListServersInProject 返回的服务器列表、网卡与标签 JSON 结构。

package stackit

// ServerListResponse 为项目服务器列表 API 的顶层响应体。
// ServerListResponse Response object for server list request.
// https://docs.api.eu01.stackit.cloud/documentation/iaas/version/v1#tag/Servers/operation/v1ListServersInProject
type ServerListResponse struct {
	Items *[]Server `json:"items"`
}

// Server 表示一台 IAAS 虚拟机及其元数据与网卡列表。
type Server struct {
	AvailabilityZone string          `json:"availabilityZone"`
	ID               string          `json:"id"`
	Labels           map[string]any  `json:"labels"`
	MachineType      string          `json:"machineType"`
	Name             string          `json:"name"`
	Nics             []ServerNetwork `json:"nics"`
	PowerStatus      string          `json:"powerStatus"`
	Status           string          `json:"status"`
}

// ServerNetwork 描述服务器与网络的绑定关系及 IPv4/公网地址。
// ServerNetwork Describes the object that matches servers to its networks.
type ServerNetwork struct {
	NetworkName string  `json:"networkName"`
	IPv4        *string `json:"ipv4,omitempty"`
	PublicIP    *string `json:"publicIp,omitempty"`
}
