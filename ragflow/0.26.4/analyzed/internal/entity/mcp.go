//
//  Copyright 2026 The InfiniFlow Authors. All Rights Reserved.
//
//  Licensed under the Apache License, Version 2.0 (the "License");
//  you may not use this file except in compliance with the License.
//  You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  See the License for the specific language governing permissions and
//  limitations under the License.

// mcp.go — MCP（Model Context Protocol）服务端配置实体：租户级外部工具服务器 URL、类型、变量与请求头。
//

package entity

// MCPServer MCP 服务器配置（名称、URL、server_type、variables/headers JSON）
type MCPServer struct {
	ID          string  `gorm:"column:id;primaryKey;size:32" json:"id"`
	Name        string  `gorm:"column:name;size:255;not null" json:"name"`
	TenantID    string  `gorm:"column:tenant_id;size:32;not null;index" json:"tenant_id"`
	URL         string  `gorm:"column:url;size:2048;not null" json:"url"`
	ServerType  string  `gorm:"column:server_type;size:32;not null" json:"server_type"`
	Description *string `gorm:"column:description;type:longtext" json:"description,omitempty"`
	Variables   JSONMap `gorm:"column:variables;type:longtext" json:"variables,omitempty"`
	Headers     JSONMap `gorm:"column:headers;type:longtext" json:"headers,omitempty"`
	BaseModel
}

// TableName 返回表名 mcp_server
func (MCPServer) TableName() string {
	return "mcp_server"
}

// MCPServer 通过 tenant_id 隔离；variables/headers 使用 JSONMap 存储任意键值，供 Agent 画布在运行时注入 MCP 连接参数。
