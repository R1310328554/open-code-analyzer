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

// evaluation.go — RAG 评测数据集实体：数据集、用例、运行批次与逐条评测结果；时间字段自定义以对齐 Python。

package entity

// EvaluationDataset 评测数据集（绑定 kb_ids、创建者）
// 注：Python 使用非空 create_time/update_time，未嵌入 BaseModel
type EvaluationDataset struct {
	ID          string  `gorm:"column:id;primaryKey;size:32" json:"id"`
	TenantID    string  `gorm:"column:tenant_id;size:32;not null;index" json:"tenant_id"`
	Name        string  `gorm:"column:name;size:255;not null;index" json:"name"`
	Description *string `gorm:"column:description;type:longtext" json:"description,omitempty"`
	KbIDs       JSONMap `gorm:"column:kb_ids;type:longtext;not null" json:"kb_ids"`
	CreatedBy   string  `gorm:"column:created_by;size:32;not null;index" json:"created_by"`
	// Custom time fields (not null) to match Python
	CreateTime int64 `gorm:"column:create_time;not null;index" json:"create_time"`
	UpdateTime int64 `gorm:"column:update_time;not null" json:"update_time"`
	Status     int64 `gorm:"column:status;default:1;index" json:"status"`
}

// TableName specify table name
func (EvaluationDataset) TableName() string {
	return "evaluation_datasets"
}

// EvaluationCase 单条问答用例（标准答案、相关 doc/chunk ID）
// Note: Python defines custom create_time (not null) instead of using BaseModel's
type EvaluationCase struct {
	ID               string   `gorm:"column:id;primaryKey;size:32" json:"id"`
	DatasetID        string   `gorm:"column:dataset_id;size:32;not null;index" json:"dataset_id"`
	Question         string   `gorm:"column:question;type:longtext;not null" json:"question"`
	ReferenceAnswer  *string  `gorm:"column:reference_answer;type:longtext" json:"reference_answer,omitempty"`
	RelevantDocIDs   *JSONMap `gorm:"column:relevant_doc_ids;type:longtext" json:"relevant_doc_ids,omitempty"`
	RelevantChunkIDs *JSONMap `gorm:"column:relevant_chunk_ids;type:longtext" json:"relevant_chunk_ids,omitempty"`
	Metadata         *JSONMap `gorm:"column:metadata;type:longtext" json:"metadata,omitempty"`
	// Custom time field (not null) to match Python
	CreateTime int64 `gorm:"column:create_time;not null" json:"create_time"`
}

// TableName specify table name
func (EvaluationCase) TableName() string {
	return "evaluation_cases"
}

// EvaluationRun 一次评测运行（dialog 快照、metrics_summary、状态机）
// Note: Python defines custom create_time/complete_time instead of using BaseModel's
type EvaluationRun struct {
	ID             string   `gorm:"column:id;primaryKey;size:32" json:"id"`
	DatasetID      string   `gorm:"column:dataset_id;size:32;not null;index" json:"dataset_id"`
	DialogID       string   `gorm:"column:dialog_id;size:32;not null;index" json:"dialog_id"`
	Name           string   `gorm:"column:name;size:255;not null" json:"name"`
	ConfigSnapshot JSONMap  `gorm:"column:config_snapshot;type:longtext;not null" json:"config_snapshot"`
	MetricsSummary *JSONMap `gorm:"column:metrics_summary;type:longtext" json:"metrics_summary,omitempty"`
	Status         string   `gorm:"column:status;size:32;not null;default:PENDING" json:"status"`
	CreatedBy      string   `gorm:"column:created_by;size:32;not null;index" json:"created_by"`
	// Custom time fields to match Python
	CreateTime   int64  `gorm:"column:create_time;not null;index" json:"create_time"`
	CompleteTime *int64 `gorm:"column:complete_time" json:"complete_time,omitempty"`
}

// TableName specify table name
func (EvaluationRun) TableName() string {
	return "evaluation_runs"
}

// EvaluationResult 单用例评测输出（生成答案、检索块、指标）
// Note: Python defines custom create_time (not null) instead of using BaseModel's
type EvaluationResult struct {
	ID              string   `gorm:"column:id;primaryKey;size:32" json:"id"`
	RunID           string   `gorm:"column:run_id;size:32;not null;index" json:"run_id"`
	CaseID          string   `gorm:"column:case_id;size:32;not null;index" json:"case_id"`
	GeneratedAnswer string   `gorm:"column:generated_answer;type:longtext;not null" json:"generated_answer"`
	RetrievedChunks JSONMap  `gorm:"column:retrieved_chunks;type:longtext;not null" json:"retrieved_chunks"`
	Metrics         JSONMap  `gorm:"column:metrics;type:longtext;not null" json:"metrics"`
	ExecutionTime   float64  `gorm:"column:execution_time;not null" json:"execution_time"`
	TokenUsage      *JSONMap `gorm:"column:token_usage;type:longtext" json:"token_usage,omitempty"`
	// Custom time field to match Python
	CreateTime int64 `gorm:"column:create_time;not null" json:"create_time"`
}

// TableName specify table name
func (EvaluationResult) TableName() string {
	return "evaluation_results"
}

// EvaluationRun.status 默认 PENDING；config_snapshot 冻结运行时的 Dialog 配置。EvaluationResult.retrieved_chunks/metrics 均为 JSONMap 便于扩展指标。
