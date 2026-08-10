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

// ingestion_task.go — 结构化摄取任务：任务/子任务（tasklet）状态机及 checkpoint 日志表。

package entity

// IngestionTask 结构化数据摄取主任务（document+dataset+schema）
type IngestionTask struct {
	ID         string  `gorm:"column:id;primaryKey;size:32" json:"id"`
	UserID     string  `gorm:"column:user_id;size:32;not null" json:"user_id"`
	DocumentID string  `gorm:"column:document_id;size:32;not null;index" json:"document_id"`
	DatasetID  string  `gorm:"column:dataset_id;size:32;not null" json:"dataset_id"`
	Schema     JSONMap `gorm:"column:schema;type:longtext" json:"schema"`
	Status     string  `gorm:"column:status;size:32;not null;" json:"status"`
	BaseModel
}

// TableName specify table name
func (IngestionTask) TableName() string {
	return "ingestion_task"
}

// IngestionTaskLog 主任务 checkpoint 日志（自增 id）
type IngestionTaskLog struct {
	ID         int     `gorm:"column:id;primaryKey;autoIncrement" json:"id"`
	TaskID     string  `gorm:"column:task_id;size:32;not null;index" json:"task_id"`
	Checkpoint JSONMap `gorm:"column:checkpoint;type:longtext;not null" json:"checkpoint"`
	BaseModel
}

// TableName specify table name
func (IngestionTaskLog) TableName() string {
	return "ingestion_task_log"
}

// IngestionTasklet 可并行执行的子任务切片
type IngestionTasklet struct {
	ID     string  `gorm:"column:id;primaryKey;size:32" json:"id"`
	TaskID string  `gorm:"column:task_id;size:32;not null;index" json:"task_id"`
	Schema JSONMap `gorm:"column:schema;type:longtext" json:"schema"`
	Status string  `gorm:"column:status;size:32;not null;" json:"status"`
	BaseModel
}

// TableName specify table name
func (IngestionTasklet) TableName() string {
	return "ingestion_tasklet"
}

// IngestionTaskletLog 子任务 checkpoint 日志
type IngestionTaskletLog struct {
	ID         int     `gorm:"column:id;primaryKey;autoIncrement" json:"id"`
	TaskletID  string  `gorm:"column:tasklet_id;size:32;not null;index" json:"tasklet_id"`
	Checkpoint JSONMap `gorm:"column:checkpoint;type:longtext;not null" json:"checkpoint"`
	BaseModel
}

// TableName specify table name
func (IngestionTaskletLog) TableName() string {
	return "ingestion_tasklet_log"
}

// schema/checkpoint 均为 JSONMap，便于存储任意摄取管道配置与断点。Tasklet 通过 task_id 关联主任务，支持细粒度重试与进度恢复。
