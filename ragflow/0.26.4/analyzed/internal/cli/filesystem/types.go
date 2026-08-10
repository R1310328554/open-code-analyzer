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
// types.go — 虚拟文件系统核心类型：节点、命令、列表/搜索选项及 Provider 元数据定义。

//

package filesystem

import "time"

// NodeType represents the type of a node in the virtual filesystem
// NodeType 表示上下文文件系统中的节点种类（目录、数据集、聊天等）。
type NodeType string

// 通用错误消息常量，供各 Provider 返回一致的错误文本。
const (
	NodeTypeDirectory NodeType = "directory"
	NodeTypeFile      NodeType = "file"
	NodeTypeDataset   NodeType = "dataset"
	NodeTypeDocument  NodeType = "document"
	NodeTypeChat      NodeType = "chat"
	NodeTypeAgent     NodeType = "agent"
	NodeTypeUnknown   NodeType = "unknown"
)

// Node represents a node in the context filesystem
// This is the unified output format for all providers
// Node 为各 Provider 统一的输出节点结构（名称、路径、元数据）。
type Node struct {
	Name      string                 `json:"name"`
	Path      string                 `json:"path"`
	Type      NodeType               `json:"type"`
	Size      int64                  `json:"size,omitempty"`
	CreatedAt time.Time              `json:"created_at,omitempty"`
	UpdatedAt time.Time              `json:"updated_at,omitempty"`
	Metadata  map[string]interface{} `json:"metadata,omitempty"`
}

// CommandType represents the type of command
// CommandType 标识 ls/search/cat 等文件系统子命令。
type CommandType string

const (
	CommandList   CommandType = "ls"
	CommandSearch CommandType = "search"
	CommandCat    CommandType = "cat"
)

// Command represents a filesystem command
// Command 描述一次文件系统操作的类型、路径与参数。
type Command struct {
	Type   CommandType            `json:"type"`
	Path   string                 `json:"path"`
	Params map[string]interface{} `json:"params,omitempty"`
}

// ListOptions represents options for list operations
// ListOptions 控制列表递归、分页与排序。
type ListOptions struct {
	Recursive bool   `json:"recursive,omitempty"`
	Limit     int    `json:"limit,omitempty"`
	Offset    int    `json:"offset,omitempty"`
	SortBy    string `json:"sort_by,omitempty"`
	SortOrder string `json:"sort_order,omitempty"` // "asc" or "desc"
}

// SearchOptions represents options for search operations
// SearchOptions 封装语义搜索的 query、top_k、阈值与目录范围。
type SearchOptions struct {
	Query     string   `json:"query"`
	Limit     int      `json:"limit,omitempty"`
	Offset    int      `json:"offset,omitempty"`
	Recursive bool     `json:"recursive,omitempty"`
	TopK      int      `json:"top_k,omitempty"`     // Number of top results to return (default: 10)
	Threshold float64  `json:"threshold,omitempty"` // Similarity threshold (default: 0.2)
	Dirs      []string `json:"dirs,omitempty"`      // List of directories to search in
}

// Result represents the result of a command execution
// Result 为命令执行结果：节点列表、总数与分页游标。
type Result struct {
	Nodes      []*Node `json:"nodes"`
	Total      int     `json:"total"`
	HasMore    bool    `json:"has_more"`
	NextOffset int     `json:"next_offset,omitempty"`
	Error      error   `json:"-"`
}

// PathInfo represents parsed path information
// PathInfo 解析路径后的 Provider 名、组件与资源 ID。
type PathInfo struct {
	Provider     string   // The provider name (e.g., "datasets", "chats")
	Path         string   // The full path
	Components   []string // Path components
	IsRoot       bool     // Whether this is the root path for the provider
	ResourceID   string   // Resource ID if applicable
	ResourceName string   // Resource name if applicable
}

// ProviderInfo holds metadata about a provider
// ProviderInfo 描述单个 Provider 的名称、说明与根路径。
type ProviderInfo struct {
	Name        string `json:"name"`
	Description string `json:"description"`
	RootPath    string `json:"root_path"`
}

// Common error messages
const (
	ErrInvalidPath      = "invalid path"
	ErrProviderNotFound = "provider not found for path"
	ErrNotSupported     = "operation not supported"
	ErrNotFound         = "resource not found"
	ErrUnauthorized     = "unauthorized"
	ErrInternal         = "internal error"
)
