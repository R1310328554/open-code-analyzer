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
//

package mcp

import (
	"encoding/json"
	"fmt"
	"strings"
)

// ServiceConnector 经进程内 service 层调用实现 Connector，避免自调用 HTTP 往返。
type ServiceConnector struct {
	userID       string
	listDatasets func(userID string, page, pageSize int, orderby string, desc bool) ([]map[string]interface{}, int64, error)
	listChats    func(userID string, page, pageSize int, orderby string, desc bool) ([]map[string]interface{}, int64, error)
	retrieval    func(userID string, req RetrievalRequest) (string, error)
}

// NewServiceConnector 创建 ServiceConnector；函数参数抽象 service 依赖以避免直接 import。
func NewServiceConnector(
	userID string,
	listDatasetsFunc func(userID string, page, pageSize int, orderby string, desc bool) ([]map[string]interface{}, int64, error),
	listChatsFunc func(userID string, page, pageSize int, orderby string, desc bool) ([]map[string]interface{}, int64, error),
	retrievalFunc func(userID string, req RetrievalRequest) (string, error),
) *ServiceConnector {
	return &ServiceConnector{
		userID:       userID,
		listDatasets: listDatasetsFunc,
		listChats:    listChatsFunc,
		retrieval:    retrievalFunc,
	}
}

// ListDatasets 返回换行分隔 JSON，每行含数据集 id/name/description。
func (c *ServiceConnector) ListDatasets(page, pageSize int, orderby string, desc bool) (string, error) {
	data, _, err := c.listDatasets(c.userID, page, pageSize, orderby, desc)
	if err != nil {
		return "", fmt.Errorf("list datasets: %w", err)
	}

	var lines []string
	for _, d := range data {
		id, _ := d["id"].(string)
		name := ""
		if v, ok := d["name"]; ok {
			if s, ok := v.(string); ok {
				name = s
			}
		}
		desc := ""
		if v, ok := d["description"]; ok {
			if s, ok := v.(string); ok {
				desc = s
			}
		}
		// 对齐 Python 输出格式。
		item := map[string]interface{}{
			"id":          id,
			"name":        name,
			"description": desc,
		}
		b, err := json.Marshal(item)
		if err != nil {
			continue
		}
		lines = append(lines, string(b))
	}
	return strings.Join(lines, "\n"), nil
}

// ListChats 返回换行分隔 JSON，每行含聊天助手 id/name/description。
func (c *ServiceConnector) ListChats(page, pageSize int, orderby string, desc bool) (string, error) {
	data, _, err := c.listChats(c.userID, page, pageSize, orderby, desc)
	if err != nil {
		return "", fmt.Errorf("list chats: %w", err)
	}

	var lines []string
	for _, d := range data {
		id, _ := d["id"].(string)
		name := ""
		if v, ok := d["name"]; ok {
			if s, ok := v.(string); ok {
				name = s
			}
		}
		description := ""
		if v, ok := d["description"]; ok {
			if s, ok := v.(string); ok {
				description = s
			}
		}
		item := map[string]interface{}{
			"id":          id,
			"name":        name,
			"description": description,
		}
		b, err := json.Marshal(item)
		if err != nil {
			continue
		}
		lines = append(lines, string(b))
	}
	return strings.Join(lines, "\n"), nil
}

// Retrieval 经进程内 service 执行检索并返回 JSON 字符串。
func (c *ServiceConnector) Retrieval(req RetrievalRequest) (string, error) {
	return c.retrieval(c.userID, req)
}
// mcp/connector.go — MCP 进程内服务连接器，避免自调用 HTTP 往返。
