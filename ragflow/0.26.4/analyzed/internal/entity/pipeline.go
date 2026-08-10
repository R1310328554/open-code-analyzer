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

// pipeline.go — 流水线操作日志实体：记录文档/数据集级入库进度、DSL 快照与 operation_status。
//

package entity

import "time"

// PipelineOperationLog 流水线操作日志 GORM 实体（表 pipeline_operation_log）
type PipelineOperationLog struct {
	// ID 日志主键
	ID              string     `gorm:"column:id;primaryKey;size:32" json:"id"`
	// DocumentID 关联文档 ID（数据集级任务使用占位 document_id）
	DocumentID      string     `gorm:"column:document_id;size:32;index" json:"document_id"`
	// TenantID 所属租户
	TenantID        string     `gorm:"column:tenant_id;size:32;not null;index" json:"tenant_id"`
	// KbID 知识库 ID
	KbID            string     `gorm:"column:kb_id;size:32;not null;index" json:"kb_id"`
	// PipelineID 关联流水线 ID
	PipelineID      *string    `gorm:"column:pipeline_id;size:32;index" json:"pipeline_id,omitempty"`
	PipelineTitle   *string    `gorm:"column:pipeline_title;size:32;index" json:"pipeline_title,omitempty"`
	ParserID        string     `gorm:"column:parser_id;size:32;not null;index" json:"parser_id"`
	DocumentName    string     `gorm:"column:document_name;size:255;not null" json:"document_name"`
	DocumentSuffix  string     `gorm:"column:document_suffix;size:255;not null" json:"document_suffix"`
	DocumentType    string     `gorm:"column:document_type;size:255;not null" json:"document_type"`
	SourceFrom      string     `gorm:"column:source_from;size:255;not null" json:"source_from"`
	// Progress 处理进度 0~1
	Progress        float64    `gorm:"column:progress;default:0;index" json:"progress"`
	ProgressMsg     *string    `gorm:"column:progress_msg;type:longtext" json:"progress_msg,omitempty"`
	ProcessBeginAt  *time.Time `gorm:"column:process_begin_at;index" json:"process_begin_at,omitempty"`
	ProcessDuration float64    `gorm:"column:process_duration;default:0" json:"process_duration"`
	// DSL 流水线 DSL 快照 JSON
	DSL             JSONMap    `gorm:"column:dsl;type:longtext" json:"dsl,omitempty"`
	TaskType        string     `gorm:"column:task_type;size:32;not null;default:''" json:"task_type"`
	// OperationStatus 操作状态（running/success/fail 等）
	OperationStatus string     `gorm:"column:operation_status;size:32;not null" json:"operation_status"`
	Avatar          *string    `gorm:"column:avatar;type:longtext" json:"avatar,omitempty"`
	Status          *string    `gorm:"column:status;size:1;index" json:"status,omitempty"`
	BaseModel
}

// TableName 返回 GORM 表名 pipeline_operation_log
func (PipelineOperationLog) TableName() string {
	return "pipeline_operation_log"
}

// 单文件入库与 graph/raptor/mindmap 数据集级任务共用此表；progress_msg 存长文本进度；process_duration 记录耗时秒数。嵌入 BaseModel 继承 create/update 时间戳。
