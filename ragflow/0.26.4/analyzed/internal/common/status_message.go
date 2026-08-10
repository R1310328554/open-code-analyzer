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
// status_message.go — 服务状态上报与采集任务消息：定义心跳/指标/事件类型及 API、Ingestor、FileSyncer 等节点标识。

//

package common

import (
	"time"
)

// MessageType 状态上报消息的类型枚举。
type MessageType string

// 消息类型常量：心跳、指标与事件。
const (
	// MessageHeartbeat 周期性心跳保活消息。
	MessageHeartbeat MessageType = "heartbeat"
	// MessageMetric 性能或资源指标上报。
	MessageMetric    MessageType = "metric"
	// MessageEvent 一次性业务事件通知。
	MessageEvent     MessageType = "event"
)

// ServerType 上报节点的服务角色标识。
type ServerType string

const (
	// ServerTypeAPI API 网关/主服务节点。
	ServerTypeAPI        ServerType = "api_server"
	// ServerTypeIngestion 文档采集/入库 Worker 节点。
	ServerTypeIngestion  ServerType = "ingestor"
	// ServerTypeFileSyncer 文件同步 Worker 节点。
	ServerTypeFileSyncer ServerType = "file_syncer"
)

// BaseMessage 各服务节点上报的通用消息骨架。
type BaseMessage struct {
	MessageID   int64       `json:"report_id"`
	MessageType MessageType `json:"report_type"`
	ServerName  string      `json:"server_id"`
	ServerType  ServerType  `json:"server_type"`
	Host        string      `json:"host"`
	Port        int         `json:"port"`
	Version     string      `json:"version"`
	Timestamp   time.Time   `json:"timestamp"`
	Ext         interface{} `json:"ext,omitempty"`
}

// StartIngestionRequest 触发采集任务的请求体，含任务 ID、类型与来源用户。
type StartIngestionRequest struct {
	TaskID   string `json:"task_id" binding:"required"`
	TaskType string `json:"task_type" binding:"required"`
	From     string `json:"from" binding:"required"`
	UserID   string `json:"user_id" binding:"required"`
}
