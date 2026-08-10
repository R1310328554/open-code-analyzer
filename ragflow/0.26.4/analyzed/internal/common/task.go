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
// task.go — 异步任务消息与处理句柄：定义采集任务类型常量及 TaskMessage/TaskHandle 接口供队列消费者使用。

//

package common

// 采集任务类型常量，与 Ingestion Worker 路由对应。
const (
	// TaskTypeIngestionTask 完整采集主任务。
	TaskTypeIngestionTask    = "ingestion_task"
	// TaskTypeIngestionTasklet 采集子任务/分片任务。
	TaskTypeIngestionTasklet = "ingestion_tasklet"
	// TaskTypeIngestionTest 采集连通性测试任务。
	TaskTypeIngestionTest    = "ingestion_test"
)

// TaskMessage 队列消息体，含任务 ID 与类型。
type TaskMessage struct {
	TaskID   string `json:"task_id" binding:"required"`
	TaskType string `json:"task_type" binding:"required"`
}

// TaskHandle 任务消费句柄：读取消息并 Ack/Nack 确认。
type TaskHandle interface {
	// GetMessage 返回当前待处理的任务消息。
	GetMessage() TaskMessage
	// Ack 确认任务处理成功，从队列移除。
	Ack() error
	// Nack 拒绝任务，通常触发重试或死信。
	Nack() error
}
