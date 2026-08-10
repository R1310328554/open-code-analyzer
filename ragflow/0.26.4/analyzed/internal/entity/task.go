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

// task.go — 文档解析任务实体：记录单文档分页切片、进度、重试与 chunk 产出摘要。
//

package entity

import "time"

// Task 文档处理任务 GORM 实体（表 task）
type Task struct {
	// ID 任务主键
	ID              string     `gorm:"column:id;primaryKey;size:32" json:"id"`
	// DocID 关联文档 ID
	DocID           string     `gorm:"column:doc_id;size:32;not null;index" json:"doc_id"`
	// FromPage 处理起始页码（PDF 等分页格式）
	FromPage        int64      `gorm:"column:from_page;default:0" json:"from_page"`
	// ToPage 处理结束页码上界
	ToPage          int64      `gorm:"column:to_page;default:100000000" json:"to_page"`
	// TaskType 任务类型（parse/embed 等）
	TaskType        string     `gorm:"column:task_type;size:32;not null;default:''" json:"task_type"`
	// Priority 调度优先级，数值越大越优先
	Priority        int64      `gorm:"column:priority;default:0" json:"priority"`
	// BeginAt 任务开始执行时间
	BeginAt         *time.Time `gorm:"column:begin_at;index" json:"begin_at,omitempty"`
	// ProcessDuration 处理耗时（秒）
	ProcessDuration float64    `gorm:"column:process_duration;default:0" json:"process_duration"`
	// Progress 进度 0~1
	Progress        float64    `gorm:"column:progress;default:0;index" json:"progress"`
	// ProgressMsg 进度详情或错误说明
	ProgressMsg     *string    `gorm:"column:progress_msg;type:longtext" json:"progress_msg,omitempty"`
	// RetryCount 已重试次数
	RetryCount      int64      `gorm:"column:retry_count;default:0" json:"retry_count"`
	// Digest 任务结果摘要（如 token 统计）
	Digest          *string    `gorm:"column:digest;type:longtext" json:"digest,omitempty"`
	// ChunkIDs 产出 chunk ID 列表（JSON/逗号分隔）
	ChunkIDs        *string    `gorm:"column:chunk_ids;type:longtext" json:"chunk_ids,omitempty"`
	BaseModel
}

// TableName 返回 GORM 表名 task
func (Task) TableName() string {
	return "task"
}

// Worker 轮询 progress<1 的任务；大文档按 from_page/to_page 拆分子任务。retry_count 达上限后标记失败并写入 progress_msg。
