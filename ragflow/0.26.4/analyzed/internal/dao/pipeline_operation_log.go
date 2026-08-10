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
// pipeline_operation_log.go — 流水线操作日志数据访问层：查询知识库级（图谱/Raptor/思维导图）与单文件级入库日志，支持分页、排序白名单与多条件筛选。

//

package dao

import (
	"strings"

	"ragflow/internal/entity"
)

// graphRaptorFakeDocID 数据集级（graph/raptor/mindmap）流水线日志使用的占位 document_id，对齐 Python task_service.py 中的 GRAPH_RAPTOR_FAKE_DOC_ID。
const graphRaptorFakeDocID = "graph_raptor_x"

// pipelineLogOrderableColumns 可排序列白名单，防止攻击者通过 orderby 参数注入任意 SQL。
var pipelineLogOrderableColumns = map[string]struct{}{
	"id":               {},
	"document_id":      {},
	"tenant_id":        {},
	"kb_id":            {},
	"pipeline_id":      {},
	"pipeline_title":   {},
	"parser_id":        {},
	"document_name":    {},
	"document_suffix":  {},
	"document_type":    {},
	"source_from":      {},
	"progress":         {},
	"process_begin_at": {},
	"process_duration": {},
	"task_type":        {},
	"operation_status": {},
	"status":           {},
	"create_time":      {},
	"create_date":      {},
	"update_time":      {},
	"update_date":      {},
}

// pipelineLogOrderClause 校验 orderby 是否在白名单内，非法则回退 create_time，并拼接 ASC/DESC。
func pipelineLogOrderClause(orderby string, desc bool) string {
	if _, ok := pipelineLogOrderableColumns[orderby]; !ok {
		orderby = "create_time"
	}
	if desc {
		return orderby + " DESC"
	}
	return orderby + " ASC"
}

// PipelineOperationLogDAO 流水线操作日志表的数据访问对象。
type PipelineOperationLogDAO struct{}

// NewPipelineOperationLogDAO 创建流水线操作日志 DAO 实例。
func NewPipelineOperationLogDAO() *PipelineOperationLogDAO {
	return &PipelineOperationLogDAO{}
}

// GetDatasetLogsByKBID 列出知识库的数据集级入库日志（document_id 为 graphRaptorFakeDocID）。仅当 page 与 pageSize 均为正数时分页，对齐 peewee paginate 行为。
func (dao *PipelineOperationLogDAO) GetDatasetLogsByKBID(kbID string, page, pageSize int, orderby string, desc bool, operationStatus []string, createDateFrom, createDateTo, keywords string) ([]*entity.PipelineOperationLog, int64, error) {
	query := DB.Model(&entity.PipelineOperationLog{}).
		Where("kb_id = ? AND document_id = ?", kbID, graphRaptorFakeDocID)

	if keywords != "" {
		query = query.Where("LOWER(document_name) LIKE ?", "%"+strings.ToLower(keywords)+"%")
	}
	if len(operationStatus) > 0 {
		query = query.Where("operation_status IN ?", operationStatus)
	}
	if createDateFrom != "" {
		query = query.Where("create_date >= ?", createDateFrom)
	}
	if createDateTo != "" {
		query = query.Where("create_date <= ?", createDateTo)
	}

	var count int64
	if err := query.Count(&count).Error; err != nil {
		return nil, 0, err
	}

	// 上文通过 pipelineLogOrderClause 将 orderby 与白名单比对，未命中则使用安全默认值；流入 Order() 的仅为白名单列名加 ASC/DESC 后缀。
	// codeql[go/sql-injection] 误报：pipelineLogOrderClause 已做列名校验
	query = query.Order(pipelineLogOrderClause(orderby, desc))
	if page > 0 && pageSize > 0 {
		query = query.Offset((page - 1) * pageSize).Limit(pageSize)
	}

	var logs []*entity.PipelineOperationLog
	if err := query.Find(&logs).Error; err != nil {
		return nil, 0, err
	}
	return logs, count, nil
}

// GetFileLogsByKBID 列出知识库内单文件级入库日志（排除数据集级占位 document_id）。
func (dao *PipelineOperationLogDAO) GetFileLogsByKBID(kbID string, page, pageSize int, orderby string, desc bool, keywords string, operationStatus []string, createDateFrom, createDateTo string) ([]*entity.PipelineOperationLog, int64, error) {
	query := DB.Model(&entity.PipelineOperationLog{}).
		Where("kb_id = ?", kbID)

	if keywords != "" {
		query = query.Where("LOWER(document_name) LIKE ?", "%"+strings.ToLower(keywords)+"%")
	}
	query = query.Where("document_id <> ?", graphRaptorFakeDocID)

	if len(operationStatus) > 0 {
		query = query.Where("operation_status IN ?", operationStatus)
	}
	if createDateFrom != "" {
		query = query.Where("create_date >= ?", createDateFrom)
	}
	if createDateTo != "" {
		query = query.Where("create_date <= ?", createDateTo)
	}

	var count int64
	if err := query.Count(&count).Error; err != nil {
		return nil, 0, err
	}

	// above validates `orderby` against pipelineLogOrderableColumns
	// (a closed allowlist of column names) and defaults to a safe value
	// if no match is found. The only string that flows into Order() is
	// the whitelisted column name + " ASC"/" DESC" suffix.
	// codeql[go/sql-injection] False positive: pipelineLogOrderClause
	query = query.Order(pipelineLogOrderClause(orderby, desc))
	if page > 0 && pageSize > 0 {
		query = query.Offset((page - 1) * pageSize).Limit(pageSize)
	}

	var logs []*entity.PipelineOperationLog
	if err := query.Find(&logs).Error; err != nil {
		return nil, 0, err
	}
	return logs, count, nil
}

// GetByIDAndKBID 在指定知识库范围内按日志 ID 查询单条入库记录。
func (dao *PipelineOperationLogDAO) GetByIDAndKBID(logID, kbID string) (*entity.PipelineOperationLog, error) {
	var log entity.PipelineOperationLog
	if err := DB.Where("id = ? AND kb_id = ?", logID, kbID).First(&log).Error; err != nil {
		return nil, err
	}
	return &log, nil
}
